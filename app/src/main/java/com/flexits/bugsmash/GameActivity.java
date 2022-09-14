package com.flexits.bugsmash;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;
    private GameGlobal gameGlobal;
    private GameViewModel gameViewModel;
    private GameLoopThread gameLoopThread;

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
        Point displSize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(displSize);

        //create LiveData model and populate the list of entities
        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
        gameGlobal = (GameGlobal) getApplication();
        ArrayList<Mob> mobs = gameGlobal.getMobs();
        if (mobs.size() <= 0) { mobsGenerator(mobs, displSize); }
        gameViewModel.getMobs().setValue(mobs);

        //create gameview
        gameView = new GameView(this, gameViewModel);
        setContentView(gameView);

        //get LiveData provider to observe changes
        final Observer<Boolean> observer = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean flag) {
                if (!Boolean.TRUE.equals(flag)) return;
                gameView.invalidate();
                gameViewModel.getIsUpdated().setValue(Boolean.FALSE);
            }
        };
        gameViewModel.getIsUpdated().observe(this, observer);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //create background worker thread
        gameLoopThread = new GameLoopThread(gameViewModel);
        gameLoopThread.allowExecution(true);
        gameLoopThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            gameLoopThread.allowExecution(false);
            //gameLoopThread.interrupt();
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
    private void mobsGenerator(ArrayList<Mob> mobs, Point viewSize){
        final int MOBS_QUANTITY = 13;

        if (mobs == null) mobs = new ArrayList<>();
        else mobs.clear();

        //mobs are generated at the borders of the screen on either side of it
        //mob species are picked randomly

        //list of available species
        ArrayList<MobSpecies> species = new ArrayList<>();
        species.add(new MobSpecies(BitmapFactory.decodeResource(getResources(), R.drawable.spider_40px)));

        for (int i=0; i<MOBS_QUANTITY; i++){
            //pick random species
            MobSpecies ms = species.get(generateRnd(0,species.size()));
            //pick random screen side to spawn a mob on
            int sideIndex = generateRnd(0,4);
            //generate coordinates and movement vectors' angles
            //(a mob never goes back; a movement vector is normal to the side a mob is spawned on)
            int x_max = viewSize.x - ms.getBmp().getWidth();
            int y_max = viewSize.y - ms.getBmp().getHeight();
            int x=0, y=0, angleDeg = 0;
            switch (sideIndex){
                case 1:
                    //upper side
                    y = 0;
                    x = generateRnd(0,x_max);
                    angleDeg = 270;
                    break;
                case 2:
                    //right side
                    x = x_max;
                    y = generateRnd(0,y_max);
                    angleDeg = 180;
                    break;
                case 3:
                    //bottom side
                    y = y_max;
                    x = generateRnd(0,x_max);
                    angleDeg = 90;
                    break;
                case 4:
                    //left side
                    x = 0;
                    y = generateRnd(0,y_max);
                    angleDeg = 360;
                    break;
            }
            //ensure the objects don't overlap
            /*boolean isOverlapping = false;
            int x_end = x + ms.getBmp().getWidth();
            int y_end = y + ms.getBmp().getHeight();
            for (Mob m : mobs){
                isOverlapping = false;
                int mob_x_start = (int)m.getCoord().x;
                int mob_x_end = mob_x_start + m.getSpecies().getBmp().getWidth();
                if (x > mob_x_end || x_end < mob_x_start) continue;
                int mob_y_start = (int)m.getCoord().y;
                int mob_y_end = mob_y_start + m.getSpecies().getBmp().getHeight();
                if (y > mob_y_end || y_end < mob_y_start) continue;
                isOverlapping = true;
                break;
            }*/
            if (isOverlapping
                    (mobs,
                    new Point(x , y),
                    new Point(ms.getBmp().getWidth(), ms.getBmp().getHeight()))
            ){
                i--;
                continue;
            }
            //deflect movement vector +- 45 degrees
            angleDeg += (generateRnd(0, 90) - 45);
            mobs.add(new Mob(x, y, angleDeg,true, ms));
        }

    }

    private int generateRnd(int min, int max){
        if (min>=max) throw new IllegalArgumentException();
        if (min+1 == max) return min;
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    private boolean isOverlapping(ArrayList<Mob> mobs, Point coordinates, Point dimensions){
        int x = coordinates.x;
        int y = coordinates.y;
        int x_end = x + dimensions.x;
        int y_end = y + dimensions.y;
        boolean result = false;
        for (Mob m : mobs){
            int mob_x_start = (int)m.getCoord().x;
            int mob_x_end = mob_x_start + m.getSpecies().getBmp().getWidth();
            if (x > mob_x_end || x_end < mob_x_start) continue;
            int mob_y_start = (int)m.getCoord().y;
            int mob_y_end = mob_y_start + m.getSpecies().getBmp().getHeight();
            if (y > mob_y_end || y_end < mob_y_start) continue;
            result = true;
            break;
        }
        return result;
    }
}