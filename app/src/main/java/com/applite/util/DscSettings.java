package com.applite.util;

import android.net.Uri;
import android.provider.BaseColumns;

public class DscSettings {
	static final String AUTHORITY = "com.android.dsc.settings";
	public static final Uri CONTENT_JOBS_URI = Uri.parse("content://" + AUTHORITY + "/jobs");
	public static final Uri CONTENT_CONFIG_URI = Uri.parse("content://" + AUTHORITY + "/config");
    public static final Uri CONTENT_MESSAGES_URI = Uri.parse("content://" + AUTHORITY + "/messages");
    public static final Uri CONTENT_BLACKLIST_URI = Uri.parse("content://" + AUTHORITY + "/blacklist");
    public static final Uri CONTENT_SERVERLIST_URI = Uri.parse("content://" + AUTHORITY + "/serverlist");
    public static final Uri CONTENT_SHOP_URI = Uri.parse("content://" + AUTHORITY + "/shop");


	public static final class Config implements BaseColumns {
		public static final String LONGINTERVAL = "longInterval";
		public static final String NETWORKINGCONDITIONS = "networkingConditions";
		public static final String HEADERRETYINTERVAL = "headerRetryInterval";
		public static final String HEADERRETYCOUNT = "headerRetryCount";
		public static final String CONTENTNETINTERVAL = "contentNetworkingInterval";
		public static final String CONTENTRETYINTERVAL = "contentRetryInterVal";
		public static final String CONTENTRETYCOUNT = "contentRetryCount";
		
		public static final String APIKEY = "api_key";
		public static final String USERID = "user_id";
		public static final String CHANNELID = "channel_id";
		public static final String APPID = "app_id";
		public static final String UUID = "uuid";
		public static final String ADDRESS = "address";
	}

	public static final class Jobs implements BaseColumns {
	    //header
	    public static final String PSVER = "psVer";
		public static final String PAVERSION = "paVersion";
		public static final String PACLASS = "paClass";
		public static final String ISEXECUTED = "isExecuted";
		public static final String EXECUTECOUNT = "executeCount";
		public static final String ISVALID = "isValid";
		public static final String ISCLICKED = "isClicked";
		public static final String HANDLER = "handler";
        public static final String PAVALIDTIME = "paValidTime";
        public static final String WIFIFLAG = "wifiFlag";
        public static final String DISPLAYTIME = "displayTime";
        public static final String EXECUTETIME = "executeTime";
        public static final String FORCEEXECUTEFLAG = "forceExecuteFlag";
        
        //body
        public static final String BODYHTTPURL = "bodyHttpUrl";    //db version=4
        public static final String APPICON = "appIcon";
        public static final String NOTISOUNDURL = "notiSoundUrl";   //db version=4
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "Description";     //db version=4
        public static final String NOTINOCLEAR = "notiNoClear";     //db version=4
        public static final String APPDOWNLOADURL = "appDownloadUrl";
        public static final String APPSIZE = "appDownloadSize";     //db version=4
        public static final String APPSDCARDURL = "appSdcardUrl";
        public static final String PACKAGENAME = "packageName";     //db version=4
        public static final String CLASSNAME = "className";         //db version=4
        public static final String DATAAUTODOWNLOAD = "dataAutoDownload";
        public static final String WIFIAUTODOWNLOAD = "wifiAutoDownload";
        public static final String APPDOWNLOADPROMPT = "appDownloadPrompt";
        public static final String WIFIDOWNLOADPROMPT = "wifiPrompt";
        public static final String CHECKN = "checkN";
        public static final String CHECKINTERVAL = "checkInterval";
        public static final String CHECKCOUNT = "checkCount";
        public static final String SILENTINSTALL = "silentInstall";
        public static final String HOMEPAGEURL = "HomepageUrl";
        public static final String INTENT = "intent";
        public static final String DOWNLOADID = "downloadId";       //db version=4
        public static final String INSTALLED = "installed";
        public static final String BROWSERS = "browsers";
        public static final String BROWSEURL = "browseUrl";
        public static final String RESERVE1 = "reserve1";
        public static final String RESERVE2 = "reserve2";
	}
	
    public static final class Messages implements BaseColumns {
        public static final String MSGID = "msgId";
        public static final String ISCLICKED = "isClicked";
        public static final String NOTITYPE = "notiType";
        public static final String TITLE = "title";
        public static final String DESC = "desc";
        public static final String URL = "url";
        public static final String ISSUPPORTAPP = "isSupportApp";
        public static final String OPENTYPE = "openType";
        public static final String INTENT = "intent";
    }
    
    public static final class Blacklist implements BaseColumns {
        public static final String PKG = "pkg";
        public static final String INTERCEPTION = "interception";
        public static final String NOTITYCOUNT = "notifyCount";
        public static final String NETCOUNT = "netCount";
    }
    
    public static final class Serverlist implements BaseColumns {
        public static final String SERVER_ADDRESS = "server_address";
        public static final String SERVER_URL = "server_url";
        public static final String ORDER = "server_order";
    }

    public static final class Shop implements BaseColumns {
        public static final String TAG = "tag";
        public static final String TITLE = "title";
        public static final String TITLE_ZH = "title_zh";
        public static final String ICON = "icon";
        public static final String ICON_ZH = "icon_zh";
        public static final String INTENT = "intent";
        public static final String PACKAGENAME = "package_name";
        public static final String CLAZZ = "clazz";
        public static final String HTTP_URL = "http_url";
    }
}
