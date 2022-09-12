package com.flexits.bugsmash;

//a moving entity class
public class Mob {
    private int x_coord;
    private int y_coord;
    private int vector;
    private boolean isAlive;
    private MobSpecies species;

    public Mob(int x_coord, int y_coord, int vector, boolean isAlive, MobSpecies species) {
        this.x_coord = x_coord;
        this.y_coord = y_coord;
        this.vector = vector;
        this.isAlive = isAlive;
        this.species = species;
    }

    public int getX_coord() {
        return x_coord;
    }

    public void setX_coord(int x_coord) {
        this.x_coord = x_coord;
    }

    public int getY_coord() {
        return y_coord;
    }

    public void setY_coord(int y_coord) {
        this.y_coord = y_coord;
    }

    public int getVector() {
        return vector;
    }

    public void setVector(int vector) {
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
