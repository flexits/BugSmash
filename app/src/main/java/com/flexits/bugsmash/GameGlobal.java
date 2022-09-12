package com.flexits.bugsmash;

import android.app.Application;

import java.util.ArrayList;

public class GameGlobal extends Application {
    private ArrayList<Mob> mobs;

    public ArrayList<Mob> getMobs() {
<<<<<<< HEAD
        if (mobs == null) mobs = new ArrayList<>();
=======
        if (mobs == null) mobs = new ArrayList<>();;
>>>>>>> temp
        return mobs;
    }

    public void setMobs(ArrayList<Mob> mobs) {
        this.mobs = mobs;
    }
}
