package com.flexits.bugsmash;

import android.graphics.Point;
import android.graphics.PointF;
import android.os.CountDownTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//all calculations of the game world are made here and saved into the LiveData object
public class GameLoopThread extends Thread implements Runnable{
    private final int THREAD_ITERATIONS_INTERVAL = 50;  //pause between thread iterations, ms
    private final float MOBS_VELOCITY = 7f;             //px per cycle
    private final int VECTOR_JITTER = 4;                //degrees
    private final int AVOID_COLLISION_ATTEMPTS = 12;    //limit the number of attempts to avoid collision
    private final int COLLISION_TURN_ANGLE = 15;        //when avoiding a collision, mob turns the given angle clockwise
    private final int CNTDOWN_INTERVAL = 1000;          //timer countdown interval

    private boolean isRunning = false;                  //thread control variable
    private int score;                                  //hit adds a point, miss subtracts a point until 0
    private final ArrayList<PointF> touchCoords = new ArrayList<>(); //touch coordinates to perform hit-test

    private final GameViewModel gameViewModel;          //observable data storage
    private final Point displaySize;                    //current device display size
    private final CountDownTimer ctimer;                //timer to limit a game round duration

    public GameLoopThread(GameViewModel gameViewModel, Point displaySize, long timerval, int score) {
        this.gameViewModel = gameViewModel;
        this.displaySize = displaySize;
        this.score = score;
        //init a timer to limit the game round time
        ctimer = new CountDownTimer(timerval, CNTDOWN_INTERVAL) {
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

    //check if a clicked point is inside of any mob
    public void hitTest(PointF coord){
        if (coord == null) return;
        touchCoords.add(coord);
    }

    @Override
    public void run() {
        //start timer and update mobs in an endless cycle
        gameViewModel.getIsOver().postValue(false);
        ctimer.start();
        while (isRunning) {
            List<Mob> mobs = gameViewModel.getMobs().getValue();
            for(Mob m : mobs){
                //if (m.isKilled()) continue;
                if (m.isKilled() && !m.isDying()) {
                    //toss a coin to decide if the mob will be revived
                    if (RandGenerator.tosscoin()){
                        m.Revive();
                    }
                }
                float x = m.getCoord().x;
                float y = m.getCoord().y;
                PointF mob_dimensions = new PointF(
                        m.getSpecies().getBmp().getWidth(),
                        m.getSpecies().getBmp().getHeight());
                //perform hit test
                boolean isHit = false;
                for(PointF coord : touchCoords){
                    isHit = (coord.x >= x) && (coord.x <= mob_dimensions.x + x)
                            && (coord.y >= y) && (coord.y <= mob_dimensions.y + y);
                    if (isHit){
                        //a mob is killed, remove the point
                        touchCoords.remove(coord);
                        break;
                    }
                }
                if (isHit) {
                    //kill the mob and skip cycle - no need to move a dead mob
                    m.Kill();
                    SoundManager.playCrunch();
                    score++;
                    continue;
                }
                //mob movement
                int hash = m.hashCode();
                int angle = m.getVectAngle();
                //add some trajectory jitter
                angle += (RandGenerator.generate(0, VECTOR_JITTER) - VECTOR_JITTER /2);
                //change new position avoiding collisions
                Mob collidedMob = null; //the object a collision had happened with
                for(int attempts = AVOID_COLLISION_ATTEMPTS; attempts >= 0; attempts--){
                    //calculate new coordinates
                    PointF mob_start = new PointF(
                            x + (float)(MOBS_VELOCITY * Math.sin(Math.toRadians(angle))),
                            y + (float)(-1 * MOBS_VELOCITY * Math.cos(Math.toRadians(angle))));
                    //perform collision test with screen boundaries
                    boolean collision = (mob_start.x < 0)
                            || (mob_start.x + mob_dimensions.x > displaySize.x)
                            || (mob_start.y < 0)
                            || (mob_start.y + mob_dimensions.y > displaySize.y);
                    //perform collision test with a previously remembered object, if any
                    if (!collision && (collidedMob != null)) {
                        //collision = checkCollision(collidedMob, mob_start, mob_end);
                        collision = collidedMob.checkOverlap(mob_start, mob_dimensions);
                    }
                    //perform collision test with other objects
                    if (!collision) {
                        for (Mob mb : mobs) {
                            if (mb.isKilled()) continue;            //avoid compare to dead mobs
                            if (mb.hashCode() == hash) continue;    //avoid compare to itself
                            collision = mb.checkOverlap(mob_start, mob_dimensions);
                            if (collision){
                                collidedMob = mb;   //rememeber the collided mob to check in the next iteration
                                break;
                            }
                        }
                    }
                    //if collision detected, turn clockwise and retry test
                    if (collision) {
                        angle += COLLISION_TURN_ANGLE;
                        continue;
                    }
                    //collision avoided
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
                TimeUnit.MILLISECONDS.sleep(THREAD_ITERATIONS_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //update score
            gameViewModel.getScore().postValue(score);
            //indicate a data update
            gameViewModel.getIsUpdated().postValue(Boolean.TRUE);
        }
        //stop timer
        if (ctimer != null) ctimer.cancel();
    }
}
