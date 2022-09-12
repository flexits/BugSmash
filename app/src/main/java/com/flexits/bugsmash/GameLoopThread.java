package com.flexits.bugsmash;

import android.graphics.Canvas;

//all calculations of the game world are made here and saved into the LiveData object
public class GameLoopThread extends Thread implements Runnable{

    private final GameViewModel gameViewModel;
    private final GameGlobal gameGlobal;
    private final GameView gameView;
    private boolean isRunning = false;

    public GameLoopThread(GameView gameView, GameViewModel gameViewModel) {
        this.gameView = gameView;
        this.gameViewModel = gameViewModel;
        gameGlobal = (GameGlobal) gameView.getContext().getApplicationContext();
    }

    //set flat to start or stop the thread
    public void setState(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean getRunning() { return isRunning; }

    @Override
    public void run() {
        while (isRunning) {
            //for(Mob m : gameViewModel.getMobs().getValue()){
            for(Mob m : gameGlobal.getMobs()){
                if (!m.isAlive()) continue;
                int x = m.getX_coord();
                int x_scr = gameView.getWidth();
                int bmp_width = m.getSpecies().getBmp().getWidth();
                if (x < (x_scr - bmp_width)) x++;
                //else x = 0;
                m.setX_coord(x);
            }
            //gameViewModel.getIsUpdated().postValue(Boolean.TRUE);

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
                //gameViewModel.getIsUpdated().setValue(Boolean.FALSE);
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //TODO control FPS
        }
    }
}
