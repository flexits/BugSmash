package com.flexits.bugsmash;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;
<<<<<<< HEAD
=======
    private GameGlobal gameGlobal;
    private GameViewModel gameViewModel;
>>>>>>> temp
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

<<<<<<< HEAD
=======
        //create LiveData model and populate the list of entities
        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
        gameGlobal = (GameGlobal) getApplication();
        ArrayList<Mob> mobs = gameGlobal.getMobs();

        if (mobs.size() <= 0) {
            MobSpecies ms1 = new MobSpecies(BitmapFactory.decodeResource(getResources(), R.drawable.spider_40px));
            Mob mb1 = new Mob(1, 10, 0, true, ms1);
            gameViewModel.getMobs().getValue().add(mb1);
        } else{
            gameViewModel.getMobs().setValue(mobs);
        }

>>>>>>> temp
        //create gameview
        gameView = new GameView(this);
        setContentView(gameView);

        //create background worker thread
        gameLoopThread = new GameLoopThread(gameView);

<<<<<<< HEAD
=======
        //get LiveData provider to observe changes
        final Observer<Boolean> observer = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean flag) {
                if (!Boolean.TRUE.equals(flag)) return;
                performDraw();
            }
        };
        gameViewModel.getIsUpdated().observe(this, observer);
    }

    private void performDraw() {
        Canvas canvas = null;
        SurfaceHolder sfhold = gameView.getHolder();
        if (!sfhold.getSurface().isValid()) return;
        try {
            //try to lock the resource to avoid conflicts
            canvas = sfhold.lockCanvas();
            synchronized (sfhold) {
                //update the screen upon lock acquisition
                if (canvas != null) {
                    gameView.draw(canvas);
                    gameViewModel.getIsUpdated().setValue(Boolean.FALSE);
                }
            }
        } finally {
            //unlock the resource if locked
            if (canvas != null) {
                sfhold.unlockCanvasAndPost(canvas);
            }
        }
>>>>>>> temp
    }


    @Override
    protected void onResume() {
        super.onResume();
        performDraw();
        try {
            gameLoopThread.setState(true);
            Thread.State state = gameLoopThread.getState();
            if (state == Thread.State.NEW) gameLoopThread.start();
            //if (!gameLoopThread.isAlive()) gameLoopThread.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            gameLoopThread.setState(false);
            gameLoopThread.interrupt();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //remember current entities
        gameGlobal.setMobs(new ArrayList<>(gameViewModel.getMobs().getValue()));

        //TODO what is the meaning of this:
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
}