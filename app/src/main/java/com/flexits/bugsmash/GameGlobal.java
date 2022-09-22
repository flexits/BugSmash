package com.flexits.bugsmash;

import android.app.Application;

import java.util.ArrayList;

//Global game parameters storage
public class GameGlobal extends Application {
    private ArrayList<Mob> mobs;
    private long timerval;
    private int score;

    public ArrayList<Mob> getMobs() {
        if (mobs == null) mobs = new ArrayList<>();
        return mobs;
    }

    public void setMobs(ArrayList<Mob> mobs) {
        this.mobs = mobs;
    }

    public long getTimerval() {
        return timerval;
    }

    public void setTimerval(long timerval) {
        this.timerval = timerval;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
