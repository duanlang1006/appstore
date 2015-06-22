package com.kapps.market.task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.CoreConnectionPNames;
import android.util.Log;

import com.kapps.market.MApplication;
import com.kapps.market.NetworkinfoParser;
import com.kapps.market.bean.DownloadItem;
import com.kapps.market.log.LogUtil;

/**
 * 2010-12-7<br>
 * WAP����<br>
 * �����Ƭ�������
 * 
 * @author shuizhu
 * 
 */
public class WapApkDownloader extends AResourceDownloader {

	// 150k
	public static final int PIECE = 1024 * 150;

	/**
	 * @param downloadProgress
	 */
	public WapApkDownloader(MApplication imContext, IDownloadProgress downloadProgress) {
		super(imContext, downloadProgress);
	}

	@Override
	protected void handleRequest(File saveFile, DownloadItem downloadItem, String downloadUrl) throws Exception {
		bos = new BufferedOutputStream(new FileOutputStream(saveFile, true), 8192);
		int restByte = 1;
		int length = getProperPieceSize();
		if (LogUtil.download_debug) {
			Log.v(TAG, "handleRequest piece: " + length + " saveFile: " + saveFile + " downloadUrl: " + downloadUrl);
		}
		while (restByte > 0) {
			// �ϳ����������·��
			HttpGet httpGet = new HttpGet(downloadUrl);
			httpGet.addHeader("RANGE", "bytes=" + skipSize + "-" + (skipSize + length));
			httpGet.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000); // ��ȡ��ʱ
			if (LogUtil.download_debug) {
				Header[] heads = httpGet.getAllHeaders();
				Log.v(TAG, "httpGet.getAllHeaders()------------ \n");
				for (Header header : heads) {
					Log.v(TAG, "header: " + header + "\n");
				}
			}

			// ������Ӧ
			HttpResponse httpResponse = httpClient.execute(httpGet);
			restByte = handleResponse(httpResponse, downloadItem);

			// ������һƬ �� begin <= range <= end��
			skipSize = skipSize + length + 1;
			if (PIECE > restByte) {
				length = restByte - 1;

			} else {
				length = PIECE;
			}

			httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.MILLISECONDS);
			Thread.sleep(3000);
		}
	}

	// ��ú��ʵķ�Ƭ��С
	// �ƶ�3g wap -> HSDPA: 1w
	// ��ͨwap -> 150k
	private int getProperPieceSize() {
		int piece = PIECE;
		String subTypeName = NetworkinfoParser.getNetSubTypeName(imContext);
		if (subTypeName != null && subTypeName.contains("HSDPA")) {
			piece = 1024 * 300;
		}
		return piece;
	}

}
