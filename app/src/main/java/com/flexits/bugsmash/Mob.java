package com.flexits.bugsmash;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;

//a moving entity class
public class Mob {
    private PointF coord;
    private int vector;
    private boolean isAlive;
    private final MobSpecies species;

    public Mob(PointF coordinates, int vectorAngle, boolean isAlive, MobSpecies species) {
        if (coordinates == null) coord = new PointF(0, 0);
        else coord = coordinates;
        this.vector = vectorAngle;
        this.isAlive = isAlive;
        this.species = species;
    }

    public Mob(int x, int y, int vectorAngle, boolean isAlive, MobSpecies species){
        this(new PointF(x, y), vectorAngle, isAlive, species);
    }

    public PointF getCoord() {
        return coord;
    }

    public void setCoord(PointF coord) {
        this.coord = coord;
    }

    public int getVectAngle() {
        return vector;
    }

    public void setVectAngle(int angle) {
        if (angle > 360) angle -= 360;
        this.vector = angle;
    }

    public boolean isKilled() {
        return !isAlive;
    }

    public void Kill() { isAlive = false; }

    public void Revive() { isAlive = true; }

    public MobSpecies getSpecies() {
        return species;
    }

    //check if an object with given starting point and dimensions overlaps with the mob
    public boolean checkOverlap(PointF coordinates, PointF dimensions){
        float x = coordinates.x;
        float y = coordinates.y;
        float x_end = x + dimensions.x;
        float y_end = y + dimensions.y;

        Bitmap mob_bmp = species.getBmp();

        float mob_x_start = coord.x;
        float mob_x_end = mob_x_start + mob_bmp.getWidth();
        if (x > mob_x_end || x_end < mob_x_start) return false;

        float mob_y_start = coord.y;
        float mob_y_end = mob_y_start + mob_bmp.getHeight();
        if (y > mob_y_end || y_end < mob_y_start) return false;

        return true;
    }
}
