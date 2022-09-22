package com.flexits.bugsmash;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

//Provides static methods to play predefined sounds in background.
//Must be instantiated before usage and released to dispose of.
public class SoundManager {
    final static int SOUND_STREAMS_MAX = 3;

    private static SoundPool soundPool;
    private static int crunchId = 0;
    private static int erralertId = 0;

    private static SoundManager instance;

    private SoundManager(Context context){
        soundPool = new SoundPool.Builder()
                .setMaxStreams(SOUND_STREAMS_MAX)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .build())
                .build();
        crunchId = soundPool.load(context, R.raw.crunch, 0);
        erralertId = soundPool.load(context, R.raw.erralert, 0);
    }

    public static void instantiate(Context context) {
        if(instance == null) instance = new SoundManager(context);
    }

    public static void release(){
        if (soundPool == null) return;
        soundPool.release();
        soundPool = null;
        instance = null;
    }

    private static void play(int id){
        if (instance == null) return;
        if (soundPool == null) return;
        if (id < 0) return;
        soundPool.play(id, 1, 1, 0, 0,1);
    }

    public static void playCrunch(){
        play(crunchId);
    }

    public static void playErrAlert(){
        play(erralertId);
    }
}
