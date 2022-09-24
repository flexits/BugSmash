package com.flexits.bugsmash;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
    private final String sgameOver;

    private boolean isRunning = false;

    public DrawThread(SurfaceHolder holder, GameViewModel gameViewModel, Resources resources){
        this.holder = holder;
        this.gameViewModel = gameViewModel;
        mobDyingSprite = BitmapFactory.decodeResource(resources, R.drawable.blood_50px);
        sgameOver = resources.getString(R.string.gameover_title);
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
        //paint used for OSD text (time and score)
        Paint osdPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        osdPaint.setColor(Color.BLACK);
        osdPaint.setTextSize(52);
        osdPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        //paint used for final title of the game
        Paint fintitlPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fintitlPaint.setColor(Color.RED);
        fintitlPaint.setTextSize(56);
        fintitlPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        fintitlPaint.setTextAlign(Paint.Align.CENTER);

        if (isOver) {
            //game over
            canvas.drawText(sgameOver, canvas.getWidth()/2, canvas.getHeight()/2, fintitlPaint);
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
                matrix.postRotate(m.getVectAngle(), bmp.getWidth() / 2f, bmp.getHeight() / 2f);
                matrix.postTranslate(m.getCoord().x, m.getCoord().y);
                canvas.drawBitmap(bmp, matrix, null);
            }
            long timr = gameViewModel.getTimeRemaining().getValue();
            String time = String.format(Locale.getDefault(), "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes( timr),
                    TimeUnit.MILLISECONDS.toSeconds(timr) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timr)));
            String score = String.valueOf(gameViewModel.getScore().getValue());
            //draw OSD time and score
            canvas.drawText(time, 100, 100, osdPaint);
            canvas.drawText(score, 800, 100, osdPaint);
        }
    }
}