package com.kapps.market.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.telephony.SmsManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.kapps.market.bean.AppDetail;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.bean.PageInfo;
import com.kapps.market.bean.Software;

/**
 * 2010-6-17
 * 
 * @author admin
 * 
 */
@SuppressLint({ "NewApi", "NewApi", "NewApi" })
public class Util {

	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat shortFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private static NumberFormat apkSizeFormat = new DecimalFormat("0.##");
	private static NumberFormat apkProcessFormat = new DecimalFormat("0.#");

	/**
	 * apk ��С
	 * 
	 * @param size
	 * @param unit
	 * @return
	 */
	public static String apkSizeFormat(double size, String unit) {
		return apkSizeFormat.format(size) + unit;
	}

	/**
	 * "yyyy-MM-dd
	 * 
	 * @return
	 */
	public static String dateFormatShort(Date currentTime) {
		return shortFormatter.format(currentTime);
	}

	public static String dateFormatShort(long currentTime) {
		return formatter.format(new Date(currentTime));
	}

	/**
	 * "yyyy-MM-dd HH:mm:ss"
	 * 
	 * @return
	 */
	public static String dateFormat(Date currentTime) {
		return formatter.format(currentTime);
	}

	public static String dateFormat(long currentTime) {
		return formatter.format(new Date(currentTime));
	}

	/**
	 * ���ĳ��Activity�Ƿ��ڶ���
	 * 
	 * @param ctx
	 * @param pname
	 * @return
	 */
	public static boolean isActivityOnTop(Context ctx, String name) {
		ActivityManager mgr = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> list = mgr.getRunningTasks(50);
		for (ActivityManager.RunningTaskInfo item : list) {
			if (item.baseActivity.getPackageName().equals(ctx.getPackageName())) {
				ComponentName componentName = item.topActivity;// ��ȡ����activity
				if (componentName.getClassName().indexOf(name) != -1) {
					return true;
				}
			}
		}
		return false;
	}

	// �������汾��
	public static int getSelfVersionCode(Context context) {
		PackageManager manager = context.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			if (info != null) {
				return info.versionCode;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return -1;
	}

	public static String getSelfVersionName(Context context) {
		PackageManager manager = context.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			if (info != null) {
				return info.versionName;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return "1.0";
	}

	/**
	 * ���Ͷ���
	 * 
	 * @param toPhone
	 * @param message
	 */
	public static void sendSMSMEssage(String toPhone, String message) {
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(toPhone, null, message, null, null);
	}

	/**
	 * ��ȡ��Ϣ
	 * 
	 * @param TAG
	 * @param info
	 * @param pm
	 * @return
	 */
	public static Software resolvePackageInfo(String TAG, Context context, PackageInfo info, PackageManager pm) {
		try {
			Software software = new Software();
			// ���ϵͳ���
			// TODO ROM ��δ��?�����Ҫ������
			if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
					|| info.packageName.equals(context.getPackageName())) {
				return null;
			}
			software.setVersion(info.versionName);
			software.setVersionCode(info.versionCode);
			software.setName(info.applicationInfo.loadLabel(pm).toString());
			software.setPackageName(info.packageName);
			// ����������ð�װ���Ӧ�����ڵ�Ŀ¼���Ա�֧�ֱ��ݺͻ�ԭ��
			software.setApkPath(info.applicationInfo.sourceDir);
			software.setSize((int) (new File(software.getApkPath()).length() / 1024));
			software.setIconUrl(software.getApkPath());
			// p.applicationInfo.loadIcon(getPackageManager());
			return software;

		} catch (Exception e) {
			// Log.e(TAG, "app packagename: " + e.getMessage());
		}
		return null;
	}

	/**
	 * ��װ�����Сת��KB��MB
	 * 
	 * @param baseApp
	 * @return
	 */
	public static String getSizeDes(BaseApp baseApp) {
		int size = baseApp.getSize() == 0 ? 1 : baseApp.getSize();
		if (size > 1024) {
			return Util.apkSizeFormat(size / 1024.0, "MB");
		} else {
			return Util.apkSizeFormat(size, "KB");
		}
	}

	/**
	 * apk�ļ���������ת��
	 * 
	 * @param baseApp
	 * @return
	 */
	public static String getDownlaodCount(AppDetail appDetail) {
		int count = appDetail.getDownloadCount();
		count = count == 0 ? 1 : count;
		return apkDownlaodCountFormat(count, 1000);

	}

	/**
	 * ���apk��������ʽ
	 * 
	 * @param downloadCount
	 * @param divisor
	 * @return
	 */
	public static String apkDownlaodCountFormat(int downloadCount, int divisor) {
		// ����ģ
		int mod = downloadCount / divisor;

		// ���ޣ����޵�ֵ
		int upline = 0, downline = 0;
		downline = mod * divisor;
		downline = downline == 0 ? 1 : downline;
		upline = (mod + 1) * divisor;

		return new String(downline + "-" + upline);

	}

	/**
	 * ����Ѿ���ж��ȷ������ݵ�����İ汾��
	 * 
	 * @param pname
	 * @param pm
	 * @return
	 */
	public static int getUninstalledSoftwareVersionCode(String pname, PackageManager pm) {
		try {
			PackageInfo pInfo = pm.getPackageInfo(pname, PackageManager.GET_UNINSTALLED_PACKAGES);
			return pInfo.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * ��ÿɻ��ƶ�����ֽ�����
	 */
	public static byte[] getDrawableBytes(BitmapDrawable icon) {
		byte[] data = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			icon.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
			data = out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * ��װһ��ҳ����Ϣ
	 * 
	 * @param pageIndex
	 *            ҳ������
	 * @param perCount
	 *            ÿҳ����
	 * @param totalCount
	 *            ����
	 * @return
	 */
	public static PageInfo wrapPageInfo(int pageIndex, int perCount, int totalCount) {
		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageIndex(pageIndex);
		pageInfo.setPageNum(totalCount / perCount);
		int restCount = totalCount - ((pageIndex - 1) * perCount);
		// ʱ��ҳ������
		pageInfo.setPageSize(restCount > perCount ? perCount : restCount);
		pageInfo.setRecordNum(totalCount);
		return pageInfo;
	}

	/**
	 * �������뷨
	 * 
	 * @param context
	 * @param view
	 */
	public static void hideInputMethodWindow(Context context, View view) {
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			view.clearFocus();
		}
	}

	/**
	 * �Ƿ�����Ч�������ʽ
	 * 
	 * @param strEmail
	 * @return
	 */
	public static boolean isEmail(String strEmail) {
		String strPattern = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(strEmail);
		return m.matches();
	}

	/**
	 * ����Ϊ��url <br>
	 * �����strut�����ٶ�%���б��롣
	 */
	public static String encodeContentForUrl(String content) {

		try {
			return (content == null ? "" : URLEncoder.encode(URLEncoder.encode(content, "UTF-8"), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * ����Ϊ��url
	 */
	public static String decodeContentForUrl(String content) {

		try {
			return (content == null ? "" : URLDecoder.decode(content, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * С�Ľ�ͼ
	 * 
	 * @param buf
	 * @return
	 */
	public static Drawable getScreenThumb(byte[] buf) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 2;
		return new BitmapDrawable(BitmapFactory.decodeByteArray(buf, 0, buf.length, opts));
	}

	/**
	 * С�Ľ�ͼ���ֽ�
	 * 
	 * @param buf
	 * @return
	 */
	public static byte[] getScreenThumbBytes(byte[] buf) {
		try {
			BitmapDrawable drawable = (BitmapDrawable) getScreenThumb(buf);
			Bitmap bitMap = drawable.getBitmap();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			boolean success = bitMap.compress(CompressFormat.PNG, 100, out);
			if (!success) {
				Bitmap cloneImg = Bitmap.createScaledBitmap(bitMap, bitMap.getWidth(), bitMap.getHeight(), false);
				out = new ByteArrayOutputStream();
				cloneImg.compress(CompressFormat.PNG, 100, out);
			}

			return out.toByteArray();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return buf;
	}

	/**
	 * ��û��Ƶ�ʱ��ʱ����Ǽ�
	 * 
	 * @param rate
	 *            0-5
	 */
	public static int getDrawRateVaue(double rate) {
		// TODO �������ֵ��0-5
		return (int) Math.ceil(rate * 2);
	}

	/**
	 * ʹ���µ����������ͼ��2.3����ͼ���÷���仯��
	 * 
	 * @return
	 */
	public static boolean useNewAppAttrView() {
		int sdkVersion = 1;
		try {
			sdkVersion = Build.VERSION.class.getField("SDK_INT").getInt(null);
		} catch (Exception e) {
		}
		return (sdkVersion > 8);
	}

	/**
	 * ��ȡ�����������
	 * 
	 * @param is
	 * @return
	 */
	public static byte[] readInputStreamData(InputStream is) {
		ByteArrayOutputStream bab = new ByteArrayOutputStream();
		byte[] datas = new byte[8192];
		int count = -1;
		try {
			while ((count = is.read(datas, 0, datas.length)) != -1) {
				bab.write(datas, 0, count);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bab.toByteArray();
	}

	/**
	 * ����apk�ļ����ؽ�ȸ����ٽ�ֵ
	 * 
	 * @return perSize(�������ؽ���ٽ�ֵ)
	 */
	public static int computeApkFileProgressCritcalSize(int totalBytes) {
		int perSize = 0;
		if (totalBytes > Constants.APK_FILE_CRITICAL_SIZE) {
			perSize = Constants.APK_LARGE_PROGRESS_SIZE;

		} else {
			perSize = totalBytes / 10;

		}

		return perSize;

	}

	/**
	 * ������ؽ���ַ�����
	 * 
	 * @param dsize
	 * @param totalSize
	 * @return
	 */
	public static String getDownloadProgressStr(double dsize, double totalSize) {
		totalSize = totalSize == 0 ? 1 : totalSize;
		return apkProcessFormat.format(dsize / totalSize * 100) + "%";
	}

}
