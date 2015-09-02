package com.mit.applite.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Bundle;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.osgi.extra.OSGIServiceHost;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
//        LogUtils.d(TAG, bm.getByteCount() + "");
//        bm = comp(bm, widthPixels, heightPixels);
//        LogUtils.d(TAG, bm.getByteCount() + "");
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
        paint.setAntiAlias(true);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.drawBitmap(bm, m, paint);
        if (!bm.equals(bm1)) {
            bm.recycle();
            LogUtils.d(TAG, "recycle");
        }
        return bm1;
    }

    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            LogUtils.d(TAG, "baos.toByteArray().length:" + baos.toByteArray().length);
            LogUtils.d(TAG, "图片压缩");
            LogUtils.d(TAG, "options:" + options);
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public static Bitmap comp(Bitmap image, float widthPixels, float heightPixels) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            LogUtils.d(TAG, "图片大于1M");
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        LogUtils.d(TAG, "outWidth:" + newOpts.outWidth + "-----outHeight:" + newOpts.outHeight);
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
//        float hh = 800f;//这里设置高度为800f
//        float ww = 480f;//这里设置宽度为480f
        float hh = heightPixels;
        float ww = widthPixels;
        LogUtils.d(TAG, "widthPixels:" + widthPixels + "-----heightPixels:" + heightPixels);
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        LogUtils.d(TAG, "be:" + be);
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
//        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
        return bitmap;
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
