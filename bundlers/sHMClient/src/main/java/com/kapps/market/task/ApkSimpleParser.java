package com.kapps.market.task;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

/**
 * Package archive parsing
 * 
 * {@hide}
 */
public class ApkSimpleParser {

	private static final String TAG = "PackageParser";
	private static int[] R_AndroidManifest;
	private static int R_AndroidManifest_versionCode = -49;
	private static int R_AndroidManifest_versionName = -49;

	private static int[] R_AndroidManifestApplication;
	private static int R_AndroidManifestApplication_label = -49;
	private static int R_AndroidManifestApplication_icon = -49;

	private ApkSimpleParser() {
	}

	/**
	 * apk�ĸ�Ҫ��Ϣ
	 * 
	 * @param sourceFile
	 * @return
	 */
	public static ApkSummary parseApkSummaryInfo(File sourceFile) {
		String mArchiveSourcePath = sourceFile.getPath();
		XmlResourceParser parser = null;
		AssetManager assmgr = null;
		boolean assetError = true;
		try {
			initR_AndroidManifest();
			initR_AndroidManifest_versionCode();
			initR_AndroidManifest_versionName();

			assmgr = getAssetManager();
			int cookie = addAssetPathForAssetManager(assmgr, mArchiveSourcePath);
			if (cookie != 0) {
				parser = assmgr.openXmlResourceParser(cookie, "AndroidManifest.xml");
				assetError = false;

			} else {
				Log.w(TAG, "Failed adding asset path:" + mArchiveSourcePath);
			}
		} catch (Exception e) {
			Log.w(TAG, "Unable to read AndroidManifest.xml of " + mArchiveSourcePath, e);
		}
		if (assetError) {
			if (assmgr != null)
				assmgr.close();
			return null;
		}

		ApkSummary pkg = null;
		Exception errorException = null;
		try {
			DisplayMetrics metrics = new DisplayMetrics();
			Resources res = new Resources(assmgr, metrics, null);
			pkg = parsePackage(res, parser);

		} catch (Exception e) {
			errorException = e;
		}

		if (pkg == null) {
			if (errorException != null) {
				Log.w(TAG, mArchiveSourcePath, errorException);
			} else {
				Log.w(TAG, mArchiveSourcePath + " (at " + parser.getPositionDescription() + "): ");
			}
			parser.close();
			assmgr.close();
			return null;
		}

		parser.close();
		assmgr.close();

		return pkg;
	}

	/**
	 * apk ����Ϣ��Ϣ��Ҫ�����ֺ�ͼ��
	 * 
	 * @param context
	 * @param sourceFile
	 * @param defaultName
	 * @param findIcon
	 *            �Ƿ����ͼ��
	 * @return
	 */
	public static ApkDetail parsetApkDetailInfo(Context context, File sourceFile, String defaultName, boolean findIcon) {
		String mArchiveSourcePath = sourceFile.getPath();
		XmlResourceParser parser = null;
		AssetManager assmgr = null;
		boolean assetError = true;
		try {
			initR_AndroidManifestApplication();
			initR_AndroidManifestApplication_icon();
			initR_AndroidManifestApplication_label();

			assmgr = getAssetManager();
			int cookie = addAssetPathForAssetManager(assmgr, mArchiveSourcePath);
			if (cookie != 0) {
				parser = assmgr.openXmlResourceParser(cookie, "AndroidManifest.xml");
				assetError = false;

			} else {
				Log.w(TAG, "Failed adding asset path:" + mArchiveSourcePath);
			}
		} catch (Exception e) {
			Log.w(TAG, "Unable to read AndroidManifest.xml of " + mArchiveSourcePath, e);
		}
		if (assetError) {
			if (assmgr != null)
				assmgr.close();
			return null;
		}

		ApkDetail pkg = null;
		Exception errorException = null;
		try {
			Resources pRes = context.getResources();
			Resources res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration());
			pkg = parsePackageDetailInfo(res, parser, context, defaultName, findIcon);

		} catch (Exception e) {
			errorException = e;
		}

		if (pkg == null) {
			if (errorException != null) {
				Log.w(TAG, mArchiveSourcePath, errorException);
			} else {
				Log.w(TAG, mArchiveSourcePath + " (at " + parser.getPositionDescription() + "): ");
			}
			parser.close();
			assmgr.close();
			return null;
		}

		parser.close();
		assmgr.close();

		return pkg;
	}

	private static ApkDetail parsePackageDetailInfo(Resources res, XmlResourceParser parser, Context context,
			String defaultName, boolean findIcon) throws Exception {
		int outerDepth = parser.getDepth();
		int type = 0;

		ApkSnippet apkSnippet = new ApkSnippet();
		while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
				&& (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
			if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
				continue;
			}

			String tagName = parser.getName();
			if (tagName != null && tagName.equals("application")) {
				TypedArray sa = res.obtainAttributes(parser, R_AndroidManifestApplication);
				TypedValue v = sa.peekValue(R_AndroidManifestApplication_label);
				if (v != null && (apkSnippet.labelRes = v.resourceId) == 0) {
					apkSnippet.nonLocalizedLabel = v.coerceToString();
				}
				apkSnippet.icon = sa.getResourceId(R_AndroidManifestApplication_icon, 0);
				sa.recycle();
				break;
			}
		}

		CharSequence label = null;
		if (apkSnippet.labelRes != 0) {
			try {
				label = res.getText(apkSnippet.labelRes);
			} catch (Resources.NotFoundException e) {
			}
		}
		if (label == null) {
			label = (apkSnippet.nonLocalizedLabel != null) ? apkSnippet.nonLocalizedLabel : defaultName;
		}

		// û��ָ����icon�ٲ���
		Drawable icon = null;
		if (findIcon) {
			if (apkSnippet.icon != 0) {
				try {
					icon = res.getDrawable(apkSnippet.icon);
				} catch (Resources.NotFoundException e) {
				}
			}
			if (icon == null) {
				icon = context.getPackageManager().getDefaultActivityIcon();
			}
		}

		return new ApkDetail(label, icon);
	}

	private static ApkSummary parsePackage(Resources res, XmlResourceParser parser) throws XmlPullParserException,
			IOException {
		AttributeSet attrs = parser;
		int type;
		while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
			;
		}
		String pkgName = attrs.getAttributeValue(null, "package");
		if (pkgName == null) {
			return null;
		}

		ApkSummary pkg = new ApkSummary(pkgName);
		TypedArray sa = res.obtainAttributes(attrs, R_AndroidManifest);
		pkg.versionCode = sa.getInteger(R_AndroidManifest_versionCode, 0);
		pkg.versionName = sa.getString(R_AndroidManifest_versionName);
		if (pkg.versionName != null) {
			pkg.versionName = pkg.versionName.intern();
		}
		sa.recycle();

		return pkg;
	}

	/**
	 * apk ��ͼ��
	 * 
	 * @return
	 */
	public static Drawable parseApkDrawableo(Context context, File sourceFile) throws Exception {
		String mArchiveSourcePath = sourceFile.getPath();
		XmlResourceParser parser = null;
		AssetManager assmgr = null;
		boolean assetError = true;
		try {
			initR_AndroidManifestApplication();
			initR_AndroidManifestApplication_icon();
			initR_AndroidManifestApplication_label();

			assmgr = getAssetManager();
			int cookie = addAssetPathForAssetManager(assmgr, mArchiveSourcePath);
			if (cookie != 0) {
				parser = assmgr.openXmlResourceParser(cookie, "AndroidManifest.xml");
				assetError = false;

			} else {
				Log.w(TAG, "Failed adding asset path:" + mArchiveSourcePath);
			}
		} catch (Exception e) {
			Log.w(TAG, "Unable to read AndroidManifest.xml of " + mArchiveSourcePath, e);
		}
		if (assetError) {
			if (assmgr != null)
				assmgr.close();
			return null;
		}

		Drawable icon = null;
		Exception errorException = null;
		try {
			Resources pRes = context.getResources();
			Resources res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration());
			icon = parseDrawableInfo(res, parser, context);

		} catch (Exception e) {
			errorException = e;
		}

		if (icon == null) {
			if (errorException != null) {
				Log.w(TAG, mArchiveSourcePath, errorException);
			} else {
				Log.w(TAG, mArchiveSourcePath + " (at " + parser.getPositionDescription() + "): ");
			}
			parser.close();
			assmgr.close();
			return null;
		}

		parser.close();
		assmgr.close();

		return icon;
	}

	private static Drawable parseDrawableInfo(Resources res, XmlResourceParser parser, Context context)
			throws Exception {
		int outerDepth = parser.getDepth();
		int type = 0;

		int iconRes = 0;
		while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
				&& (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
			if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
				continue;
			}

			String tagName = parser.getName();
			if (tagName != null && tagName.equals("application")) {
				TypedArray sa = res.obtainAttributes(parser, R_AndroidManifestApplication);
				iconRes = sa.getResourceId(R_AndroidManifestApplication_icon, 0);
				sa.recycle();
				break;
			}
		}

		// û��ָ����icon�ٲ���
		Drawable icon = null;
		if (iconRes != 0) {
			try {
				icon = res.getDrawable(iconRes);
			} catch (Resources.NotFoundException e) {
			}
		}
		if (icon == null) {
			icon = context.getPackageManager().getDefaultActivityIcon();
		}

		return icon;
	}

	private static AssetManager getAssetManager() throws Exception {
		return AssetManager.class.newInstance();
	}

	private static int addAssetPathForAssetManager(AssetManager assetManager, String path) throws Exception {
		Method method = assetManager.getClass().getMethod("addAssetPath", String.class);
		return Integer.parseInt(method.invoke(assetManager, path).toString());
	}

	private static void initR_AndroidManifest() throws Exception {
		if (R_AndroidManifest == null) {
			Class styleable = Class.forName("com.android.internal.R$styleable");
			R_AndroidManifest = (int[]) styleable.getField("AndroidManifest").get(styleable);
		}
	}

	private static void initR_AndroidManifest_versionCode() throws Exception {
		if (R_AndroidManifest_versionCode == -49) {
			Class styleable = Class.forName("com.android.internal.R$styleable");
			R_AndroidManifest_versionCode = styleable.getField("AndroidManifest_versionCode").getInt(styleable);
		}
	}

	private static void initR_AndroidManifest_versionName() throws Exception {
		if (R_AndroidManifest_versionName == -49) {
			Class styleable = Class.forName("com.android.internal.R$styleable");
			R_AndroidManifest_versionName = styleable.getField("AndroidManifest_versionName").getInt(styleable);
		}
	}

	private static void initR_AndroidManifestApplication() throws Exception {
		if (R_AndroidManifestApplication == null) {
			Class styleable = Class.forName("com.android.internal.R$styleable");
			R_AndroidManifestApplication = (int[]) styleable.getField("AndroidManifestApplication").get(styleable);
		}
	}

	private static void initR_AndroidManifestApplication_label() throws Exception {
		if (R_AndroidManifestApplication_label == -49) {
			Class styleable = Class.forName("com.android.internal.R$styleable");
			R_AndroidManifestApplication_label = styleable.getField("AndroidManifestApplication_label").getInt(
					styleable);
		}
	}

	private static void initR_AndroidManifestApplication_icon() throws Exception {
		if (R_AndroidManifestApplication_icon == -49) {
			Class styleable = Class.forName("com.android.internal.R$styleable");
			R_AndroidManifestApplication_icon = styleable.getField("AndroidManifestApplication_icon").getInt(styleable);
		}
	}

	public final static class ApkSnippet {
		public int labelRes;
		public CharSequence nonLocalizedLabel;
		public int icon;

		@Override
		public String toString() {
			return "ApkDetail [icon=" + icon + ", labelRes=" + labelRes + ", nonLocalizedLabel=" + nonLocalizedLabel
					+ "]";
		}

	}

	public final static class ApkSummary {
		public final String packageName;

		public int versionCode;

		public String versionName;

		public ApkSummary(String _name) {
			packageName = _name;
		}

	}

	public static class ApkDetail {
		public CharSequence label;
		public Drawable icon;

		public ApkDetail(CharSequence label, Drawable icon) {
			this.label = label;
			this.icon = icon;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "AppDetail [icon=" + icon + ", label=" + label + "]";
		}

	}

}
