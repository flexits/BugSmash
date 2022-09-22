package com.flexits.bugsmash;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;
    private GameGlobal gameGlobal;
    private GameViewModel gameViewModel;
    private GameLoopThread gameLoopThread;
    private SharedPreferences sPref;
    private Point displSize;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide the status bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //display content edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        //get display size
        displSize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(displSize);

        //instantiate sound manager
        SoundManager.instantiate(this);

        //create LiveData model and populate the list of entities
        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
        gameGlobal = (GameGlobal) getApplication();

        //init preferences
        sPref = getSharedPreferences(
                getResources().getString(R.string.pref_filename),
                Context.MODE_PRIVATE);

        //generate mobs if new game or load existing collection on resume
        ArrayList<Mob> mobs = gameGlobal.getMobs();
        if (mobs.size() <= 0) {
            int mobs_quantity = 0;
            try{
                mobs_quantity = Integer.parseInt(
                        sPref.getString(
                                getResources().getString(R.string.pref_mobs_quantity),
                                getResources().getString(R.string.pref_mobs_quantity_default))
                );
            } catch (Exception e){ e.printStackTrace(); }
            mobsGenerator(mobs, mobs_quantity, displSize);
        }
        gameViewModel.getMobs().setValue(mobs);

        //create gameview
        gameView = new GameView(this, gameViewModel);
        setContentView(gameView);

        //observe LiveData changes flag and update screen if true
        final Observer<Boolean> updateObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean flag) {
                if (!Boolean.TRUE.equals(flag)) return;
                gameView.invalidate();
                gameViewModel.getIsUpdated().setValue(Boolean.FALSE);
            }
        };
        gameViewModel.getIsUpdated().observe(this, updateObserver);

        //observe LiveData gameover flag and update score if true
        final Observer<Boolean> gameoverObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean flag) {
                if (!Boolean.TRUE.equals(flag)) return;
                updateMaxScore();
                mobs.clear();
            }
        };
        gameViewModel.getIsOver().observe(this, gameoverObserver);

        //create touch listener
        gameView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gameLoopThread.hitTest(new PointF(motionEvent.getX(), motionEvent.getY()));
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //create background worker thread
        gameLoopThread = new GameLoopThread(gameViewModel, displSize);
        gameLoopThread.allowExecution(true);
        gameLoopThread.start();
        //TODO implement game pause/resume - score
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            gameLoopThread.allowExecution(false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //remember current entities
        gameGlobal.setMobs(new ArrayList<>(gameViewModel.getMobs().getValue()));

        //wait for the thread to terminate
        boolean retry = true;
        while (retry) {
            try {
                gameLoopThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace(); }
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        SoundManager.release();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //hide the system bars
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    //generate a list of mobs with random coordinates
    private void mobsGenerator(ArrayList<Mob> mobs, int quantity, Point viewSize){
        final int PLACEMENT_ITERATIONS = 20;    //limit of attempts to place mobs without overlapping

        if (mobs == null) mobs = new ArrayList<>();
        else mobs.clear();

        //list of available species
        ArrayList<MobSpecies> species = new ArrayList<>();
        species.add(new MobSpecies(BitmapFactory.decodeResource(getResources(), R.drawable.spider_40px)));
        species.add(new MobSpecies(BitmapFactory.decodeResource(getResources(), R.drawable.hornet_40px)));

        //generate a given number of mobs with random placement at the borders of the screen and
        //random movement vectors, trying to avoid overlapping
        for (int counter=0, attempts=0; counter<quantity && attempts<PLACEMENT_ITERATIONS;){
            //pick random species
            MobSpecies ms = species.get(generateRnd(0, species.size()-1));
            Bitmap ms_bmp = ms.getBmp();
            //pick random screen side to spawn a mob on
            int sideIndex = generateRnd(1, 4);
            //generate coordinates and movement vectors' angles
            //(a mob never goes back; a movement vector is normal to the side a mob is spawned on)
            int x_max = viewSize.x - ms_bmp.getWidth();
            int y_max = viewSize.y - ms_bmp.getHeight();
            int x=0, y=0, angleDeg = 0;
            switch (sideIndex){
                case 1:
                    //upper side
                    y = 0;
                    x = generateRnd(0, x_max);
                    angleDeg = 180;
                    break;
                case 2:
                    //right side
                    x = x_max;
                    y = generateRnd(0, y_max);
                    angleDeg = 270;
                    break;
                case 3:
                    //bottom side
                    y = y_max;
                    x = generateRnd(0, x_max);
                    angleDeg = 360;
                    break;
                case 4:
                    //left side
                    x = 0;
                    y = generateRnd(0, y_max);
                    angleDeg = 90;
                    break;
            }
            //ensure the objects don't overlap
            PointF startp = new PointF(x , y);
            PointF dimensns = new PointF(ms_bmp.getWidth(), ms_bmp.getHeight());
            boolean isOverlapping = false;
            for (Mob m : mobs) {
                if (m.checkOverlap(startp, dimensns)){
                    isOverlapping = true;
                    break;
                }
            }
            if (isOverlapping){
                attempts++;
                continue;
            }
            //randomly deflect movement vector +- 45 degrees
            angleDeg += (generateRnd(0, 90) - 45);
            mobs.add(new Mob(x, y, angleDeg,true, ms));
            counter++;
            attempts = 0;
        }
    }

    private int generateRnd(int min, int max){
        if (min >= max) throw new IllegalArgumentException();
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    private void updateMaxScore(){
        int max_score = 0;
        try {
            max_score = Integer.parseInt(
                    sPref.getString(
                            getResources().getString(R.string.pref_max_score),
                            getResources().getString(R.string.pref_max_score_default))
            );
        } catch (Exception e){ e.printStackTrace(); }
        int current_score = gameViewModel.getScore().getValue();
        if (current_score > max_score){
            SharedPreferences.Editor spEditor =  sPref.edit();
            spEditor.putString(
                    getResources().getString(R.string.pref_max_score),
                    String.valueOf(current_score)
            );
            spEditor.commit();
        }
    }
}