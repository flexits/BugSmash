package com.flexits.bugsmash;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class GameView extends SurfaceView {
    private final GameViewModel gameViewModel;

    public GameView(Context context, GameViewModel gameViewModel) {
        super(context);
        this.gameViewModel = gameViewModel;
        SurfaceHolder holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) { performDraw(); }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) { }
        });
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.YELLOW);
        MutableLiveData<List<Mob>> mobs =  gameViewModel.getMobs();
        if (mobs == null) return;
        for(Mob m : mobs.getValue()){
            if (!m.isAlive()) continue;
            canvas.drawBitmap(m.getSpecies().getBmp(), m.getX_coord(),m.getY_coord(),null);
        }
    }

    //this method will be called explicitly to refresh the screen content
    public void performDraw() {
        Canvas canvas = null;
        SurfaceHolder sfhold = this.getHolder();
        if (!sfhold.getSurface().isValid()) return;
        try {
            //try to lock the resource to avoid conflicts
            canvas = sfhold.lockCanvas();
            synchronized (sfhold) {
                //update the screen upon lock acquisition
                if (canvas != null) {
                    this.draw(canvas);
                }
            }
        } finally {
            //unlock the resource if locked
            if (canvas != null) {
                sfhold.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        return true;
    }


}