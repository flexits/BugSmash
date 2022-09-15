package com.flexits.bugsmash;

import android.graphics.Point;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

//all calculations of the game world are made here and saved into the LiveData object
public class GameLoopThread extends Thread implements Runnable{
    private final float MOBS_VELOCITY = 1.0f; //px per cycle

    private final GameViewModel gameViewModel;

    private boolean isRunning = false;

    public GameLoopThread(GameViewModel gameViewModel) {
        this.gameViewModel = gameViewModel;
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
                //add some trajectory jitter
                angle += (generateRnd(0, 10) - 5);
                //calculate new coordinates
                //float new_x_start = x + (float)(MOBS_VELOCITY * Math.cos(Math.toRadians(angle)));
                //float new_y_start = y + (float)(-1 * MOBS_VELOCITY * Math.sin(Math.toRadians(angle)));
                float new_x_start = x + (float)(MOBS_VELOCITY * Math.sin(Math.toRadians(angle)));
                float new_y_start = y + (float)(-1 * MOBS_VELOCITY * Math.cos(Math.toRadians(angle)));
                //perform collision test
                float new_width = m.getSpecies().getBmp().getWidth();
                float new_x_end = new_x_start + new_width;
                float new_height = m.getSpecies().getBmp().getHeight();
                float new_y_end = new_y_start + new_height;
                boolean x_collision = false;
                boolean y_collision = false;
                for (Mob mb : mobs){
                    if (mb.hashCode() == hash) continue;
                    float mob_x_start = mb.getCoord().x;
                    int mob_width = mb.getSpecies().getBmp().getWidth();
                    float mob_x_end = mob_x_start + mob_width;
                    if (new_x_start > mob_x_end || new_x_end < mob_x_start) continue;
                    x_collision = true;
                    float mob_y_start = mb.getCoord().y;
                    float mob_height = mb.getSpecies().getBmp().getHeight();
                    float mob_y_end = mob_y_start + mob_height;
                    if (new_y_start > mob_y_end || new_y_end < mob_y_start) continue;
                    y_collision = true;
                    //detect the axis of collision, make a clockwise turn, continue moving along the axis
                    float x_projection_start = Math.min(Math.min(new_x_start, mob_x_start), Math.min(new_x_end, mob_x_end));
                    float x_projection_end = Math.max(Math.max(new_x_end, mob_x_end), Math.max(new_x_start, mob_x_start));
                    float x_projection_diff = (new_width + mob_width) - (x_projection_end - x_projection_start);
                    float y_projection_start = Math.min(Math.min(new_y_start, mob_y_start), Math.min(new_y_end, mob_y_end));
                    float y_projection_end = Math.max(Math.max(new_y_start, mob_y_start), Math.max(new_y_end, mob_y_end));
                    float y_projection_diff = (new_height + mob_height) - (y_projection_end - y_projection_start);
                    if (y_projection_diff > x_projection_diff){
                        //TODO angles are wrong now
                        //collision on the Y axis
                        if (Math.sin(Math.toRadians(angle)) > 0) {
                            //moving down
                            new_y_start = y + MOBS_VELOCITY;
                        } else{
                            //moving up
                            new_y_start = y - MOBS_VELOCITY;
                        }
                        new_x_start = x;
                    } else{
                        //collision on the X axis
                        if (Math.cos(Math.toRadians(angle)) > 0){
                            //moving right
                            new_x_start = x + MOBS_VELOCITY;
                        } else{
                            //moving left
                            new_x_start = x - MOBS_VELOCITY;
                        }
                        new_y_start = y;
                    }
                    break;
                }
                //if (x_collision && y_collision) continue;
                m.setCoord(new PointF(new_x_start, new_y_start));
                m.setVectAngle(angle);
                //TODO screen boundaries
                //TODO mob collisions
                //TODO store collided object's hash to check firstly
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

    private int generateRnd(int min, int max){
        if (min>=max) throw new IllegalArgumentException();
        if (min+1 == max) return min;
        return min + (int)(Math.random() * ((max - min) + 1));
    }
}
