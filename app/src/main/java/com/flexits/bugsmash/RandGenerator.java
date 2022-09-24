package com.flexits.bugsmash;

public class RandGenerator {

    //generate a random integer in a range
    public static int generate(int min, int max){
        if (min >= max) throw new IllegalArgumentException();
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    //toss coin unfair
    public static boolean tosscoin(){
        return (generate(0, 100000) < 10) ? true : false;
    }
}
