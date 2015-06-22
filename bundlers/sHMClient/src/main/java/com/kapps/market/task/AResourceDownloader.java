package com.kapps.market.task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

import com.kapps.market.MApplication;
import com.kapps.market.NetworkinfoParser;
import com.kapps.market.bean.DownloadItem;
import com.kapps.market.log.LogUtil;
import com.kapps.market.service.impl.HttpMarketService;
import com.kapps.market.util.Constants;
import com.kapps.market.util.Util;

/**
 * 2010-12-7<br>
 * ���ظ���
 * 
 * @author shuizhu
 * 
 */
public abstract class AResourceDownloader {

	public static final String TAG = "AResourceDownloader";

	protected DefaultHttpClient httpClient = null;
	// �Ƿ�ȡ������
	protected boolean cancelDownload;
	// �Ƿ�ֹͣ����
	protected boolean stopDownload;
	protected BufferedOutputStream bos;
	// ��ǰҪ�����ֽ�
	protected int skipSize;
	protected IDownloadProgress downloadProgress;
	protected MApplication imContext;

	/**
	 * @param downloadProgress
	 */
	public AResourceDownloader(MApplication imContext, IDownloadProgress downloadProgress) {
		this.imContext = imContext;
		this.downloadProgress = downloadProgress;

	}

	/**
	 * �������ʱ������host·��
	 */
	private String getItemGetPath(DownloadItem downloadItem, String sign) {
		String getPath = null;
		if (downloadItem.getHostPath() != null && downloadItem.getHostPath().trim().length() != 0) {
			getPath = downloadItem.getHostPath();

		} else {
			getPath = HttpMarketService.BASE_URL;
		}
		getPath += downloadItem.getApkPath() + "&sign=" + sign + "&dhid=" + imContext.getContextConfig().getDeviceId()
				+ "&chid=" + imContext.getContextConfig().getCid()+"&ver=1";
		if (LogUtil.download_debug) {
			Log.d(TAG, "res host path: " + getPath);
		}
		return getPath;
	}

	/**
	 * ������Դ
	 * 
	 * @param downloadItem
	 *            ������
	 * @param sign
	 *            ��ȫ��ʶ
	 * @param ts
	 *            key
	 * @throws Exception
	 */
	public void downloadResource(DownloadItem downloadItem, String sign, String ts) throws Exception {
		try {
			// ���ر����ļ��������һЩ��ϢgetRealDownloadUrlҪ�á�
			File saveFile = preHandle(downloadItem);

			// pv 2.0 ��ʼ��Դ����ʹ��cdn �ض���ʽ�����������Ȼ���ض���ĵ�ַ������android httpclient �汾
			// �ض����ǲ�����ͷ����Ϣ�������������Լ������ض����ĵ�ַ
			String realDownloadUrl = getRealDownloadUrl(downloadItem, sign, ts);
			if (realDownloadUrl == null) {
				throw new Exception("can not find realDownloadUrl");

			} else if (LogUtil.download_debug) {
				Log.d(TAG, "realDownloadUrl: " + realDownloadUrl);
			}

			// ��������
			httpClient = NetworkinfoParser.getHttpConnector(imContext);
			httpClient.setHttpRequestRetryHandler(new RetryHandler()); // ����
			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 45000); // ���ӳ�ʱ

			// ��������
			handleRequest(saveFile, downloadItem, realDownloadUrl);

		} catch (Exception ex) {
			throw ex;

		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
				}
			}
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}

	// ���ʵ�ʵ����ص�ַ
	private String getRealDownloadUrl(DownloadItem downloadItem, String sign, String ts) throws Exception {
		DefaultHttpClient httpClient = null;
		try {
			// ��������
			httpClient = NetworkinfoParser.getHttpConnector(imContext);
			httpClient.setHttpRequestRetryHandler(new RetryHandler()); // ����
			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 45000); // ���ӳ�ʱ

			String orgialGetUrl = getItemGetPath(downloadItem, sign);
			HttpGet httpGet = new HttpGet(orgialGetUrl);
			httpGet.addHeader(Constants.H_SER_TS, ts);
			// ������ȷ���Ƿ�Ҫ�������ؼ�¼��ע��downloadItem��dsizeû�г־û���
			httpGet.addHeader(Constants.H_RESUME_DOWNLOAD, skipSize > 0 ? Constants.DRESUME_YES : Constants.DRESUME_NO);
			httpGet.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000); // ��ȡ��ʱ
			HttpClientParams.setRedirecting(httpGet.getParams(), false);

			if (LogUtil.download_debug) {
				Header[] heads = httpGet.getAllHeaders();
				Log.v(TAG, "getRealDownloadUrl \n httpGet.getAllHeaders()------------ \n");
				for (Header header : heads) {
					Log.v(TAG, "header: " + header + "\n");
				}
			}

			// ִ��
			HttpResponse httpResponse = httpClient.execute(httpGet);

			if (LogUtil.download_debug) {
				Header[] heads = httpResponse.getAllHeaders();
				Log.v(TAG, "getRealDownloadUrl \n httpResponse.getAllHeaders()------------ \n");
				for (Header header : heads) {
					Log.v(TAG, "header: " + header + "\n");
				}
			}

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			switch (statusCode) {
			case HttpStatus.SC_MOVED_TEMPORARILY:
			case HttpStatus.SC_MOVED_PERMANENTLY:
			case HttpStatus.SC_SEE_OTHER:
			case HttpStatus.SC_TEMPORARY_REDIRECT:
				return httpResponse.getLastHeader("location").getValue();
			}
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}

		return null;
	}

	// Ԥ����
	protected File preHandle(DownloadItem downloadItem) {
		// �ȼ���Ƿ��Ѿ����ز�����
		File file = new File(downloadItem.getSavePath());
		if (file.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				skipSize = fis.available();

			} catch (Exception e) {
			} finally {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}

		if (LogUtil.download_debug) {
			Log.v(TAG,
					"do download: apkPath: " + downloadItem.getSavePath() + "\nremote path: "
							+ downloadItem.getApkPath() + "\n version: " + downloadItem.getVersion()
							+ "\nfile.exists(): " + file.exists() + " \n skipSize: " + skipSize);
		}
		return file;
	}

	/**
	 * ������Ӧ ��������
	 * 
	 * @param saveFile
	 * @param downloadUrl
	 *            pv2.0 �Ժ���Դ��ʱ��̨�ض���Ϊ��̬url
	 * @param downloadItem
	 * @throws Exception
	 */
	protected abstract void handleRequest(File saveFile, DownloadItem downloadItem, String downloadUrl)
			throws Exception;

	/**
	 * ������Ӧ
	 * 
	 * @param httpResponse
	 * @param downloadItem
	 * 
	 * @return ʣ��ĳ���
	 * 
	 * @throws Exception
	 */
	protected int handleResponse(HttpResponse httpResponse, DownloadItem downloadItem) throws Exception {
		int httpCode = httpResponse.getStatusLine().getStatusCode();
		if (LogUtil.download_debug) {
			Header[] heads = httpResponse.getAllHeaders();
			Log.v(TAG, "httpResponse.getAllHeaders()----------------------- \n");
			for (Header header : heads) {
				Log.v(TAG, "header: " + header + "\n");
			}
			Log.v(TAG, "http response httpCode: " + httpCode);
		}

		// 200: �� 206����
		// ���������ܳ��ȣ��ļ����ܳ���
		// ��̬��ַ����ʱhttp��Ӧ��������;�Ȼ��"text/plain"
		int totalBytes = -2, fileTotalBytes = -1;
		if ((httpCode == HttpStatus.SC_PARTIAL_CONTENT || httpCode == HttpStatus.SC_OK)) {
			HttpEntity entity = httpResponse.getEntity();
			Header h = httpResponse.getFirstHeader("Content-Length");
			int contentLength = Integer.valueOf(h.getValue());
			// ���ݳ��ȺϷ�������
			if (contentLength > 0) {
				// ע����������ճ��ܳ��ȴ����ļ�ʵ�ʳ��ȣ�����totalBytes����Ӱ�������߼���
				totalBytes = contentLength + skipSize;
				h = httpResponse.getFirstHeader("Content-Range");
				if (h != null) {
					fileTotalBytes = (Integer.valueOf(h.getValue().split("/")[1]));
				} else {
					fileTotalBytes = totalBytes;
				}
				int downloadedBytes = skipSize;

				if (LogUtil.download_debug) {
					Log.v(TAG, "software: " + downloadItem.getPackageName() + " skipSize: " + skipSize
							+ " totalBytes: " + totalBytes + "  fileTotalBytes: " + fileTotalBytes);
				}
				InputStream is = entity.getContent();
				byte[] datas = new byte[8192];
				int count = -1;

				// ����apk�ļ�"��С"�ٽ���ж�
				int perSize = Util.computeApkFileProgressCritcalSize(totalBytes);
				
				int showProgrossCount = 1;

				// Ҫȷ���Ƿ������Ѿ���ȡ��
				while ((count = is.read(datas, 0, datas.length)) != -1) {
					if (isCancelDownload() || isStopDownload()) {
						throw new IllegalStateException("download task be stop.");

					} else {
						bos.write(datas, 0, count);
						downloadedBytes += count;
						if ((downloadedBytes / perSize) >= showProgrossCount || downloadedBytes == totalBytes) {
							showProgrossCount++;
							// ֪ͨ���
							downloadProgress.receiveProgress(downloadItem, downloadedBytes);
						}
					}
				}
			}

		} else {
			throw new Exception("can not download apk: bad response");
		}

		return (fileTotalBytes - totalBytes);
	}

	/**
	 * @return the cancelDownload
	 */
	public boolean isCancelDownload() {
		return cancelDownload;
	}

	/**
	 * ȡ������
	 */
	public void cancelDownload() {
		this.cancelDownload = true;

		if (bos != null) {
			try {
				bos.close();
			} catch (IOException e) {
			}
		}
		if (httpClient != null) {
			try {
				httpClient.getConnectionManager().shutdown();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	public boolean isStopDownload() {
		return stopDownload;
	}

	/**
	 * ֹͣ����
	 */
	public void stopDownload() {
		this.stopDownload = true;

		if (bos != null) {
			try {
				bos.close();
			} catch (IOException e) {
			}
		}
		if (httpClient != null) {
			try {
				httpClient.getConnectionManager().shutdown();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	/**
	 * ����ִ�н��<br>
	 * 2010-12-7
	 * 
	 * @author shuizhu
	 * 
	 */
	public static interface IDownloadProgress {

		/**
		 * �������ؽ��
		 * 
		 * @param downloadItem
		 *            ������
		 * @param downloadedBytes
		 *            ��ǰ���ص�����
		 */
		public void receiveProgress(DownloadItem downloadItem, int downloadedBytes);
	}

	// ���Դ�����, ��������3�Ρ�
	private class RetryHandler implements HttpRequestRetryHandler {

		@Override
		public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
			if (LogUtil.download_debug) {
				Log.v(TAG, "download app retry: executionCount: " + executionCount + " exception:" + exception);
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
}
