package com.flexits.bugsmash;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView {
    private final GameViewModel gameViewModel;
    private final GameGlobal gameGlobal;

    public GameView(Context context, GameViewModel gameViewModel) {
        super(context);
        gameGlobal = (GameGlobal) context.getApplicationContext();
        this.gameViewModel = gameViewModel;
        /*SurfaceHolder holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) { }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) { }
        });*/
    }

    //this method will be called explicitly to refresh the screen content
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.YELLOW);

        /*for(Mob m : gameViewModel.getMobs().getValue()){
            if (!m.isAlive()) continue;
            canvas.drawBitmap(m.getSpecies().getBmp(), m.getX_coord(),m.getY_coord(),null);
        }*/

        for(Mob m : gameGlobal.getMobs()){
            if (!m.isAlive()) continue;
            canvas.drawBitmap(m.getSpecies().getBmp(), m.getX_coord(),m.getY_coord(),null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        return true;
    }


}