package com.flexits.bugsmash;

import android.graphics.Point;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

//all calculations of the game world are made here and saved into the LiveData object
public class GameLoopThread extends Thread implements Runnable{
    private final float MOBS_VELOCITY = 7f; //px per cycle
    private final int TRAJECTORY_JITTER = 4;
    private final int AVOID_COLLISION_ATTEMPTS = 12;
    private final int COLLISION_TURN_ANGLE = 15;

    private boolean isRunning = false;
    private final GameViewModel gameViewModel;
    private final Point displaySize;

    private final ArrayList<PointF> touchCoords = new ArrayList<>();

    public GameLoopThread(GameViewModel gameViewModel, Point displaySize) {
        this.gameViewModel = gameViewModel;
        this.displaySize = displaySize;
    }

    //set flag to start or stop the thread
    public void allowExecution(boolean isAllowed) {
        this.isRunning = isAllowed;
    }

    public void hitTest(PointF coord){
        if (coord == null) return;
        touchCoords.add(coord);
    }

    @Override
    public void run() {
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
                    continue;
                }
                int hash = m.hashCode();
                int angle = m.getVectAngle();
                //add some trajectory jitter
                angle += (generateRnd(0, TRAJECTORY_JITTER) - TRAJECTORY_JITTER/2);
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
            if (!touchCoords.isEmpty()){
                touchCoords.clear();
                SoundManager.playErrAlert();
            }

            //sleep to limit FPS and system load
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //indicate a data update
            gameViewModel.getIsUpdated().postValue(Boolean.TRUE);
        }
    }

    private int generateRnd(int min, int max){
        if (min >= max) throw new IllegalArgumentException();
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    private boolean checkCollision(Mob mb, PointF start, PointF end){
        float mob_x_start = mb.getCoord().x;
        float mob_x_end = mob_x_start + mb.getSpecies().getBmp().getWidth();
        if (start.x > mob_x_end || end.x < mob_x_start) return false;
        float mob_y_start = mb.getCoord().y;
        float mob_y_end = mob_y_start + mb.getSpecies().getBmp().getHeight();
        if (start.y > mob_y_end || end.y < mob_y_start) return false;
        return true;
    }
}
