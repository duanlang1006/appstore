package com.mit.applite.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.osgi.extra.OSGIServiceHost;

/**
 * Created by LSY on 15-5-22.
 */
public class DetailUtils {

    private static final String TAG = "DetailUtils";

    /**
     * 图片旋转
     *
     * @param bm
     * @param degree
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bm, float degree) {
        int X, Y;
        if (bm.getWidth() > bm.getHeight()) {
            X = bm.getHeight();
            Y = bm.getWidth();
        } else {
            X = bm.getWidth();
            Y = bm.getHeight();
        }
        Matrix m = new Matrix();
        m.setRotate(degree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        if ((int) degree == 90) {
            float targetX, targetY = 0;
            if (degree == 90) {
                targetX = bm.getHeight();
                targetY = 0;
            }
            final float[] values = new float[9];
            m.getValues(values);
            float x1 = values[Matrix.MTRANS_X];
            float y1 = values[Matrix.MTRANS_Y];
            m.postTranslate(X - x1, targetY - y1);
        }
        Bitmap bm1 = Bitmap.createBitmap(X, Y, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);
        bm.recycle();
        return bm1;
    }

//    /****
//     * 搜索
//     */
//    public static void launchSearchFragment(OSGIServiceHost host) {
//        if (null != host){
//            host.jumpto(Constant.OSGI_SERVICE_SEARCH_FRAGMENT,null,null,true);
//        }
//    }
}
