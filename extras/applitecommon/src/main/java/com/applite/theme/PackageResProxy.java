package com.applite.theme;

import java.io.IOException;
import java.io.InputStream;

import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.view.View;

public class PackageResProxy {
private final Resources res;
	
	private final String packageName;

	public PackageResProxy(Resources res, String packageName) throws NameNotFoundException{
		this.res = res;
		this.packageName = packageName; 
	}
	
	
	
	public String getString(String name){
		int resId = getResId(name, "string");
		if(resId == 0){
			return null;
		}
		return res.getString(resId);
	}
	
	public Drawable getDrawable(String name){
		int resId = getResId(name, "drawable");
		if(resId == 0){
			return null;
		}
		return res.getDrawable(resId);
	}
	
	public Drawable getDrawable(int resId){
		if(resId == 0){
			return null;
		}
		return res.getDrawable(resId);
	}
	
	public View findViewById(View v, String name){
		int resId = res.getIdentifier(name, "id", packageName);
		if(resId == 0){
			return null;
		}
		return v.findViewById(resId);
	}
	
	public int getResId(String name, String type){
		if(name == null)
			return 0;
		int resId = res.getIdentifier(name, type, packageName);
		return resId;
	}
	
	public Bitmap getBitmap(String name) {
		if(name == null || name.trim().length() == 0) {
			return null;
		}
		Bitmap bmp = null;
		int wallpaperId=getResId(name, "drawable");
		if(wallpaperId!=0){
            Options mOptions = new BitmapFactory.Options();
            mOptions.inDither = false;
            mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
            	bmp=BitmapFactory.decodeResource(res,wallpaperId, mOptions);
            } catch (OutOfMemoryError e) {
            	e.printStackTrace();
            }
		}
		
		return bmp;
	}
	
	public Bitmap getBitmap(String name,int h){
		Bitmap bmp = null;
		int wallpaperId=getResId(name, "drawable");
		if(wallpaperId!=0){
			try {
					BitmapFactory.Options options =new BitmapFactory.Options();   
					options.inJustDecodeBounds =true;   
			
					bmp =BitmapFactory.decodeResource(res,wallpaperId,options); 
					options.inJustDecodeBounds =false;   
			
					int be = (int)(options.outHeight/(float)h);   
					if (be <= 0)   
			           be = 1;   
					options.inSampleSize = be;   
			     
					bmp =BitmapFactory.decodeResource(res,wallpaperId,options);   
			} catch(OutOfMemoryError e) {
				
			}
		}
		
		return bmp;
	}
	
	public XmlResourceParser getXML(int xmlRes) {
        return res.getXml(xmlRes);
    }

	public Bitmap getBitmap(int resId) {
		Bitmap bmp = null;
		if(resId!=0){
            Options mOptions = new BitmapFactory.Options();
            mOptions.inDither = false;
            mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
            	bmp=BitmapFactory.decodeResource(res,resId, mOptions);
            } catch (OutOfMemoryError e) {
            }
		}
		
		return bmp;
	}
	
	public Bitmap getBitmap(String name, int width, int height) {
		Bitmap bmp = null;
		int id = getResId(name, "drawable");
		if(id != 0) {
			return getBitmap(id, width, height);
		} else {
			return null;
		}
	}

	public Bitmap getBitmap(int resId, int width, int height) {
		Bitmap bmp = null;
		if(resId!=0){
            try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(res,resId, opts);
			int srcWidth = opts.outWidth;
			int srcHeight = opts.outHeight;
			int destWidth = 0;
			int destHeight = 0;
			// 缂傗晜鏂侀惃鍕槷娓氾拷
			double ratio = 0.0;

			// 閹稿鐦笟瀣吀缁犳缂夐弨鎯ф倵閻ㄥ嫬娴橀悧鍥с亣鐏忓骏绱漨axLength閺勵垶鏆遍幋鏍ь啍閸忎浇顔忛惃鍕付婢堆囨毐鎼达拷
			if(srcWidth <= width && srcHeight <= height){
				return BitmapFactory.decodeResource(res, resId, null);
			}
			
			ratio = Math.max(srcWidth/width, srcHeight/height);
			
			destWidth = (int)(srcWidth / ratio);
			destHeight= (int)(srcHeight / ratio);
			
			// 鐎电懓娴橀悧鍥箻鐞涘苯甯囩紓鈺嬬礉閺勵垰婀拠璇插絿閻ㄥ嫯绻冪粙瀣╄厬鏉╂稖顢戦崢瀣級閿涘矁锟芥稉宥嗘Ц閹跺﹤娴橀悧鍥嚢鏉╂稐绨￠崘鍛摠閸愬秷绻樼悰灞藉竾缂傦拷
			BitmapFactory.Options newOpts = new BitmapFactory.Options();
			// 缂傗晜鏂侀惃鍕槷娓氬绱濈紓鈺傛杹閺勵垰绶㈤梾鐐瘻閸戝棗顦搁惃鍕槷娓氬绻樼悰宀�級閺�墽娈戦敍宀�窗閸撳秵鍨滈崣顏勫絺閻滄澘褰ч懗浠嬶拷鏉╁檮nSampleSize閺夈儴绻樼悰宀�級閺�拝绱濋崗璺猴拷鐞涖劍妲戠紓鈺傛杹閻ㄥ嫬锟介弫甯礉
			// SDK娑擃厼缂撶拋顔煎従閸婂吋妲�閻ㄥ嫭瀵氶弫鏉匡拷
			newOpts.inSampleSize = (int) ratio + 1;
			// inJustDecodeBounds鐠佸彞璐�false鐞涖劎銇氶幎濠傛禈閻楀洩顕版潻娑樺敶鐎涙ü鑵�
			newOpts.inJustDecodeBounds = false;
			// 鐠佸墽鐤嗘径褍鐨敍宀冪箹娑擃亙绔撮懜顒佹Ц娑撳秴鍣涵顔炬畱閿涘本妲告禒顨唍SampleSize閻ㄥ嫪璐熼崙鍡礉娴ｅ棙妲告俊鍌涚亯娑撳秷顔曠純顔煎祱娑撳秷鍏樼紓鈺傛杹
			newOpts.outHeight = destHeight; 
			newOpts.outWidth = destWidth;

			return BitmapFactory.decodeResource(res, resId, newOpts);
            } catch (OutOfMemoryError e) {
            	return null;
            }
			
		}
		
		return bmp;
	}
	
	public static BitmapFactory.Options getSizeOpt(InputStream is, int maxSize) throws IOException{
		int maxLength = maxSize;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, opts);
		is.close();
		int srcWidth = opts.outWidth;
		int srcHeight = opts.outHeight;
		int destWidth = 0;
		int destHeight = 0;
		// 缂傗晜鏂侀惃鍕槷娓氾拷
		double ratio = 0.0;

		// 閹稿鐦笟瀣吀缁犳缂夐弨鎯ф倵閻ㄥ嫬娴橀悧鍥с亣鐏忓骏绱漨axLength閺勵垶鏆遍幋鏍ь啍閸忎浇顔忛惃鍕付婢堆囨毐鎼达拷
		if(srcWidth <= maxLength && srcHeight <= maxLength){
			return null;
		}
		
		if (srcWidth > srcHeight) {
			ratio = srcWidth / maxLength;
			destWidth = maxLength;
			destHeight = (int) (srcHeight / ratio);
		} else {
			ratio = srcHeight / maxLength;
			destHeight = maxLength;
			destWidth = (int) (srcWidth / ratio);
		}
		// 鐎电懓娴橀悧鍥箻鐞涘苯甯囩紓鈺嬬礉閺勵垰婀拠璇插絿閻ㄥ嫯绻冪粙瀣╄厬鏉╂稖顢戦崢瀣級閿涘矁锟芥稉宥嗘Ц閹跺﹤娴橀悧鍥嚢鏉╂稐绨￠崘鍛摠閸愬秷绻樼悰灞藉竾缂傦拷
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 缂傗晜鏂侀惃鍕槷娓氬绱濈紓鈺傛杹閺勵垰绶㈤梾鐐瘻閸戝棗顦搁惃鍕槷娓氬绻樼悰宀�級閺�墽娈戦敍宀�窗閸撳秵鍨滈崣顏勫絺閻滄澘褰ч懗浠嬶拷鏉╁檮nSampleSize閺夈儴绻樼悰宀�級閺�拝绱濋崗璺猴拷鐞涖劍妲戠紓鈺傛杹閻ㄥ嫬锟介弫甯礉
		// SDK娑擃厼缂撶拋顔煎従閸婂吋妲�閻ㄥ嫭瀵氶弫鏉匡拷
		newOpts.inSampleSize = (int) ratio + 1;
		// inJustDecodeBounds鐠佸彞璐�false鐞涖劎銇氶幎濠傛禈閻楀洩顕版潻娑樺敶鐎涙ü鑵�
		newOpts.inJustDecodeBounds = false;
		// 鐠佸墽鐤嗘径褍鐨敍宀冪箹娑擃亙绔撮懜顒佹Ц娑撳秴鍣涵顔炬畱閿涘本妲告禒顨唍SampleSize閻ㄥ嫪璐熼崙鍡礉娴ｅ棙妲告俊鍌涚亯娑撳秷顔曠純顔煎祱娑撳秷鍏樼紓鈺傛杹
		newOpts.outHeight = destHeight;
		newOpts.outWidth = destWidth;

		// 閼惧嘲褰囩紓鈺傛杹閸氬骸娴橀悧锟�
		return newOpts;
	}

	public InputStream getBitmapStream(String name) {
		int id=getResId(name, "drawable");
		if(id!=0){
			try {
				return res.openRawResource(id) ;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public InputStream getBitmapStream(int id) {
		if(id!=0){
			try {
				return res.openRawResource(id) ;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

}
