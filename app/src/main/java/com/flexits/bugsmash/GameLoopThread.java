package com.flexits.bugsmash;

import android.graphics.Canvas;

import java.util.ArrayList;

//all calculations of the game world are made here and saved into the LiveData object
public class GameLoopThread extends Thread implements Runnable{
<<<<<<< HEAD
    private final GameGlobal gameGlobal;
=======

    private final GameViewModel gameViewModel;
>>>>>>> temp
    private final GameView gameView;
    private final GameGlobal gameGlobal;
    private boolean isRunning = false;

    public GameLoopThread(GameView gameView) {
        this.gameView = gameView;
        gameGlobal = (GameGlobal) gameView.getContext().getApplicationContext();
    }

    //set flat to start or stop the thread
    public void setState(boolean isRunning) {
        this.isRunning = isRunning;
    }

    @Override
    public void run() {
        while (isRunning) {
<<<<<<< HEAD
            for(Mob m : gameGlobal.getMobs()){
=======
            for(Mob m : gameViewModel.getMobs().getValue()){
>>>>>>> temp
                if (!m.isAlive()) continue;
                int x = m.getX_coord();
                int x_scr = gameView.getWidth();
                int bmp_width = m.getSpecies().getBmp().getWidth();
                if (x < (x_scr - bmp_width)) x++;
                //else x = 0;
                m.setX_coord(x);
            }
<<<<<<< HEAD

            Canvas canvas = null;
            try {
                //try to lock the resource to avoid conflicts
                canvas = gameView.getHolder().lockCanvas();
                synchronized (gameView.getHolder()) {
                    //update the screen upon lock acquisition
                    if ((canvas != null) && isRunning) gameView.draw(canvas);
                }
            } finally {
                //unlock the resource if locked
                if (canvas != null) {
                    gameView.getHolder().unlockCanvasAndPost(canvas);
                }
            }
=======
>>>>>>> temp

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            gameViewModel.getIsUpdated().postValue(Boolean.TRUE);
            //TODO control FPS
        }
    }
}
