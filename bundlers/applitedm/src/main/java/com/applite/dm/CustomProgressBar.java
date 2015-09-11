package com.applite.dm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by wanghaochen on 15-8-25.
 */
public class CustomProgressBar extends ImageButton {
    private int progress = 0;
    private int max = 100;
    private Paint paint;
    private RectF oval;
    private Bitmap background = BitmapFactory.decodeResource(getResources(),
            R.drawable.download_progress_background);
    private Bitmap mask = BitmapFactory.decodeResource(getResources(),
            R.drawable.download_progress_running);
    private PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        oval = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x = Math.max(0,(getWidth() - background.getWidth())/2);
        int y = Math.max(0,(getHeight() - background.getHeight())/2);

        paint.setAntiAlias(true);// 设置是否抗锯齿
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);// 帮助消除锯齿
        canvas.drawBitmap(background, x, y, paint);  //画背景
        paint.setFilterBitmap(false);

        int sc = canvas.saveLayer(x, y, x + background.getWidth(), y + background.getHeight(),
                null,Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
                        | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                        | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                        | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
        oval.set(x, y, x + background.getWidth(), background.getHeight());
        paint.setStrokeWidth(2);// 设置画笔宽度
        canvas.drawArc(oval, -90, ((float) progress / max) * 360, true, paint);
        paint.setXfermode(mode);
        canvas.drawBitmap(mask, x, y, paint);
        paint.setXfermode(null);
        canvas.restoreToCount(sc);
    }

}