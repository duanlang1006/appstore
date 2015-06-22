package com.kapps.market.ui.app;

import android.content.Context;
import android.view.View;

import com.kapps.market.R;
import com.kapps.market.bean.AppCategory;
import com.kapps.market.bean.PageInfo;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppListTaskMark;
import com.kapps.market.ui.CommonView;
import com.kapps.market.ui.ConverFlowBase;
import com.kapps.market.ui.TabableAppView;
import com.kapps.market.util.Constants;
import com.kapps.market.util.ResourceEnum;

/**
 * market锟斤拷页 锟斤拷锟斤拷页锟斤拷锟绞碉拷锟绞癸拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟轿拷锟酵硷拷锟斤拷锟斤拷汀锟�
 * 
 * @author admin
 * 
 */
public class AppOfDailyCategoryPage extends TabableAppView {

	public static final String TAG = "AppOfDailyCategoryPage";
    private final AppCategory categoryDaily;
    public  boolean is_move=false;
	public float pre_x,pre_y=0;;
	public static final int VIEW_TAB1_TYPE = 0;
	public static final int VIEW_TAB2_TYPE = 1;
	public static final int VIEW_TAB3_TYPE = 2;


    ConverFlowBase coverFlow;
    private CaAppListView mAppList;

    /**
	 * @param context
	 */
	public AppOfDailyCategoryPage(Context context) {
		super(context);
		addView(R.layout.m_app_page);

		registerView(VIEW_TAB1_TYPE);

		//by shuiz
		/*current requirement is unknown, just comment category.*/
        coverFlow = (ConverFlowBase) findViewById(R.id.coverFlow);

        categoryDaily = new AppCategory();
        categoryDaily.setAppCount(1);
        categoryDaily.setIconUrl("?op=1102&type=4&id=81");
        categoryDaily.setId(81);//type=3?
        categoryDaily.setName("日常实用");
        categoryDaily.setPid(34);

		// 默锟斤拷选锟斤拷锟揭伙拷锟
		showChoosedView(VIEW_TAB1_TYPE);
	}

	/*
	 *
	 * */
	@Override
	protected View createContentView(int viewMark) {
		switch (viewMark) {
		case VIEW_TAB1_TYPE:
            mAppList = new CaAppListView(getContext());
            mAppList.initLoadleList(taskMarkPool.getAppListTaskMark(categoryDaily.getId(),
                    ResourceEnum.SORT_ADD_TIME_DESC, ResourceEnum.FEE_FREE_TYPE));

//            appCABrowseViewDaily = new AppOfDailyCategoryListView(getContext());

//            appCABrowseViewDaily.initAppBrowse(categoryDaily);
//            return appCABrowseViewDaily;
            return mAppList;
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
        if (coverFlow != null) {
            coverFlow.refresh();
        }
    }

    @Override
	protected void onSetButton(int mark, boolean selected) 
	{
//		switch (mark) {
//		case VIEW_TAB2_TYPE:
//			tab2Button.setSelected(selected);
//			break;
//
//		case VIEW_TAB3_TYPE:
//			tab3Button.setSelected(selected);
//			break;
//
//		case VIEW_TAB1_TYPE:
//			tab1Button.setSelected(selected);
//			break;
//		}
	}

	@Override
	protected void onBeforeShowView(int viewMark, Object data) {
		int oldMark = getCurrentTabMark();
//		setButtonSelected(oldMark, false);
//		setButtonSelected(viewMark, true);
	}

//	private void setButtonSelected(int mark, boolean selected) {
//		switch (mark) {
//		case VIEW_TAB2_TYPE:
//			tab2Button.setSelected(selected);
//			break;
//
//		case VIEW_TAB3_TYPE:
//			tab3Button.setSelected(selected);
//			break;
//
//		case VIEW_TAB1_TYPE:
//			tab1Button.setSelected(selected);
//			break;
//		}
//	}

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

    // 锟叫憋拷锟铰碉拷应锟斤拷锟叫憋拷
    private class CaAppListView extends AppListView {

        public CaAppListView(Context context) {
            super(context, true);
        }

        @Override
        protected void handleLoadNewItems(ATaskMark taskMark) {
            AppListTaskMark listTaskMark = (AppListTaskMark) taskMark;
            PageInfo pageInfo = listTaskMark.getPageInfo();
            serviceWraper.getAppListByCategory(this, listTaskMark, listTaskMark.getCategory(), listTaskMark
                            .getSortType(), listTaskMark.getFeeType(), pageInfo.getNextPageIndex(), pageInfo.getPageSize(),
                    true);
        }

        @Override
        public void flushView(int what) {
            notifyDataSetInvalid();
        }
    }
}
