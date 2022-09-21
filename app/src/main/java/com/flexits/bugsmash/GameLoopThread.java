package com.flexits.bugsmash;

import android.graphics.Point;
import android.graphics.PointF;
import android.os.CountDownTimer;

import java.util.ArrayList;
import java.util.List;

//all calculations of the game world are made here and saved into the LiveData object
public class GameLoopThread extends Thread implements Runnable{
    private final float MOBS_VELOCITY = 7f; //px per cycle
    private final int VECTOR_JITTER = 4;    //degrees
    private final int AVOID_COLLISION_ATTEMPTS = 12;//limit the number of attempts to avoid collision
    private final int COLLISION_TURN_ANGLE = 15;    //when avoiding a collision, mob turns the given angle clockwise

    private boolean isRunning = false;  //thread control variable
    private int score = 0;              //hit adds a point, miss subtracts a point until 0
    private final ArrayList<PointF> touchCoords = new ArrayList<>(); //touch coordinates to perform hit-test

    private final GameViewModel gameViewModel;
    private final Point displaySize;
    private final CountDownTimer ctimer;
    //TODO load score and timer remaining value for implementing a pause game

    public GameLoopThread(GameViewModel gameViewModel, Point displaySize) {
        this.gameViewModel = gameViewModel;
        this.displaySize = displaySize;
        //init a timer to limit the game round time
        ctimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long l) {
                gameViewModel.getTimeRemaining().postValue(l);  //update time left value
            }

            @Override
            public void onFinish() {
                //stop the thread and set the time over flag
                isRunning = false;
                gameViewModel.getIsOver().postValue(true);
            }
        };
    }

    //set flag to start or stop the thread
    public void allowExecution(boolean isAllowed) {
        this.isRunning = isAllowed;
    }

    //
    public void hitTest(PointF coord){
        if (coord == null) return;
        touchCoords.add(coord);
    }

    @Override
    public void run() {
        //TODO revive mobs if there are too little of them
        gameViewModel.getIsOver().postValue(false);
        ctimer.start();
        while (isRunning) {
            List<Mob> mobs = gameViewModel.getMobs().getValue();
            for(Mob m : mobs){
                if (m.isKilled()) continue;
                float x = m.getCoord().x;
                float y = m.getCoord().y;
                int m_width = m.getSpecies().getBmp().getWidth();
                int m_height = m.getSpecies().getBmp().getHeight();
                //perform hit test
                boolean isHit = false;
                for(PointF coord : touchCoords){
                    isHit = (coord.x >= x) && (coord.x <= m_width + x)
                            && (coord.y >= y) && (coord.y <= m_height + y);
                    if (isHit){
                        touchCoords.remove(coord);
                        break;
                    }
                }
                if (isHit) {
                    m.Kill();
                    SoundManager.playCrunch();
                    score++;
                    continue;
                }
                int hash = m.hashCode();
                int angle = m.getVectAngle();
                //add some trajectory jitter
                angle += (generateRnd(0, VECTOR_JITTER) - VECTOR_JITTER /2);
                //change new position avoiding collisions
                Mob collidedMob = null; //the object a collision had happened with
                for(int attempts = AVOID_COLLISION_ATTEMPTS; attempts >= 0; attempts--){
                    //calculate new coordinates
                    PointF mob_start = new PointF(
                            x + (float)(MOBS_VELOCITY * Math.sin(Math.toRadians(angle))),
                            y + (float)(-1 * MOBS_VELOCITY * Math.cos(Math.toRadians(angle))));
                    PointF mob_end = new PointF(mob_start.x + m_width, mob_start.y + m_height);
                    //perform collision test with screen boundaries
                    boolean collision = (mob_start.x < 0)
                            || (mob_end.x > displaySize.x)
                            || (mob_start.y < 0)
                            || (mob_end.y > displaySize.y);
                    //perform collision test with a previously remembered object, if any
                    if (!collision && (collidedMob != null)) {
                        collision = checkCollision(collidedMob, mob_start, mob_end);
                    }
                    //perform collision test with other objects
                    if (!collision) {
                        for (Mob mb : mobs) {
                            if (mb.hashCode() == hash) continue;    //avoid compare to itself
                            collision = checkCollision(mb, mob_start, mob_end);
                            if (collision){
                                collidedMob = mb;
                                break;
                            }
                        }
                    }
                    //if collision detected, turn clockwise and retry test
                    if (collision) {
                        angle += COLLISION_TURN_ANGLE;
                        continue;
                    }
                    m.setCoord(mob_start);
                    m.setVectAngle(angle);
                    break;
                }
            }

            //all remaining touch events are misses, clear them
            int misses = touchCoords.size();
            if (misses > 0){
                touchCoords.clear();
                SoundManager.playErrAlert();
                score -= misses;
                if (score < 0) score = 0;
            }

            //sleep to limit FPS and system load
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameViewModel.getScore().postValue(score);
            //indicate a data update
            gameViewModel.getIsUpdated().postValue(Boolean.TRUE);
        }
        //stop timer
        if (ctimer != null) ctimer.cancel();
    }

    private int generateRnd(int min, int max){
        if (min >= max) throw new IllegalArgumentException();
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    private boolean checkCollision(Mob mb, PointF start, PointF end){
        //TODO move this into a method in mob class
        float mob_x_start = mb.getCoord().x;
        float mob_x_end = mob_x_start + mb.getSpecies().getBmp().getWidth();
        if (start.x > mob_x_end || end.x < mob_x_start) return false;
        float mob_y_start = mb.getCoord().y;
        float mob_y_end = mob_y_start + mb.getSpecies().getBmp().getHeight();
        if (start.y > mob_y_end || end.y < mob_y_start) return false;
        return true;
    }
}
