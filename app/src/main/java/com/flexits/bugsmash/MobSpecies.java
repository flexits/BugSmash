package com.flexits.bugsmash;

import android.graphics.Bitmap;

//description of a mob species
public class MobSpecies {
    private Bitmap bmp;

    public MobSpecies(Bitmap bmp) {
        this.bmp = bmp;
    }

    public Bitmap getBmp() {
        return bmp;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }
}
