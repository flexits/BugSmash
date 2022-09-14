package com.flexits.bugsmash;

import android.graphics.Point;

//all calculations of the game world are made here and saved into the LiveData object
public class GameLoopThread extends Thread implements Runnable{
    private final int MOBS_VELOCITY = 1; //px per cycle

    private final GameViewModel gameViewModel;
    private final GameView gameView;

    private boolean isRunning = false;

    public GameLoopThread(GameView gameView, GameViewModel gameViewModel) {
        this.gameView = gameView;
        this.gameViewModel = gameViewModel;
    }

    //set flag to start or stop the thread
    public void allowExecution(boolean isAllowed) {
        this.isRunning = isAllowed;
    }

    @Override
    public void run() {
        while (isRunning) {
            for(Mob m : gameViewModel.getMobs().getValue()){
                if (!m.isAlive()) continue;
                int x = m.getCoord().x;
                int y = m.getCoord().y;
                int angle = m.getVectAngle();
                x += (int)(MOBS_VELOCITY * Math.cos(Math.toRadians(angle)));
                y += (int)(-1 * MOBS_VELOCITY * Math.sin(Math.toRadians(angle)));
                m.setCoord(new Point(x, y));
                //TODO screen boundaries
                //TODO mob collisions
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            gameViewModel.getIsUpdated().postValue(Boolean.TRUE);
            //TODO control FPS
        }
    }
}
