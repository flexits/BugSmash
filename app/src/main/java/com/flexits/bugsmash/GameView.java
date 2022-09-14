package com.flexits.bugsmash;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class GameView extends SurfaceView {
    private DrawThread drawThread;

    public GameView(Context context, GameViewModel gameViewModel) {
        super(context);
        SurfaceHolder holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //allow surface redraw
                setWillNotDraw(false);
                //create a drawing thread and start it
                drawThread = new DrawThread(holder, gameViewModel);
                drawThread.allowExecution(true);
                drawThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //finish drawing
                drawThread.allowExecution(false);
                //wait for the thread to terminate
                boolean retry = true;
                while (retry) {
                    try {
                        drawThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace(); }
                }
            }
        });
    }
}

class DrawThread extends Thread{
    private final SurfaceHolder holder;
    private final GameViewModel gameViewModel;

    private boolean isRunning = false;

    public DrawThread(SurfaceHolder holder, GameViewModel gameViewModel){
        this.holder = holder;
        this.gameViewModel = gameViewModel;
    }

    public void allowExecution(boolean isAllowed) {
        this.isRunning = isAllowed;
    }

    @Override
    public void run() {
        while (isRunning) {
            //obtain canvas and perform drawing

            if (!holder.getSurface().isValid()) return;
            Canvas canvas = null;
            try {
                //try to lock the resource to avoid conflicts
                canvas = holder.lockCanvas();
                synchronized (holder) {
                    //update the screen upon lock acquisition
                    if (canvas != null) {
                        //draw the game objects
                        performDraw(canvas);
                    }
                }
            } finally {
                //unlock the resource if locked
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void performDraw(Canvas canvas){
        canvas.drawColor(Color.YELLOW);
        MutableLiveData<List<Mob>> mobs =  gameViewModel.getMobs();
        if (mobs == null) return;
        for(Mob m : mobs.getValue()){
            if (!m.isAlive()) continue;
            //canvas.drawBitmap(m.getSpecies().getBmp(), m.getCoord().x, m.getCoord().y,null);
            Bitmap bmp = m.getSpecies().getBmp();
            Matrix matrix = new Matrix();
            matrix.postRotate(270, bmp.getWidth()/2, bmp.getHeight()/2);
            matrix.postTranslate(m.getCoord().x, m.getCoord().y);
            canvas.drawBitmap(bmp, matrix, null);
        }
    }
}