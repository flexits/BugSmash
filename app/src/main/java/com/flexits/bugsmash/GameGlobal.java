package com.flexits.bugsmash;

import android.app.Application;

import java.util.ArrayList;

public class GameGlobal extends Application {
    private ArrayList<Mob> mobs;

    public ArrayList<Mob> getMobs() {
        if (mobs == null) mobs = new ArrayList<>();
        return mobs;
    }

    public void setMobs(ArrayList<Mob> mobs) {
        this.mobs = mobs;
    }
}
