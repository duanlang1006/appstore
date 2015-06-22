package com.kapps.market.service.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

import com.kapps.market.MApplication;
import com.kapps.market.NetworkinfoParser;
import com.kapps.market.R;
import com.kapps.market.bean.AppBadness;
import com.kapps.market.bean.AppCategory;
import com.kapps.market.bean.AppComment;
import com.kapps.market.bean.AppDetail;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.AppPermission;
import com.kapps.market.bean.CommentMark;
import com.kapps.market.bean.KeyWord;
import com.kapps.market.bean.LoginResult;
import com.kapps.market.bean.MarketUpdateInfo;
import com.kapps.market.bean.PageableResult;
import com.kapps.market.bean.Software;
import com.kapps.market.bean.StaticAD;
import com.kapps.market.bean.UserInfo;
import com.kapps.market.bean.config.ContextConfig;
import com.kapps.market.log.LogUtil;
import com.kapps.market.service.ActionException;
import com.kapps.market.service.IMarketService;
import com.kapps.market.util.Constants;
import com.kapps.market.util.SecurityUtil;

/**
 * 2010-6-8
 *
 * @author admin
 * 
 */
public class HttpMarketService implements IMarketService {

	public static final String TAG = "HttpMarketService";

	private final MApplication marketContext;
	private final ContextConfig contextConfig;
	// 锟斤拷梅锟斤拷锟斤拷锟斤拷锟斤拷协锟介工锟斤拷
	private final HIMProtocalFactory protocalFactory;

	// http://192.168.1.111:8080
	// http://apks.mogoyun.com:8849

//	 public static final String DOMAIN = "http://192.168.1.44:8080";
//	public static final String DOMAIN = "http://apks.mogoyun.com:8849";
	// public static final String DOMAIN = "http://54.148.68.60:5050";
	public static final String DOMAIN = "http://112.124.40.31:4047";
	//public static final String DOMAIN = "http://192.168.0.102:8080";

	// 锟斤拷通
	public static final String BASE_URL = DOMAIN + "/apkclient/api.do";

	// private static final String DOMAIN = "http://test.market.hiapk.com";
	//
	// // 锟斤拷锟斤拷
	// public static final String SERVICE_DOMAIN = DOMAIN + "/service";
	// // 锟斤拷通
	// public static final String BASE_URL = DOMAIN + "/service/api2.php";

	public HttpMarketService(MApplication marketContext, HIMProtocalFactory protocalFactory) {
		this.marketContext = marketContext;
		this.protocalFactory = protocalFactory;
		contextConfig = marketContext.getContextConfig();
	}

	@Override
	public LoginResult login(UserInfo user, String deviceId, String simId, String sign) throws ActionException {
		ProtocalWrap protocal = null;

		protocal = protocalFactory.loginProtocal(user.getName(), user.getPassword(), deviceId, simId, sign);
		// 锟斤拷锟斤拷锟斤拷
		byte[] result = requestServiceResource(protocal);		
		LoginHandler loginHandler = new LoginHandler();
		loginHandler.parserXml(result);
		return loginHandler.getLoginResult();
	}

	@Override
	public void register(UserInfo user, String sign) throws ActionException {
		ProtocalWrap protocal = protocalFactory.registeProtocal(user.getName(), user.getPassword(), sign);
		byte[] result = requestServiceResource(protocal);

		ResultStateHandler resultStateHandler = new ResultStateHandler();
		resultStateHandler.parserXml(result);
	}

	@Override
	public List<AppCategory> getAppCategoryList() throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryCategoryProtocal();
		byte[] result = requestServiceResource(protocal);

		CategoryHandler categoryHandler = new CategoryHandler();
		categoryHandler.parserXml(result);

		return categoryHandler.getCategoryList();
	}

	@Override
	public PageableResult getAppListByCategory(int categoryId, int sortType, int feeType, int pageIndex, int perCount)
			throws ActionException {

		ProtocalWrap protocal = protocalFactory.queryAppItemsByCategoryProtocal(categoryId, sortType, feeType,
				pageIndex, perCount);
//        Log.d("temp", "getAppListByCategory--->in:protocal="+protocal.getGetData());
		byte[] result = requestServiceResource(protocal);
        Log.d("temp", "getAppListByCategory--->data="+new String(result));
		AppListHandler appItemHandler = new AppListHandler();
		appItemHandler.parserXml(result);
        List<AppItem> appItems = appItemHandler.getAppItemList();
//        Log.d("temp", "getAppListByCategory--->out."+(appItems==null?0:appItems.size()));
		return new PageableResult(appItemHandler.getAppItemList(), appItemHandler.getPageInfo());
	}

	@Override
	public PageableResult getAppListByTopDownload(int sorttype, int pi, int ps) throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryAppItemsByDownloadProtocal(sorttype, pi, ps);
		byte[] result = requestServiceResource(protocal);

		AppListHandler appItemHandler = new AppListHandler();
		appItemHandler.parserXml(result);

		return new PageableResult(appItemHandler.getAppItemList(), appItemHandler.getPageInfo());
	}

	@Override
	public PageableResult getAppListByTopDownloadFirstPage(int sorttype, int pi, int ps) throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryAppItemsByDownloadFirstPageProtocal(sorttype, pi, ps);
		byte[] result = requestServiceResource(protocal);

		AppListHandler appItemHandler = new AppListHandler();
		appItemHandler.parserXml(result);

		return new PageableResult(appItemHandler.getAppItemList(), appItemHandler.getPageInfo());
	}
	@Override
	public PageableResult getAppListByRecommend(int recommendId, int sortType, int pageIndex, int perCount)
			throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryAppItemsByRecommendProtocal(recommendId, sortType, pageIndex,
				perCount);
		byte[] result = requestServiceResource(protocal);

		AppListHandler appItemHandler = new AppListHandler();
		appItemHandler.parserXml(result);

		return new PageableResult(appItemHandler.getAppItemList(), appItemHandler.getPageInfo());
	}

	@Override
	public PageableResult getNewsAppList(int pageIndex, int perCount) throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryNewsAppItemsProtocal(pageIndex, perCount);
		byte[] result = requestServiceResource(protocal);

		AppListHandler appItemHandler = new AppListHandler();
		appItemHandler.parserXml(result);

		return new PageableResult(appItemHandler.getAppItemList(), appItemHandler.getPageInfo());
	}

	@Override
	public PageableResult getAdvertiseApps(int poptype, int pageIndex, int perCount) throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryAdvertiseAppsProtocal(poptype, pageIndex, perCount);
		byte[] result = requestServiceResource(protocal);
//Log.d("temp", "adv="+(new String(result)));
		AppListHandler appItemHandler = new AppListHandler();
		appItemHandler.parserXml(result);

		return new PageableResult(appItemHandler.getAppItemList(), appItemHandler.getPageInfo());
	}

	@Override
	public PageableResult getHistoryAppList(int appId, String pname) throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryHistoryAppItemsProtocal(appId, pname);
		byte[] result = requestServiceResource(protocal);

		AppListHandler appItemHandler = new AppListHandler();
		appItemHandler.parserXml(result);

		return new PageableResult(appItemHandler.getAppItemList(), appItemHandler.getPageInfo());
	}

	@Override
	public AppDetail getAppDetailById(int appId) throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryAppDetailProtocal(appId);
		byte[] result = requestServiceResource(protocal);

		AppDetailHandler appDetailHandler = new AppDetailHandler();
		appDetailHandler.parserXml(result);

		return appDetailHandler.getAppDetail();
	}

	@Override
	public PageableResult getAppCommentList(int appId, String pname, int pageIndex, int perCount)
			throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryAppCommentsProtocal(appId, pname, pageIndex, perCount);
		byte[] result = requestServiceResource(protocal);

		CommentsHandler appCommentHandler = new CommentsHandler();
		appCommentHandler.parserXml(result);

		return new PageableResult(appCommentHandler.getAppCommentList(), appCommentHandler.getPageInfo());
	}

	@Override
	public void commitAppComment(AppComment appComment, String pname, String sign) throws ActionException {
		ProtocalWrap protocal = protocalFactory.commitAppCommentProtocal(appComment.getAppId(), pname,
				appComment.getRating(), appComment.getContent(), sign);
		byte[] result = requestServiceResource(protocal);

		ResultStateHandler resultStateHandler = new ResultStateHandler();
		resultStateHandler.parserXml(result);
	}

	@Override
	public CommentMark commitAppCommentMark(int commentId, int mark) throws ActionException {
		ProtocalWrap protocal = protocalFactory.commitAppCommentMarkProtocal(commentId, mark);
		byte[] result = requestServiceResource(protocal);

		CommentMarketHandler commentMarkHandler = new CommentMarketHandler();
		commentMarkHandler.parserXml(result);
		return commentMarkHandler.getComentMark();
	}

	@Override
	public void commitBadnessContent(AppBadness appBadness, String sign) throws ActionException {
		ProtocalWrap protocal = protocalFactory.commitAppBadnessProtocal(appBadness.getAppId(), appBadness.getIndex(),
				appBadness.getContent(), sign);
		byte[] result = requestServiceResource(protocal);

		ResultStateHandler resultStateHandler = new ResultStateHandler();
		resultStateHandler.parserXml(result);
	}

	@Override
	public PageableResult searchAppByCondition(int type, String key, int pageIndex, int perCount)
			throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryAppByConditionProtocal(type, key, pageIndex, perCount);
		byte[] result = requestServiceResource(protocal);

		AppListHandler appItemHandler = new AppListHandler();
		appItemHandler.parserXml(result);

		return new PageableResult(appItemHandler.getAppItemList(), appItemHandler.getPageInfo());
	}

	@Override
	public PageableResult getAppListByDeveloper(String developer, int pageIndex, int perCount) throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryAppItemsByDeveloperProtocal(developer, pageIndex, perCount);
		byte[] result = requestServiceResource(protocal);

		AppListHandler appItemHandler = new AppListHandler();
		appItemHandler.parserXml(result);

		return new PageableResult(appItemHandler.getAppItemList(), appItemHandler.getPageInfo());
	}

	@Override
	public byte[] getAppImageResource(int appId, String url, int type, String sign) throws ActionException {
		ProtocalWrap protocal = protocalFactory.getAppImageResouceProtocal(appId, url, sign);
		byte[] result = requestServiceResource(protocal);

		return result;
	}

	@Override
	public void addFavorAppItem(int appId, String sign) throws ActionException {
		ProtocalWrap protocal = protocalFactory.addFavorAppItemProtocal(appId, sign);
		byte[] result = requestServiceResource(protocal);

		ResultStateHandler resultStateHandler = new ResultStateHandler();
		resultStateHandler.parserXml(result);
	}

	@Override
	public void deleteFavorAppItem(int appId, String sign) throws ActionException {
		ProtocalWrap protocal = protocalFactory.deleteFavorAppItemProtocal(appId, sign);
		byte[] result = requestServiceResource(protocal);

		ResultStateHandler resultStateHandler = new ResultStateHandler();
		resultStateHandler.parserXml(result);
	}

	@Override
	public PageableResult getAppFavorList(int pageIndex, int perCount) throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryFavorAppListProtocal(pageIndex, perCount);
		byte[] result = requestServiceResource(protocal);

		AppListHandler appListHandler = new AppListHandler();
		appListHandler.parserXml(result);

		return new PageableResult(appListHandler.getAppItemList(), appListHandler.getPageInfo());
	}

	@Override
	public List<AppPermission> getAppPermissionList(int appId) throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryAppPermissionProtocal(appId);
		byte[] result = requestServiceResource(protocal);

		AppPermissionHandler appPermissionHandler = new AppPermissionHandler();
		appPermissionHandler.parserXml(result);

		return appPermissionHandler.getAppPermissionList();
	}

	@Override
	public AppItem getAppSummary(String pname, int versionCode) throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryAppSummaryProtocal(pname, versionCode);
		byte[] result = requestServiceResource(protocal);

		AppListHandler appListHandler = new AppListHandler();
		appListHandler.parserXml(result);

		if (appListHandler.getAppItemList() == null || appListHandler.getAppItemList().size() == 0) {
			return null;

		} else {
			return appListHandler.getAppItemList().get(0);
		}
	}

	@Override
	public AppItem getAppSummaryById(int appid) throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryAppSummaryProtocal(appid);
		byte[] result = requestServiceResource(protocal);

		AppListHandler appListHandler = new AppListHandler();
		appListHandler.parserXml(result);

		if (appListHandler.getAppItemList() == null || appListHandler.getAppItemList().size() == 0) {
			return null;

		} else {
			return appListHandler.getAppItemList().get(0);
		}
	}

	@Override
	public List<AppItem> checkSoftwareUpdate(List<Software> softwareList) throws ActionException {
		ProtocalWrap protocal = protocalFactory.checkSoftwareUpdateProtocal(softwareList);
		byte[] result = requestServiceResource(protocal);
		AppListHandler appListHandler = new AppListHandler();
		appListHandler.parserXml(result);
		return appListHandler.getAppItemList();
	}

	@Override
	public MarketUpdateInfo checkMarketUpdate(ContextConfig contextConfig) throws ActionException {
		ProtocalWrap protocal = protocalFactory.checkMarketUpdateProtocal(contextConfig.getVid(),
				contextConfig.getVersionCode());
		byte[] result = requestServiceResource(protocal);
		MarketUpdateHandler marketUpdateHandler = new MarketUpdateHandler();
		marketUpdateHandler.parserXml(result);
		return marketUpdateHandler.getMarketUpdateInfo();
	}

	@Override
	public void reportChannel(String did, String cid, long createTime, String sign) throws ActionException {
		ProtocalWrap protocal = protocalFactory.getChannelReportProtocal(did, cid, createTime, sign);
		byte[] result = requestServiceResource(protocal);
		ResultStateHandler resultStateHandler = new ResultStateHandler();
		resultStateHandler.parserXml(result);
	}

	// /////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////
	// 只锟叫凤拷锟斤拷HTTP_OK锟斤拷锟叫斤拷锟斤拷锟斤拷锟斤拷壮锟絊erviceException
	private byte[] requestServiceResource(ProtocalWrap protocal) throws ActionException {
		DefaultHttpClient httpClient = null;
		byte[] result = null;
		try {
			// 锟斤拷锟斤拷锟斤拷锟斤拷
			httpClient = NetworkinfoParser.getHttpConnector(marketContext);
			// 锟角凤拷要锟斤拷锟斤拷锟斤拷锟皆伙拷锟斤拷
			if (protocal.isReTry()) {
				httpClient.setHttpRequestRetryHandler(new RetryHandler());
			}
			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					protocal.getSoTimeout() > 0 ? protocal.getSoTimeout()
							: 15000); // 锟斤拷锟接筹拷时

			String urlStrl = null;
			if (protocal.getHost() == null) {
				urlStrl = BASE_URL;
			} else {
				urlStrl = protocal.getHost();
			}

			// url锟斤拷锟斤拷锟�
			if (protocal.getGetData() != null) {
				urlStrl += "?" + protocal.getGetData();
			}

			HttpRequestBase httpRequest = null;
			// post锟斤拷莶锟斤拷锟�
			if (protocal.getPostData() != null) {
//                Log.d("temp", "post url="+urlStrl);
				httpRequest = new HttpPost(urlStrl);
				byte[] sendData = protocal.getPostData().getBytes("UTF-8");
				((HttpPost) httpRequest).setEntity(new ByteArrayEntity(sendData));

			} else {
//                Log.d("temp", "get url="+urlStrl);
				httpRequest = new HttpGet(urlStrl);
			}

			// 锟斤拷锟斤拷时
			httpRequest.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					protocal.getSoTimeout() > 0 ? protocal.getSoTimeout() : 15000);

			// 头锟斤拷锟斤拷锟斤拷
			String sessionId = marketContext.getSharedPrefManager().getSession();
			httpRequest.addHeader(H_SESSION_ID, sessionId);
			httpRequest.addHeader(H_SER_TS, marketContext.getTs());
			httpRequest.addHeader(H_RESOLUTION, contextConfig.getResolution());
			httpRequest.addHeader(H_DENSITY, contextConfig.getDensity());
			httpRequest.addHeader(H_SDK_VERSION, contextConfig.getSdkVersion());
			httpRequest.addHeader(H_LANGUAGE, Locale.getDefault().getLanguage());
			httpRequest.addHeader("Accept-Encoding", "gzip");
			httpRequest.addHeader("Content-Type", "text/html;charset=UTF-8");

			if (LogUtil.httpDebug) {
				Log.v(TAG, "requestServiceResource sendProtocal cookie " + sessionId + " \nprotocal: " + protocal
						+ " \nurlStrl: " + urlStrl + "\npost data: " + protocal.getPostData() + "\n");
				Header[] heads = httpRequest.getAllHeaders();
				Log.v(TAG, "httpRequest.getAllHeaders()------------ \n");
				for (Header header : heads) {
					Log.v(TAG, "header: " + header + "\n");
				}
			}

			// 执锟斤拷
			HttpResponse httpResponse = httpClient.execute(httpRequest);
			int httpCode = httpResponse.getStatusLine().getStatusCode();

			if (LogUtil.httpDebug) {
				Log.v(TAG, "requestServiceResource response httpCode: " + httpCode + "");
				Header[] heads = httpResponse.getAllHeaders();
				Log.v(TAG, "httpResponse.getAllHeaders()------------ \n");
				for (Header header : heads) {
					Log.v(TAG, "header: " + header + "\n");
				}
			}

			if (httpCode == HttpURLConnection.HTTP_OK) {
				Header encodeHader = httpResponse.getLastHeader("Content-Encoding");
				if (encodeHader != null && "gzip".equals(encodeHader.getValue())) {
					result = handleReponse(httpResponse, true);
				} else {
					// 锟斤拷锟斤拷锟斤拷
					result = handleReponse(httpResponse, false);
				}

				if (LogUtil.httpDebug) {
					Log.v(TAG, result == null ? "" : new String(result));
				}

			} else {
				throw new ActionException(ActionException.RESULT_ERROR, marketContext.getResources().getString(R.string.network_abnormal));
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			if (ex instanceof ActionException) {
				throw (ActionException) ex;

			} else if (ex.getCause() instanceof ActionException) {
				throw (ActionException) ex.getCause();

			} else if (ex instanceof SocketException) {
				throw new ActionException(ActionException.RESULT_ERROR, marketContext.getResources().getString(R.string.networkyichang));

			} else {
				throw new ActionException(ActionException.RESULT_ERROR,marketContext.getResources().getString(R.string.network_abnormal));
			}

		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}

		return result;
	}

	// 锟斤拷锟斤拷锟斤拷应
	private byte[] handleReponse(HttpResponse response, boolean gzip) throws IOException {
		InputStream is = null;
		HttpEntity entity = response.getEntity();
		ByteArrayOutputStream bab = new ByteArrayOutputStream();

		if (entity != null) {
			if (gzip) {
				is = new GZIPInputStream(entity.getContent(), 8196);
			} else {
				is = new BufferedInputStream(entity.getContent());
			}
			byte[] datas = new byte[8192];
			int count = -1;
			
			while ((count = is.read(datas, 0, datas.length)) != -1) {
				bab.write(datas, 0, count);
			
			}
		}
		return bab.toByteArray();
	}

	// 锟斤拷锟皆达拷锟斤拷锟斤拷, 锟斤拷锟斤拷锟斤拷锟斤拷3锟轿★拷
	private class RetryHandler implements HttpRequestRetryHandler {

		@Override
		public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
			if (LogUtil.httpDebug) {
				Log.v(TAG, "requestServiceResource response executionCount: " + executionCount + " exception:"
						+ exception);
			}
			if (executionCount > 3) {
				// Do not retry if over max retry count
				return false;
			}
			if (exception instanceof NoHttpResponseException) {
				// Retry if the server dropped connection on us
				return true;
			}
			if (exception instanceof SSLHandshakeException) {
				// Do not retry on SSL handshake exception
				return false;
			}
			HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
			boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
			if (idempotent) {
				// Retry if the request is considered idempotent
				return true;
			}
			return false;
		}

	}

	@Override
	public StaticAD checkStaticAD(long oldAdId) throws ActionException {
		ProtocalWrap protocal = protocalFactory.checkStaticAD(oldAdId);
		byte[] result = requestServiceResource(protocal);
		StaticADHandler staticADHandler = new StaticADHandler();
		staticADHandler.parserXml(result);

		return staticADHandler.getStaticAD();

	}
	@Override
	public Integer commitDownloadRecord(String id, String sign,String ei,String cid)
			throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryCommitDownloadProtocal(id,sign,ei,cid);
		byte[] result = requestServiceResource(protocal);
		CommitDownloadHandler commitDownloadHandler = new CommitDownloadHandler();
		commitDownloadHandler.parserXml(result);
		return new Integer(commitDownloadHandler.getState());
	}
	@Override
	public Integer commitMarketDownloadRecord(Integer id, String sign,String ei)
			throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryCommitMarketDownloadProtocal(id,sign,ei);
		byte[] result = requestServiceResource(protocal);
		CommitDownloadHandler commitDownloadHandler = new CommitDownloadHandler();
		commitDownloadHandler.parserXml(result);
		return new Integer(commitDownloadHandler.getState());
	}
	@Override
	public Integer commitMarketDownloadRecordFirst(int vcode, String sign)
			throws ActionException {
		ProtocalWrap protocal = protocalFactory.queryCommitMarketDownloadFirstProtocal(vcode,sign);
		byte[] result = requestServiceResource(protocal);
		CommitDownloadHandler commitDownloadHandler = new CommitDownloadHandler();
		commitDownloadHandler.parserXml(result);
		return new Integer(commitDownloadHandler.getState());
	}
	@Override
	public List<KeyWord> getSearchKeyword()throws ActionException {
		String sign = SecurityUtil.md5Encode(marketContext.getTs() + Constants.SEC_KEY_STRING);
		ProtocalWrap protocal = protocalFactory.querySearchKeywordProtocal(sign);
		byte[] result = requestServiceResource(protocal);
		SearchKeywordHandler searchKeywordHandler = new SearchKeywordHandler();
		searchKeywordHandler.parserXml(result);
		return searchKeywordHandler.getKeyWordList();
	}
	
}
