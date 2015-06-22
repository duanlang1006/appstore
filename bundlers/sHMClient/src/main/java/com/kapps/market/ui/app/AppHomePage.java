package com.kapps.market.ui.app;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.kapps.market.R;
import com.kapps.market.bean.PageInfo;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppAdvertiseTaskMark;
import com.kapps.market.task.mark.AppListTaskMark;
import com.kapps.market.ui.CommonView;
import com.kapps.market.ui.TabableAppView;
import com.kapps.market.util.Constants;
import com.kapps.market.util.ResourceEnum;

/**
 * market锟斤拷页 锟斤拷锟斤拷页锟斤拷锟绞碉拷锟绞癸拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟轿拷锟酵硷拷锟斤拷锟斤拷汀锟�
 * 
 * @author admin
 * 
 */
public class AppHomePage extends TabableAppView {

	public static final String TAG = "AppHomePage";
	public  boolean is_move=false;
	public float pre_x,pre_y=0;;
	public static final int VIEW_TAB1_TYPE = 0;
	public static final int VIEW_TAB2_TYPE = 1;
	public static final int VIEW_TAB3_TYPE = 2;

	// 锟斤拷锟斤拷
	private Button tab1Button, tab2Button, tab3Button;
	/**
	 * @param context
	 */
	public AppHomePage(Context context) {
		super(context);
		addView(R.layout.m_home_page);
		

		// 锟斤拷始锟斤拷锟斤拷锟脚ワ拷锟斤拷虻サ锟斤拷锟斤拷锟阶拷锟斤拷锟斤拷锟斤拷锟皆憋拷锟斤拷锟斤拷图锟斤拷时锟斤拷
		// 锟斤拷锟斤拷锟斤拷图锟斤拷选锟斤拷
		// 每锟斤拷锟斤拷锟斤拷约锟揭伙拷锟揭筹拷锟
		registerView(VIEW_TAB1_TYPE);
		registerView(VIEW_TAB2_TYPE);
		registerView(VIEW_TAB3_TYPE);

		tab1Button = (Button) findViewById(R.id.tab1Button);
		registerTrigger(tab1Button);
		tab2Button = (Button) findViewById(R.id.tab2Button);
		registerTrigger(tab2Button);
		tab3Button = (Button) findViewById(R.id.tab3Button);
		registerTrigger(tab3Button);

		//by shuiz
		/*current requirement is unknown, just comment category.*/
		findViewById(R.id.funcList).setVisibility(View.GONE);
		
		// 默锟斤拷选锟斤拷锟揭伙拷锟
		showChoosedView(VIEW_TAB1_TYPE);
	}

	/*
	 * 
	 * 锟斤拷图锟斤拷羌锟轿︼拷没锟斤拷锟斤拷恰锟�br> 锟斤拷锟斤拷实锟斤拷锟斤拷每锟轿讹拷锟斤拷锟斤拷锟铰★拷<br> 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷只锟斤拷锟斤拷锟斤拷一锟轿★拷
	 */
	@Override
	protected View createContentView(int viewMark) {
		switch (viewMark) {
		case VIEW_TAB1_TYPE:
			AppListView pickAppListView = new AppPickListView(getContext());
			pickAppListView.initLoadleList(marketContext.getTaskMarkPool().getAppAdvertiseTaskMark(
					ResourceEnum.AD_TYPE_EXCEL));
			return pickAppListView;

		case VIEW_TAB2_TYPE:
			AppListView dayHostListView = new DayHostListView(getContext());
			dayHostListView.initLoadleList(marketContext.getTaskMarkPool().getAppListTaskMark(
					ResourceEnum.SORT_DAY_DOWNLOAD, ResourceEnum.FEE_NONE_TYPE));
			return dayHostListView;

		case VIEW_TAB3_TYPE:
			AppListView newestListView = new NewestAppListView(getContext());
			newestListView.initLoadleList(marketContext.getTaskMarkPool().getAppListTaskMark(
					ResourceEnum.SORT_ADD_TIME_DESC, ResourceEnum.FEE_NONE_TYPE));
			return newestListView;

		default:
			return null;
		}
	}

    @Override
    public void flushView(int what) {
        CommonView cv = (CommonView)getTabByMarkFromCache(VIEW_TAB1_TYPE);
        if (cv!= null) {
            cv.flushView(what);
        }
    }

    @Override
	protected void onSetButton(int mark, boolean selected) 
	{
		switch (mark) {
		case VIEW_TAB2_TYPE:
			tab2Button.setSelected(selected);
			break;

		case VIEW_TAB3_TYPE:
			tab3Button.setSelected(selected);
			break;

		case VIEW_TAB1_TYPE:
			tab1Button.setSelected(selected);
			break;
		}
	}
	@Override
	protected void onBeforeShowView(int viewMark, Object data) {
		int oldMark = getCurrentTabMark();
		setButtonSelected(oldMark, false);
		setButtonSelected(viewMark, true);
	}

	private void setButtonSelected(int mark, boolean selected) {
		switch (mark) {
		case VIEW_TAB2_TYPE:
			tab2Button.setSelected(selected);
			break;

		case VIEW_TAB3_TYPE:
			tab3Button.setSelected(selected);
			break;

		case VIEW_TAB1_TYPE:
			tab1Button.setSelected(selected);
			break;
		}
	}

	/*
	 * (non-Javadoc) 锟斤拷图锟斤拷羌锟轿︼拷没锟斤拷锟斤拷锟
	 * 
	 * @see 锟斤拷图锟斤拷羌锟斤拷腔锟斤拷锟斤拷志
	 */
	@Override
	protected int getShowViewMark(View trigger) {
		switch (trigger.getId()) {
		case R.id.tab1Button:
			return VIEW_TAB1_TYPE;

		case R.id.tab2Button:
			return VIEW_TAB2_TYPE;

		case R.id.tab3Button:
			return VIEW_TAB3_TYPE;

		default:
			return Constants.NONE_VIEW;
		}
	}

	// 锟斤拷选锟叫憋拷
	private class AppPickListView extends AppListView {

		/**
		 * @param context
		 */
		public AppPickListView(Context context) {
			super(context, true);
		}

		@Override
		protected void handleLoadNewItems(ATaskMark taskMark) {
			// 锟矫讹拷锟狡硷拷
			AppAdvertiseTaskMark excelTaskMark = (AppAdvertiseTaskMark) taskMark;
			PageInfo pageInfo = excelTaskMark.getPageInfo();
			// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
			serviceWraper.getAppAdvertiseByType(this, excelTaskMark, excelTaskMark.getPopType(),
					pageInfo.getNextPageIndex(), pageInfo.getPageSize());
		}

        @Override
        public void flushView(int what) {
            notifyDataSetInvalid();
        }
    }

	// 锟斤拷锟斤拷锟斤拷锟斤拷
	private class DayHostListView extends AppListView {

		/**
		 * @param context
		 */
		public DayHostListView(Context context) {
			super(context, true);
		}

		@Override
		protected void handleLoadNewItems(ATaskMark taskMark) {
			AppListTaskMark listTaskMark = (AppListTaskMark) taskMark;
			PageInfo pageInfo = listTaskMark.getPageInfo();
			serviceWraper.getAppListByTopDownloadFirstPage(this, listTaskMark, listTaskMark.getSortType(),
					pageInfo.getNextPageIndex(), pageInfo.getPageSize());

		}
	}

	// 锟斤拷锟斤拷锟较硷拷
	private class NewestAppListView extends AppListView {

		/**
		 * @param context
		 */
		public NewestAppListView(Context context) {
			super(context, true);
		}

		@Override
		protected void handleLoadNewItems(ATaskMark taskMark) {
			AppListTaskMark listTaskMark = (AppListTaskMark) taskMark;
			PageInfo pageInfo = listTaskMark.getPageInfo();
			serviceWraper.getNewsAppList(this, listTaskMark, pageInfo.getNextPageIndex(), pageInfo.getPageSize());
		}
	}
	public boolean handleScroll(MotionEvent event)
	{
		float x=event.getX();
		float y=event.getY();
		float distx=x-pre_x;
		float disty=y-pre_y;
		int cur=getCurrentTabMark();
		if(distx<0&&Math.abs(distx)>80)
		{
			switch(cur)
			{
			case 0:
				showChoosedView(1);
				break;
			case 1:
				showChoosedView(2);
				break;
			case 2:
				showChoosedView(0);
				break;
			}
			return true;
		}
		if(distx>0&&Math.abs(distx)>80)
		{
			switch(cur)
			{
			case 0:
				showChoosedView(2);
				break;
			case 1:
				showChoosedView(0);
				break;
			case 2:
				showChoosedView(1);
				break;
			}
			return true;
		}
		return false;
	}
//	@Override
//	public boolean dispatchTouchEvent(MotionEvent event)
//	{
//		int action = event.getAction();
//		switch(action)
//		{
//		case MotionEvent.ACTION_DOWN:
//			pre_x=event.getX();
//			pre_y=event.getY();
//			break;
//		case MotionEvent.ACTION_MOVE:
//			is_move=true;
////			scrollBy(20, 0);
//			break;
//		case MotionEvent.ACTION_UP:
//			if(is_move)
//			{
//				is_move=false;
//				if(!handleScroll(event))
//					return super.dispatchTouchEvent(event);
//				return true;
//			}
//			
//		}
//		return super.dispatchTouchEvent(event);
//		
//	}
}
