package com.flexits.bugsmash;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class GameView extends SurfaceView {
    private DrawThread drawThread;

    public GameView(Context context, GameViewModel gameViewModel) {
        super(context);
        SurfaceHolder holder = getHolder();
        Resources resources = getResources();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //allow surface redraw
                setWillNotDraw(false);
                //create a drawing thread and start it
                drawThread = new DrawThread(holder, gameViewModel, resources);
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
    private final Bitmap mobDyingSprite;

    private boolean isRunning = false;

    public DrawThread(SurfaceHolder holder, GameViewModel gameViewModel, Resources resources){
        this.holder = holder;
        this.gameViewModel = gameViewModel;
        mobDyingSprite = BitmapFactory.decodeResource(resources, R.drawable.blood_50px);
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

    private void performDraw(Canvas canvas) {
        canvas.drawColor(Color.YELLOW);
        MutableLiveData<List<Mob>> mobs = gameViewModel.getMobs();

        Boolean isOver = gameViewModel.getIsOver().getValue();
        if (isOver) {
            //game over
            Paint p = new Paint();
            p.setColor(Color.BLUE);
            p.setStrokeWidth(20);
            p.setTextSize(48);
            //TODO gameover banner
            canvas.drawText("Game over!", 100, 100, p);
        } else {
            //game continues
            if (mobs == null) return;
            for (Mob m : mobs.getValue()) {
                //for each mob get its species picture if it's alive;
                //or get a blood stain picture if it's dying;
                //or skip the mob if it's already dead
                Bitmap bmp;
                if (m.isKilled()) {
                    if (m.isDying()){
                        bmp = mobDyingSprite;
                        m.DecreaseLife();
                    } else {
                        continue;
                    }
                } else {
                    bmp = m.getSpecies().getBmp();
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(m.getVectAngle(), bmp.getWidth() / 2, bmp.getHeight() / 2);
                matrix.postTranslate(m.getCoord().x, m.getCoord().y);
                canvas.drawBitmap(bmp, matrix, null);
            }
            String time = String.valueOf(gameViewModel.getTimeRemaining().getValue());
            String score = String.valueOf(gameViewModel.getScore().getValue());
            //TODO: convert to mm:ss
            Paint p = new Paint();
            p.setColor(Color.BLUE);
            p.setStrokeWidth(20);
            p.setTextSize(48);
            //TODO test position and style
            canvas.drawText(time, 100, 100, p);
            canvas.drawText(score, 300, 100, p);
        }
    }
}