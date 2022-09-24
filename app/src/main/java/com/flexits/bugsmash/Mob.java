package com.flexits.bugsmash;

import android.graphics.Bitmap;
import android.graphics.PointF;

//a moving entity class

//a mob lifecycle:
// alive (isAlive==true & isDying==false)
// -> dying (isAlive==false & isDying==true)
// -> dead (isAlive==false & isDying==false)

public class Mob {
    private final int DYING_DURATION = 5;

    private PointF coord;
    private int vector;
    private boolean isAlive;
    private boolean isDying;
    private int lifeRemainder;
    private final MobSpecies species;

    public Mob(PointF coordinates, int vectorAngle, boolean isAlive, MobSpecies species) {
        if (coordinates == null) coord = new PointF(0, 0);
        else coord = coordinates;
        this.vector = vectorAngle;
        this.isAlive = isAlive;
        this.species = species;
        isDying = false;
        lifeRemainder = -1;
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

    public MobSpecies getSpecies() {
        return species;
    }

    public boolean isKilled() {
        return !isAlive;
    }

    public boolean isDying() {
        return isDying;
    }

    public void Kill() {
        isAlive = false;
        isDying = true;
        lifeRemainder = DYING_DURATION;
    }

    public void Revive(){
        isAlive = true;
        isDying = false;
        lifeRemainder = -1;
    }

    public void DecreaseLife() {
        if (--lifeRemainder < 0) isDying = false;
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
