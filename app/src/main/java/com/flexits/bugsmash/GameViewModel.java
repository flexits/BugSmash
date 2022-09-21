package com.flexits.bugsmash;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

//ViewModel stores the UI-content, updatable by the game loop

public class GameViewModel extends ViewModel {
    private MutableLiveData<List<Mob>>mobs;     //list of mobs
    private MutableLiveData<Boolean>isUpdated;  //the flag is set upon the list update and
                                                //is reset upon the UI update
    private MutableLiveData<Long> timeRemaining;
    private MutableLiveData<Integer> score;
    private MutableLiveData<Boolean>isOver;

    public MutableLiveData<Boolean> getIsUpdated() {
        if (isUpdated == null) isUpdated = new MutableLiveData<>(Boolean.FALSE);
        return isUpdated;
    }

    public MutableLiveData<List<Mob>> getMobs() {
        if (mobs == null) mobs = new MutableLiveData<>(new ArrayList<>());
        return mobs;
    }

    public MutableLiveData<Long> getTimeRemaining() {
        if (timeRemaining == null) timeRemaining = new MutableLiveData<>(0l);
        return timeRemaining;
    }

    public MutableLiveData<Integer> getScore() {
        if (score == null) score = new MutableLiveData<>(0);
        return score;
    }

    public MutableLiveData<Boolean> getIsOver() {
        if (isOver == null) isOver = new MutableLiveData<>(Boolean.FALSE);
        return isOver;
    }
}
