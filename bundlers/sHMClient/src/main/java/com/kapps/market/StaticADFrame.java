package com.kapps.market;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.kapps.market.bean.MImageType;
import com.kapps.market.bean.StaticAD;
import com.kapps.market.cache.AssertCacheManager;
import com.kapps.market.cache.CacheConstants;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppImageTaskMark;
import com.kapps.market.task.mark.AppQuickDownloadTaskMark;

/**
 * 
 * @author shuizhu
 * 
 */
public class StaticADFrame extends MarketActivity implements IResultReceiver, OnClickListener {
	// ��Դ
	private AssertCacheManager assertCacheManager;
	// ��̬�����Ϣ
	private StaticAD staticAD;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.static_ad);

		assertCacheManager = marketContext.getAssertCacheManager();

		initFrame();
	}

	private void initFrame() {
		staticAD = (StaticAD) marketContext.getLocaleCacheManager().getDataFormCache(CacheConstants.STATIC_AD_INFO);
		if (staticAD != null) {
			marketContext.getLocaleCacheManager().deleteCacheData(CacheConstants.STATIC_AD_INFO);

			TextView infoLabel = (TextView) findViewById(R.id.titleLabel);
			ImageView iconLabel = (ImageView) findViewById(R.id.iconLabel);
			// ͼ��
			Drawable icon = assertCacheManager.getAppIconFromCache(staticAD.getId());
			if (icon == null) {
				iconLabel.setBackgroundDrawable(marketContext.emptyAppIcon);
				AppImageTaskMark imageTaskMark = taskMarkPool.createAppImageTaskMark(staticAD.getAid(),
						staticAD.getIconUrl(), MImageType.APP_ICON);
				serviceWraper.getAppImageResource(this, imageTaskMark, null, staticAD.getAid(), staticAD.getIconUrl(),
						MImageType.APP_ICON);

			} else {
				iconLabel.setBackgroundDrawable(icon);
			}

			infoLabel.setText(staticAD.getName());

			try {
				infoLabel = (TextView) findViewById(R.id.desLabel);
				String tempDes = staticAD.getDes();
				SpannableString sp = new SpannableString(tempDes);
				Pattern pattern = Pattern.compile("himarket:\\S*");
				Matcher matcher = pattern.matcher(sp);
				if (matcher.find()) {
					int start = matcher.start();
					int end = matcher.end();
					String linkedStr = tempDes.substring(start, end);
					// ���ó�����
					sp.setSpan(new URLSpan(linkedStr), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				pattern = Pattern.compile("http:\\S*");
				matcher = pattern.matcher(sp);
				if (matcher.find()) {
					int start = matcher.start();
					int end = matcher.end();
					String linkedStr = tempDes.substring(start, end);
					// ���ó�����
					sp.setSpan(new URLSpan(linkedStr), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				infoLabel.setText(sp);
				// ����TextView�ɵ��
				infoLabel.setMovementMethod(LinkMovementMethod.getInstance());

				// ע�����ذ�ť����¼�
				findViewById(R.id.quickAdDownload).setOnClickListener(this);
				// ע��ȡ��ť����¼�
				findViewById(R.id.cancelBtn).setOnClickListener(this);

			} catch (Exception e) {
				e.printStackTrace();

			}

		} else {
			finish();

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.quickAdDownload:
			AppQuickDownloadTaskMark quickDownloadTaskMark = taskMarkPool.createAppQuickDownloadTaskMark(staticAD
					.getAid());
			marketContext.getServiceWraper().quickDownloadApp(null, quickDownloadTaskMark, staticAD.getAid());
			finish();

			break;

		case R.id.cancelBtn:
			finish();

			break;

		default:
			break;

		}

	}

	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		if (taskMark instanceof AppImageTaskMark) {
			if (taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
				Drawable icon = assertCacheManager.getAppIconFromCache(staticAD.getAid(), true);
				if (icon != null) {
					ImageView iconLabel = (ImageView) findViewById(R.id.iconLabel);
					iconLabel.setBackgroundDrawable(icon);
				}
			}
		}

	}

	@Override
	protected void initCustomActionbar() {
		// TODO Auto-generated method stub
		
	}

}
