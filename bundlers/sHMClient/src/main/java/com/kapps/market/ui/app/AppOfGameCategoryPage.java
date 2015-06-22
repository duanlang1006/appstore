package com.kapps.market.ui.app;

import android.content.Context;
import android.view.View;
import android.widget.Button;

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
public class AppOfGameCategoryPage extends TabableAppView {

	public static final String TAG = "AppHomePage";
    private final ConverFlowBase coverFlow;
    private final AppCategory categoryGame;
    public  boolean is_move=false;
	public float pre_x,pre_y=0;;
	public static final int VIEW_TAB1_TYPE = 0;
	public static final int VIEW_TAB2_TYPE = 1;
	public static final int VIEW_TAB3_TYPE = 2;

	// 锟斤拷锟斤拷
	private Button tab1Button, tab2Button, tab3Button;
    private CaAppListView mAppList;

    /**
	 * @param context
	 */
	public AppOfGameCategoryPage(Context context) {
		super(context);
		addView(R.layout.m_game_page);
		

		// 锟斤拷始锟斤拷锟斤拷锟脚ワ拷锟斤拷虻サ锟斤拷锟斤拷锟阶拷锟斤拷锟斤拷锟斤拷锟皆憋拷锟斤拷锟斤拷图锟斤拷时锟斤拷
		// 锟斤拷锟斤拷锟斤拷图锟斤拷选锟斤拷
		// 每锟斤拷锟斤拷锟斤拷约锟揭伙拷锟揭筹拷锟
		registerView(VIEW_TAB1_TYPE);


		//by shuiz
		/*current requirement is unknown, just comment category.*/
        coverFlow = (ConverFlowBase) findViewById(R.id.coverFlow);

        categoryGame = new AppCategory();
        categoryGame.setAppCount(0);categoryGame.setIconUrl("?op=1102&type=3&id=90");categoryGame.setId(90);
        categoryGame.setName("游戏竞技");categoryGame.setPid(17);

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
            mAppList = new CaAppListView(getContext());
            mAppList.initLoadleList(taskMarkPool.getAppListTaskMark(categoryGame.getId(),
                    ResourceEnum.SORT_ADD_TIME_DESC, ResourceEnum.FEE_FREE_TYPE));
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

	private void setButtonSelected(int mark, boolean selected) {
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


    // �б��µ�Ӧ���б�
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
