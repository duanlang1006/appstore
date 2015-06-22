package com.kapps.market;

import java.util.LinkedHashMap;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.kapps.market.ui.CommonView;
import com.kapps.market.ui.search.AppSearchPage;
import com.kapps.market.util.Constants;

/**
 * 2010-6-15 锟斤拷同锟斤拷contentFrame锟结构
 * 
 * @author admin
 * 
 */
public abstract class TabableActivity extends MarketActivity implements OnClickListener {

	public static final String TAG = "TabableActivity";

	// 锟斤拷图锟斤拷锟斤拷,希锟斤拷锟斤拷锟斤拷锟斤拷锟�
	// key: 锟斤拷锟斤拷选锟斤拷锟斤拷锟酵�value:锟斤拷示锟斤拷view
	// 锟矫伙拷锟斤拷锟斤拷锟剿帮拷钮锟斤拷时锟斤拷锟斤拷锟斤拷锟窖★拷锟斤拷锟斤拷图锟斤拷锟斤拷顺锟斤拷锟叫伙拷
	private LinkedHashMap<Integer, View> viewCache = new LinkedHashMap<Integer, View>();
	// 锟斤拷始锟斤拷锟斤拷锟揭�
	protected FrameLayout contentFrame = null;
	// 锟斤拷前锟斤拷示锟斤拷锟斤拷图锟斤拷示/锟酵达拷锟斤拷锟斤拷
	private int currentMark = Constants.NONE_VIEW;

	/**
	 * 注锟斤拷拥锟叫达拷锟斤拷锟斤拷图锟斤拷示锟斤拷view同时锟斤拷锟�锟斤拷锟斤拷锟铰硷拷锟斤拷锟斤拷锟�
	 * 
	 * @param trigger
	 */
	protected final void registerTrigger(View trigger) {
		trigger.setOnClickListener(this);
	}

	/**
	 * 注锟斤拷拥锟叫达拷锟斤拷锟斤拷图锟侥憋拷恰锟�br>
	 * 默锟斤拷注锟斤拷锟斤拷锟酵硷拷锟斤拷潜锟斤拷锟斤拷锟侥★拷<br>
	 * 锟斤拷锟津不伙拷锟斤拷
	 * 
	 * @param viewMark
	 */
	protected final void registerView(int viewMark) {
		viewCache.put(viewMark, null);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		// 锟斤拷锟斤拷锟斤拷锟�
		//contentFrame = (FrameLayout) findViewById(R.id.contentFrame);
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		// 锟斤拷锟斤拷锟斤拷锟�
		//contentFrame = (FrameLayout) findViewById(R.id.contentFrame);
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		super.setContentView(view, params);
		// 锟斤拷锟斤拷锟斤拷锟�
		//contentFrame = (FrameLayout) findViewById(R.id.contentFrame);
	}

	/**
	 * 锟斤拷玫锟角帮拷锟酵硷拷锟街�
	 * 
	 * @return
	 */
	public int getCurrentTabMark() {
		return currentMark;
	}

	/**
	 * 锟斤拷玫锟角帮拷锟绞撅拷锟斤拷锟酵�
	 * 
	 * @return
	 */
	public View getCurrentTab() {
		if (contentFrame != null && contentFrame.getChildCount() > 0) {
			return contentFrame.getChildAt(0);
		} else {
			return null;
		}
	}

	/*
	 * just get, not create, may return @null.
	 */
	protected View getViewByMarkFromCache(int viewMark) {
		View cacheView = viewCache.get(viewMark);
		if (cacheView == null) {
			cacheView = createContentView(viewMark);
			if (viewCache.containsKey(viewMark)) {
				cacheView(viewMark, cacheView);
			}
		}
		return cacheView;
	}

	public void hideContentShowViewPager() {
		if (contentFrame != null) {
			contentFrame.setVisibility(View.GONE);
		}
	}

	
	/**
	 * 锟斤拷始锟斤拷锟斤拷应锟斤拷锟斤拷图, 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟绞憋拷某锟绞硷拷锟斤拷锟�锟斤拷锟斤拷view锟斤拷hashcode锟斤拷为锟斤拷识锟斤拷
	 * 
	 * @param triggerView
	 *            谁锟斤拷锟斤拷锟斤拷要锟斤拷示锟斤拷锟斤拷锟酵�
	 */
	protected View showChoosedView(int viewMark) {
		boolean firstCreate = false;

		View cacheView = viewCache.get(viewMark);
		if (cacheView == null) {
			firstCreate = true;
			cacheView = createContentView(viewMark);
			// 锟斤拷锟街帮拷锟矫伙拷锟阶拷锟斤拷锟津不伙拷锟斤拷
			if (viewCache.containsKey(viewMark)) {
				cacheView(viewMark, cacheView);
			}
		}

		// 前
		onBeforeShowView(viewMark);

		// 确锟斤拷锟角凤拷锟窖撅拷锟斤拷锟斤拷示锟斤拷要锟斤拷页锟斤拷锟斤拷
		View currentView = getCurrentTab();
		if (currentView == null || currentView != cacheView) {
			if (currentView instanceof CommonView) {
				((CommonView) currentView).releaseView();
			}

			currentMark = viewMark;

			//for show view!!
			if (contentFrame != null) {
				contentFrame.removeAllViews();
				contentFrame.addView(cacheView);
			}

			// 锟斤拷应每锟轿讹拷锟斤拷锟斤拷锟斤拷锟竭碉拷一锟斤拷锟斤拷时锟角诧拷锟斤拷要锟斤拷锟斤拷flush锟侥★拷
			if (!firstCreate && (cacheView instanceof CommonView)) {
//				if(!(cacheView instanceof AppSearchPage))
//						clearsearch();//search page have removed.
				((CommonView) cacheView).flushView(Constants.NONE_VIEW);
			}
		} else {
			if(currentView instanceof AppSearchPage)
				((CommonView) currentView).flushView(Constants.NONE_VIEW);
			Log.d(TAG, "already choose View");
		}

		// 锟斤拷
		onAfterShowView(viewMark);
		return cacheView;
	}

	/**
	 * 锟斤拷锟斤拷囟锟斤拷锟绞碉拷锟秸︼拷锟斤拷锟揭拷锟斤拷锟阶刺拷锟斤拷锟斤拷瓤锟斤拷锟斤拷诖锟斤拷锟侥碉拷时锟斤拷<br>
	 * 锟斤拷锟斤拷锟斤拷愿锟斤拷谴朔锟斤拷锟斤拷锟斤拷锟绞碉拷郑锟斤拷锟斤拷锟矫匡拷味锟斤拷锟斤拷锟斤拷锟斤拷碌锟斤拷锟酵硷拷锟斤拷瞥锟斤拷锟斤拷图锟斤拷 锟斤拷锟秸★拷
	 * 
	 * @param viewMark
	 * @param view
	 */
	protected void cacheView(int viewMark, View view) {
		viewCache.put(viewMark, view);
	}

	/*
	 * (non-Javadoc) 锟斤拷锟斤拷锟斤拷录锟斤拷锟斤拷锟街伙拷锟斤拷锟斤拷图锟斤拷选锟斤拷锟斤拷锟斤拷锟斤拷锟�锟斤拷锟角ｏ拷锟斤拷锟斤拷锟斤拷锟饺碉拷锟矫革拷锟斤拷姆锟斤拷锟斤拷锟�
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View trigger) {
		int viewMark = getShowViewMark(trigger);
		// 确锟斤拷锟斤拷图确实锟斤拷锟斤拷
		if (viewMark != Constants.NONE_VIEW) {
			showChoosedView(viewMark);
		}
	}

	/**
	 * @return the viewCache
	 */
	protected LinkedHashMap<Integer, View> getViewCache() {
		return viewCache;
	}

	/**
	 * 锟斤拷示指锟斤拷锟斤拷图前锟斤拷锟斤拷锟斤拷什么
	 */
	protected void onBeforeShowView(int viewMark) {

	}

	/**
	 * 锟斤拷示指锟斤拷锟斤拷图锟斤拷锟斤拷锟斤拷什么
	 */
	protected void onAfterShowView(int viewMark) {

	}
	
	/**
	 * 锟斤拷么锟斤拷锟斤拷锟斤拷锟揭拷锟斤拷锟酵�
	 * 
	 * @param trigger
	 * @return
	 */
	protected abstract int getShowViewMark(View trigger);

	/**
	 * 锟缴撅拷锟斤拷锟斤拷锟洁创锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷图
	 * 
	 * @return
	 */
	protected abstract View createContentView(int viewMark);

}
