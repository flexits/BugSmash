package com.flexits.bugsmash;

import android.graphics.Point;

//a moving entity class
public class Mob {
    private Point coord;
    private int vector;
    private boolean isAlive;
    private MobSpecies species;

    public Mob(Point coordinates, int vectorAngle, boolean isAlive, MobSpecies species) {
        if (coordinates == null) coord = new Point(0, 0);
        else coord = coordinates;
        this.vector = vectorAngle;
        this.isAlive = isAlive;
        this.species = species;
    }

    public Mob(int x, int y, int vectorAngle, boolean isAlive, MobSpecies species){
        this(new Point(x, y), vectorAngle, isAlive, species);
    }

    public Point getCoord() {
        return coord;
    }

    public void setCoord(Point coord) {
        this.coord = coord;
    }

    public int getVectAngle() {
        return vector;
    }

    public void setVectAngle(int vector) {
        this.vector = vector;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public MobSpecies getSpecies() {
        return species;
    }

    public void setSpecies(MobSpecies species) {
        this.species = species;
    }
}
