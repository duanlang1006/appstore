package com.applite.dm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
        paint.setAntiAlias(true);// 设置是否抗锯齿
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);// 帮助消除锯齿
        paint.setColor(Color.GRAY);// 设置画笔灰色
        paint.setStrokeWidth(2);// 设置画笔宽度
        paint.setStyle(Paint.Style.STROKE);// 设置中空的样式
        canvas.drawCircle(40, 40, 30, paint);// 在中心为（60,60）的地方画个半径为45的圆，宽度为setStrokeWidth：2
        paint.setStrokeWidth(4);// 设置画笔宽度
        paint.setColor(Color.GREEN);// 设置画笔为绿色
        oval.set(9, 9, 71, 71);
        canvas.drawArc(oval, -90, ((float) progress / max) * 360, false, paint);// 画圆弧，第二个参数为：起始角度，第三个为跨的角度，第四个为true的时候是实心，false的时候为空心
    }

}
