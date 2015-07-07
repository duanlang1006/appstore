package com.applite.theme;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import com.applite.common.R;

public class ThemeManager {
    private Context mContext;
    private PackageResProxy mresProxy = null;
    private Bitmap mIconMask = null;
    private Bitmap mIconFg = null;
    private Bitmap[] mIconBg;
    private int mIconWidth = -1;
    private int mIconHeight = -1;
    private ThemeModel model;
    private Resources mRes = null;
    private static ThemeManager mInstance = null; 

    public static ThemeManager getInstance(Context context){
        if (null == mInstance){
            mInstance = new ThemeManager(context);
        }
        return mInstance;
    }

    private ThemeManager(Context context){
        mContext = context;
        mRes = context.getResources();
        String packageName = context.getPackageName();
        try{
            mresProxy = new PackageResProxy(mRes, packageName);
        }catch(Exception e){
            e.printStackTrace();
        }
        mIconWidth = mIconHeight = (int) mRes.getDimension(R.dimen.app_icon_size);
        model = parseThemeFromXml(packageName);
        initIconBg();
        initIconMaskAndFg();
    }

    private ThemeModel parseThemeFromXml(String packageName){
        try{
            String THEME_XML = "theme.xml";
            InputStream is = mContext.getAssets().open(THEME_XML);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            ThemeModel theme = new ThemeModel();
            Element root = doc.getDocumentElement();
            theme.packageName = packageName;
            NodeList list = root.getChildNodes();
            int count = list.getLength();
            for (int i = 0; i < count; i++) {
                if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element node = (Element) list.item(i);
                    if ("iconmask".equals(node.getTagName())) {
                        NodeList folderList = node.getChildNodes();
                        int n = folderList.getLength();
                        for (int j = 0; j < n; j++) {
                                if (folderList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                Element item = (Element) folderList.item(j);
                                if ("theme_icon_mask".equals(item.getNodeName())) {
                                    theme.iconMask.mask = item.getFirstChild().getNodeValue();
                                } else if ("theme_icon_mask_scale_x".equals(item.getNodeName())) {
                                    theme.iconMask.x = Integer.parseInt(item.getFirstChild().getNodeValue());
                                } else if ("theme_icon_mask_scale_y".equals(item.getNodeName())) {
                                    theme.iconMask.y = Integer.parseInt(item.getFirstChild().getNodeValue());
                                } else if ("theme_icon_mask_scale_w".equals(item.getNodeName())) {
                                    theme.iconMask.w = Integer.parseInt(item.getFirstChild().getNodeValue());
                                } else if ("theme_icon_mask_scale_h".equals(item.getNodeName())) {
                                    theme.iconMask.h = Integer.parseInt(item.getFirstChild().getNodeValue());
                                } else if ("theme_icon_mask_deg_x".equals(item.getNodeName())) {
                                    theme.iconMask.degX = Float.parseFloat(item.getFirstChild().getNodeValue());
                                } else if ("theme_icon_mask_deg_y".equals(item.getNodeName())) {
                                    theme.iconMask.degY = Float.parseFloat(item.getFirstChild().getNodeValue());
                                } else if ("theme_icon_mask_padding_left".equals(item.getNodeName())) {
                                    theme.iconMask.paddingLeft = Integer.parseInt(item.getFirstChild().getNodeValue());
                                } else if ("theme_icon_mask_padding_top".equals(item.getNodeName())) {
                                    theme.iconMask.paddingTop = Integer.parseInt(item.getFirstChild().getNodeValue());
                                } else if ("theme_icon_mask_padding_bottom".equals(item.getNodeName())) {
                                    theme.iconMask.paddingBottom = Integer.parseInt(item.getFirstChild().getNodeValue());
                                } else if ("theme_icon_mask_padding_right".equals(item.getNodeName())) {
                                    theme.iconMask.paddingRight = Integer.parseInt(item.getFirstChild().getNodeValue());
                                }
                            }
                        }
                    } else if ("icon_bg".equals(node.getTagName())) {
                        NodeList iconBgList = node.getElementsByTagName("item");
                        final int n = iconBgList.getLength();
                            for (int j = 0; j < n; j++) {
                            Element iconBg = (Element) iconBgList.item(j);
                            String iconBgName = iconBg.getFirstChild().getNodeValue();
                            if (iconBgName != null) {
                                theme.iconBackgrounds.add(iconBgName);
                            }
                        }
                    }
                }
            }
            is.close();
            return theme;
        }catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initIconMaskAndFg() {
        if (model != null && mresProxy != null) {
            mIconMask = mresProxy.getBitmap(model.iconMask.mask);
            mIconFg = mresProxy.getBitmap(model.iconFg);
        } else {
            mIconMask = null;
            mIconFg = null;
        }
    }

    private Bitmap getIconBg(String key){
        if(mIconBg != null){
            int size = mIconBg.length;
            
            if(size == 0 || key == null){
                return null;
            }
            
            int len = key.length();
    
            int i = len%size;
            return mIconBg[i];
        }
        return null;
    }

    private void initIconBg() {
        if (model != null && mresProxy != null) {
            List<Bitmap> bmpList = new ArrayList<Bitmap>();
            for (String str : model.iconBackgrounds) {
                Bitmap bmp = mresProxy.getBitmap(str);
                if (bmp != null) {
                    bmpList.add(bmp);
                }
            }
    
            int len = bmpList.size();
    
            if (len > 0) {
                mIconBg = new Bitmap[len];
                for (int i = 0; i < len; i++) {
                    mIconBg[i] = bmpList.get(i);
                }
    
            } else {
                mIconBg = null;
            }
        } else {
            mIconBg = null;
        }
    }

    private Bitmap getIconBitmapWithTheme(Bitmap bitmap,int dstW,int dstH){
//        Log.d("theme","dstW/dstH:"+dstW+"/"+dstH+",iconW/H:"+mIconMask.getWidth()+","+mIconMask.getHeight());
        dstW = (null == mIconMask)?dstW:mIconMask.getWidth();
        dstH = (null == mIconMask)?dstH:mIconMask.getHeight();
        Bitmap retIcon = null;
        if (0 == model.iconMask.degX && 0 == model.iconMask.degY){
            int nw,nh;
            if (bitmap.getWidth() >= bitmap.getHeight()){
                nw = dstW
                        -Math.max(model.iconMask.paddingLeft +model.iconMask.paddingRight,model.iconMask.paddingTop + model.iconMask.paddingBottom);
                nh = (int)(bitmap.getHeight() * (float)nw/bitmap.getWidth());
                retIcon = Bitmap.createScaledBitmap(bitmap, nw, nh,true);
            }else{
                nh = dstH 
                        -Math.max(model.iconMask.paddingLeft +model.iconMask.paddingRight,model.iconMask.paddingTop + model.iconMask.paddingBottom);
                nw = (int)(bitmap.getWidth() * (float)nh/bitmap.getHeight());
                retIcon = Bitmap.createScaledBitmap(bitmap,nw, nh, true);
            }
        }else{
            float scaleWidth = ((float)dstW / bitmap.getWidth());
            float scaleHeight = ((float)dstH / bitmap.getHeight());
            Matrix matrix = new Matrix();
            Camera camera = new Camera();
            camera.save(); 
            camera.rotateX(model.iconMask.degX); 
            camera.rotateY(model.iconMask.degY); 
            camera.getMatrix(matrix); 
            camera.restore(); 
            matrix.postScale(scaleWidth, scaleHeight); 
            retIcon = Bitmap.createBitmap(bitmap,0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        if (null == mIconMask){
            return retIcon;
        }
        
        Bitmap canvsBitmap = Bitmap.createBitmap(dstW,dstH,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(canvsBitmap); 
        canvas.drawBitmap(retIcon,
                Math.max(model.iconMask.x,dstW-retIcon.getWidth())/2, 
                Math.max(model.iconMask.y,dstH-retIcon.getHeight())/2, null);
        /*
         * canvas原有的图片可以理解为背景，就是dst；画上去的图片可以理解为前景，就是src。
         */
        Paint paint = new Paint();
        paint.setAntiAlias(true); // 消除锯齿
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        canvas.drawBitmap(mIconMask, 0, 0, paint);
        canvas.setBitmap(null);
        if (retIcon != bitmap){
            retIcon.recycle();
        }
        return canvsBitmap;
    }
    
    public Bitmap getIconBitmap(Bitmap icon, boolean needBg, String key){
        Bitmap retIcon = null;
        Bitmap bgimg = getIconBg(key);
        if(needBg && null != bgimg){
            retIcon = Bitmap.createBitmap(bgimg.getWidth(),bgimg.getHeight(),Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(retIcon);
            //画背景
            canvas.drawBitmap(bgimg, 0, 0, null);
            //画icon
            Bitmap iconTheme = getIconBitmapWithTheme(icon,bgimg.getWidth(),bgimg.getHeight());
            int x = Math.max(0,bgimg.getWidth() - iconTheme.getWidth())/2;
            int y = Math.max(0,bgimg.getHeight() - iconTheme.getHeight())/2;
            canvas.drawBitmap(iconTheme, 
                    x, 
                    y, null);
			// 画前景
            if(null != mIconFg){
                canvas.drawBitmap(mIconFg, 0, 0, null);
            }
            canvas.setBitmap(null);
        }else {
            retIcon = getIconBitmapWithTheme(icon,mIconWidth,mIconHeight);
        }
        return retIcon;
    }

// 放大缩小图片
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
    	int width = bitmap.getWidth();
    	int height = bitmap.getHeight();
    	Matrix matrix = new Matrix();
    	float scaleWidht = ((float) w / width);
    	float scaleHeight = ((float) h / height);
    	matrix.postScale(scaleWidht, scaleHeight);
    	Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
    			matrix, true);
    	return newbmp;
    }

    // 将Drawable转化为Bitmap
    public static Bitmap drawableToBitmap(Drawable drawable) {
    	int width = drawable.getIntrinsicWidth();
    	int height = drawable.getIntrinsicHeight();
    	Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
    			.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
    			: Bitmap.Config.RGB_565);
    	Canvas canvas = new Canvas(bitmap);
    	drawable.setBounds(0, 0, width, height);
    	drawable.draw(canvas);
    	return bitmap;
    }

    // 获得圆角图片的方法
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {

    	Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
    			bitmap.getHeight(), Config.ARGB_8888);
    	Canvas canvas = new Canvas(output);
    
    	final int color = 0xff424242;
    	final Paint paint = new Paint();
    	final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    	final RectF rectF = new RectF(rect);
    
    	paint.setAntiAlias(true);
    	canvas.drawARGB(0, 0, 0, 0);
    	paint.setColor(color);
    	canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
    
    	paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    	canvas.drawBitmap(bitmap, rect, rect, paint);
    
    	return output;
    }

    // 获得带倒影的图片方法
    public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
    	final int reflectionGap = 4;
    	int width = bitmap.getWidth();
    	int height = bitmap.getHeight();
    
    	Matrix matrix = new Matrix();
    	matrix.preScale(1, -1);
    
    	Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
    			width, height / 2, matrix, false);
    
    	Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
    			(height + height / 2), Config.ARGB_8888);
    
    	Canvas canvas = new Canvas(bitmapWithReflection);
    	canvas.drawBitmap(bitmap, 0, 0, null);
    	Paint deafalutPaint = new Paint();
    	canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);
    
    	canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
    
    	Paint paint = new Paint();
    	LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
    			bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
    			0x00ffffff, TileMode.CLAMP);
    	paint.setShader(shader);
    	// Set the Transfer mode to be porter duff and destination in
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);

		return bitmapWithReflection;
	}
    	
	public Bitmap getIconMask() {
		return mIconMask;
	}
    
    public Bitmap getIconFg() {
        return mIconFg;
    }
}