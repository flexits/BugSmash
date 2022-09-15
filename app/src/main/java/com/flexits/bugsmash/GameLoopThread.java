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
    private final int COLLISION_TURN_ANGLE = 30;

    private final GameViewModel gameViewModel;
    private final Point displaySize;

    private boolean isRunning = false;

    public GameLoopThread(GameViewModel gameViewModel, Point displaySize) {
        this.gameViewModel = gameViewModel;
        this.displaySize = displaySize;
    }

    //set flag to start or stop the thread
    public void allowExecution(boolean isAllowed) {
        this.isRunning = isAllowed;
    }

    @Override
    public void run() {
        while (isRunning) {
            List<Mob> mobs = gameViewModel.getMobs().getValue();
            for(Mob m : mobs){
                if (!m.isAlive()) continue;
                int hash = m.hashCode();
                float x = m.getCoord().x;
                float y = m.getCoord().y;
                int angle = m.getVectAngle();
                int m_width = m.getSpecies().getBmp().getWidth();
                int m_height = m.getSpecies().getBmp().getHeight();
                //add some trajectory jitter
                angle += (generateRnd(0, TRAJECTORY_JITTER) - TRAJECTORY_JITTER/2);
                //change new position avoiding collisions
                for(int attempts = AVOID_COLLISION_ATTEMPTS; attempts >= 0; attempts--){
                    //calculate new coordinates
                    float new_x_start = x + (float)(MOBS_VELOCITY * Math.sin(Math.toRadians(angle)));
                    float new_y_start = y + (float)(-1 * MOBS_VELOCITY * Math.cos(Math.toRadians(angle)));
                    float new_x_end = new_x_start + m_width;
                    float new_y_end = new_y_start + m_height;
                    //perform collision test with screen boundaries
                    boolean collision = (new_x_start < 0)
                            || (new_x_end > displaySize.x)
                            || (new_y_start < 0)
                            || (new_y_end > displaySize.y);
                    if (!collision) {
                        //perform collision test with other objects
                        for (Mob mb : mobs) {
                            if (mb.hashCode() == hash) continue;    //do not compare with itself
                            float mob_x_start = mb.getCoord().x;
                            float mob_x_end = mob_x_start + mb.getSpecies().getBmp().getWidth();
                            if (new_x_start > mob_x_end || new_x_end < mob_x_start)
                                continue;   //no coincidence on the X axis, go to another object
                            float mob_y_start = mb.getCoord().y;
                            float mob_y_end = mob_y_start + mb.getSpecies().getBmp().getHeight();
                            if (new_y_start > mob_y_end || new_y_end < mob_y_start)
                                continue;   //no coincidence on the Y axis, go to another object
                            collision = true; //collision detected, finish comparison
                            break;
                            //TODO store collided object hash to check firstly after turn
                        }
                    }
                    //if collision detected, turn clockwise and retry test
                    if (collision) {
                        angle += COLLISION_TURN_ANGLE;
                        continue;
                    }
                    m.setCoord(new PointF(new_x_start, new_y_start));
                    m.setVectAngle(angle);
                    break;
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            gameViewModel.getIsUpdated().postValue(Boolean.TRUE);
        }
    }

    private int generateRnd(int min, int max){
        if (min>=max) throw new IllegalArgumentException();
        if (min+1 == max) return min;
        return min + (int)(Math.random() * ((max - min) + 1));
    }
}
