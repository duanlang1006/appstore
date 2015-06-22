package com.kapps.market.log;

import android.util.Log;

/**
 * 
 * @author admin
 * 
 */
public class LogUtil {

	// debug
	public static final boolean debug = true;

	// debug operate
	public static final boolean opDebug = false;

	// http
	public static final boolean httpDebug = true;

	// download
	public static final boolean download_debug = true;

	public static void i(String tag, String msg) {
		if (debug) {
			Log.i(tag, msg);
		}
	}

	public static void v(String tag, String msg) {
		if (debug) {
			Log.v(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (debug) {
			Log.w(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (debug) {
			Log.e(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (debug) {
			Log.d(tag, msg);
		}
	}

	public static void iop(String tag, String msg) {
		if (opDebug) {
			Log.i(tag, msg);
		}
	}

	public static void wop(String tag, String msg) {
		if (opDebug) {
			Log.i(tag, msg);
		}
	}

}
