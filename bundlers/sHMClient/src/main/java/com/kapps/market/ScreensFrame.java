package com.kapps.market;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemSelectedListener;

import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.MImageType;
import com.kapps.market.cache.AssertCacheManager;
import com.kapps.market.service.ActionException;
import com.kapps.market.task.IResultReceiver;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.MultipleTaskMark;
import com.kapps.market.ui.ScreenPieceView;
import com.kapps.market.util.Constants;

/**
 * 2010-8-11 <br>
 * 应锟斤拷锟斤拷锟斤拷锟斤拷图
 * 
 * @author admin
 * 
 */
public class ScreensFrame extends MarketActivity implements OnItemSelectedListener, IResultReceiver {

	private AppItem appItem;
	private AssertCacheManager assertCacheManager;
	private Gallery gallery;
	private LinearLayout shotmarkList;

	// 小锟斤拷
	private Drawable smallShot;
	private Drawable bigShot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 全锟斤拷
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		assertCacheManager = marketContext.getAssertCacheManager();

		int id = getIntent().getIntExtra(Constants.APP_ID, -9999);
		if (id != 9999) {
			appItem = marketContext.getAppCahceManager().getAppItemById(id);
		}
		// 锟斤拷效
		if (appItem == null || id == 9999) {
			finish();
		}

		setContentView(R.layout.item_screenshot_page);

		gallery = (Gallery) findViewById(R.id.screenshotGallery);
		gallery.setOnItemSelectedListener(this);
		gallery.setHorizontalFadingEdgeEnabled(false);
		String[] screentshotUrls = appItem.getAppDetail().getScreenshots();
		gallery.setAdapter(new ImageAdapter(screentshotUrls));

		int choosedIndex = getIntent().getIntExtra(Constants.CHOOSED_SCREEN_INDEX, 0);
		gallery.setSelection(choosedIndex);

		marketContext.getServiceWraper().forceTakeoverImageScheduleTask(this);

		initShotMarkList(screentshotUrls);
	}

	// 锟斤拷锟斤拷锟绞�
	private void initShotMarkList(String[] screentshotUrls) {
		smallShot = getResources().getDrawable(R.drawable.small_shot);
		bigShot = getResources().getDrawable(R.drawable.big_shot);

		shotmarkList = (LinearLayout) findViewById(R.id.shotMarkList);
		int count = gallery.getAdapter().getCount();
		ImageView imageView = null;
		LinearLayout.LayoutParams layoutParams;
		MultipleTaskMark mTaskMark = new MultipleTaskMark();
		for (int index = 0; index < count; index++) {
			imageView = new ImageView(this);
			if (index == 0) {
				imageView.setImageDrawable(bigShot);
			} else {
				imageView.setImageDrawable(smallShot);
			}
			layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParams.rightMargin = 10;
			shotmarkList.addView(imageView, layoutParams);
			// 锟斤拷锟斤拷使锟斤拷锟剿憋拷锟截伙拷锟芥，锟斤拷锟斤拷锟绞憋拷锟斤拷锟斤拷图片锟窖撅拷锟斤拷锟斤拷锟节达拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟阶懊伙拷械慕锟酵�
			if (gallery.getAdapter().getItem(index) == null) {
				mTaskMark.addSubTaskMark(marketContext.getTaskMarkPool().createAppImageTaskMark(appItem.getId(),
						screentshotUrls[index], MImageType.APP_SCREENSHOT));
			}
		}

		// 锟斤拷锟截斤拷图
		if (mTaskMark.getTaskMarkList().size() > 0) {
			marketContext.getServiceWraper().scheduleAppImageResourceTask(this, mTaskMark, null);
		}
	}

	// 锟斤拷锟斤拷锟酵硷拷锟斤拷锟斤拷锟�
	private class ImageAdapter extends BaseAdapter {
		// 锟斤拷图
		private String[] screentshotUrls;

		public ImageAdapter(String[] screenshotUrls) {
			this.screentshotUrls = screenshotUrls;
		}

		@Override
		public int getCount() {
			return screentshotUrls.length;
		}

		@Override
		public Drawable getItem(int position) {
			return assertCacheManager.getScreenshotsFromCache(screentshotUrls[position], true, false);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				ScreenPieceView view = new ScreenPieceView(ScreensFrame.this);
				view.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				convertView = view;
			}

			// 锟斤拷锟斤拷图片
			ScreenPieceView imageView = ((ScreenPieceView) convertView);
			Drawable drawable = getItem(position);
			if (drawable == null) {
				imageView.setImageDrawable(marketContext.emptyScreenshotLoad);
			} else {
				imageView.setImageDrawable(drawable);
			}

			return convertView;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.task.IResultReceiver#receiveResult(com.ck.market.
	 * task.mark.ATaskMark, com.ck.market.service.ActionException)
	 */
	@Override
	public void receiveResult(ATaskMark taskMark, ActionException exception, Object trackerResult) {
		if (taskMark.getTaskStatus() == ATaskMark.HANDLE_OVER) {
			((BaseAdapter) (gallery.getAdapter())).notifyDataSetChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android
	 * .widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		int childCount = shotmarkList.getChildCount();
		ImageView imageView = null;
		for (int index = 0; index < childCount; index++) {
			imageView = (ImageView) shotmarkList.getChildAt(index);
			if (index == position) {
				imageView.setImageDrawable(bigShot);
			} else {
				imageView.setImageDrawable(smallShot);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android
	 * .widget.AdapterView)
	 */
	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.logoutMenuItem).setVisible(false);
		menu.findItem(R.id.closeMenuItem).setVisible(false);
		return super.onPrepareOptionsMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		gallery = null;
		bigShot = null;
		smallShot = null;
		shotmarkList = null;
		System.gc();

		super.onDestroy();
	}

	@Override
	protected void initCustomActionbar() {
		// TODO Auto-generated method stub
		
	}

}
