package com.kapps.market.task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.CoreConnectionPNames;
import android.util.Log;

import com.kapps.market.MApplication;
import com.kapps.market.bean.DownloadItem;
import com.kapps.market.log.LogUtil;

/**
 * 2010-12-7<br>
 * ��ͨ����
 * 
 * @author shuizhu
 * 
 */
public class NetApkDownloader extends AResourceDownloader {

	/**
	 * @param downloadProgress
	 */
	public NetApkDownloader(MApplication imContext, IDownloadProgress downloadProgress) {
		super(imContext, downloadProgress);
	}

	@Override
	protected void handleRequest(File saveFile, DownloadItem downloadItem, String downloadUrl) throws Exception {
		// �ϳ����������·��
		HttpGet httpGet = new HttpGet(downloadUrl);
		httpGet.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000); // ��ȡ��ʱ

		if (skipSize > 0) {
			httpGet.addHeader("RANGE", "bytes=" + skipSize + "-");
			bos = new BufferedOutputStream(new FileOutputStream(saveFile, true), 8192);

		} else {
			bos = new BufferedOutputStream(new FileOutputStream(saveFile), 8192);
		}

		if (LogUtil.download_debug) {
			Header[] heads = httpGet.getAllHeaders();
			Log.v(TAG, "httpGet.getAllHeaders()------------ \n");
			for (Header header : heads) {
				Log.v(TAG, "header: " + header + "\n");
			}
		}

		// ������Ӧ
		HttpResponse httpResponse = httpClient.execute(httpGet);
		handleResponse(httpResponse, downloadItem);
	}
}
