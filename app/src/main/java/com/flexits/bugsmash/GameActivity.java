package com.flexits.bugsmash;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;
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

        //create LiveData model and populate the list of entities
        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
        MobSpecies ms1 = new MobSpecies(BitmapFactory.decodeResource(getResources(), R.drawable.spider_40px));
        Mob mb1 = new Mob(1, 10, 0, true, ms1);
        //gameViewModel.getMobs().getValue().add(mb1);

        GameGlobal gameGlobal = (GameGlobal) getApplicationContext();
        gameGlobal.getMobs().add(mb1);

        //create gameview
        gameView = new GameView(this, gameViewModel);
        setContentView(gameView);

        //create background worker thread
        gameLoopThread = new GameLoopThread(gameView, gameViewModel);


        //get LiveData provider to observe changes
        final Observer<Boolean> observer = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean flag) {
                if (!Boolean.TRUE.equals(flag)) return;
                Canvas canvas = null;
                try {
                    //try to lock the resource to avoid conflicts
                    canvas = gameView.getHolder().lockCanvas();
                    synchronized (gameView.getHolder()) {
                        //update the screen upon lock acquisition
                        if ((canvas != null) && gameLoopThread.getRunning()) gameView.draw(canvas);
                    }
                } finally {
                    //unlock the resource if locked
                    if (canvas != null) {
                        gameView.getHolder().unlockCanvasAndPost(canvas);
                    }
                    gameViewModel.getIsUpdated().setValue(Boolean.FALSE);
                }
            }
        };
        //gameViewModel.getIsUpdated().observe(this, observer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        gameLoopThread.setState(true);
        Thread.State state = gameLoopThread.getState();
        if (!gameLoopThread.isAlive()) gameLoopThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameLoopThread.setState(false);
        //TODO what is the meaning of this:
        boolean retry = true;
        while (retry) {
            try {
                gameLoopThread.join();
                retry = false;
            } catch (InterruptedException e) { e.printStackTrace(); }
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
}