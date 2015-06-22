package com.kapps.market.ui.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kapps.market.R;
import com.kapps.market.bean.AppCategory;
import com.kapps.market.bean.Iconable;
import com.kapps.market.bean.MImageType;
import com.kapps.market.cache.AssertCacheManager;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppImageTaskMark;
import com.kapps.market.ui.CommonView;
import com.kapps.market.ui.ListItemsBroswer;
import com.kapps.market.ui.TabableAppView;
import com.kapps.market.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * category view: like system app,net app, sns app,...
 * 
 * @author admin
 * 
 */
public class AppCCBrowsePage extends TabableAppView {

	public static final String TAG = "AppCCBrowsePage";

	private AssertCacheManager assertCacheManager;
	// ����
	private Button tab1Button, tab2Button, tab3Button;
	// ����
	private CommonView parentView;
	// ������
	private List<AppCategory> appCategoryList;

	public static final int VIEW_TAB1_TYPE = 1;
	public static final int VIEW_TAB2_TYPE = 2;
	public static final int VIEW_TAB3_TYPE = 3;

	/**
	 * @param context
	 */
	public AppCCBrowsePage(Context context, CommonView parentView) {
		super(context);
		this.parentView = parentView;

		addView(R.layout.cat_browse_view);

		assertCacheManager = marketContext.getAssertCacheManager();

		// ������ͼ��ѡ��
		registerView(VIEW_TAB1_TYPE);
		registerView(VIEW_TAB2_TYPE);
		registerView(VIEW_TAB3_TYPE);

		// ���
		tab1Button = (Button) findViewById(R.id.tab1Button);
		registerTrigger(tab1Button);

		tab2Button = (Button) findViewById(R.id.tab2Button);
		registerTrigger(tab2Button);

		tab3Button = (Button) findViewById(R.id.tab3Button);
		registerTrigger(tab3Button);

	}

	/**
	 * ���»�����б�
	 * 
	 * @param appCategoryList
	 */
	public void initCCBrowse(List<AppCategory> appCategoryList) {
		this.appCategoryList = appCategoryList;
		showChoosedView(VIEW_TAB1_TYPE);

	}

	@Override
	protected void onBeforeShowView(int viewMark, Object data) {
		int oldMark = getCurrentTabMark();
		setButtonSelected(oldMark, false);
		setButtonSelected(viewMark, true);
	}

	private void setButtonSelected(int mark, boolean selected) {
		Button currentButton = null;
		switch (mark) {
		case VIEW_TAB1_TYPE:
			currentButton = tab1Button;
			break;

		case VIEW_TAB2_TYPE:
			currentButton = tab2Button;
			break;

		case VIEW_TAB3_TYPE:
			currentButton = tab3Button;
			break;
		}
		if (currentButton != null) {
			currentButton.setSelected(selected);
		}
	}

	@Override
	protected View createContentView(int viewMark) {
		CategoryList categoryListView = null;
		if (viewMark == VIEW_TAB1_TYPE) {
			AppCategory appCategory = appCategoryList.get(0);
			categoryListView = new CategoryList(getContext(), appCategory);
			categoryListView.initLoadleList(marketContext.getTaskMarkPool().getCategoryTask());

		} else if (viewMark == VIEW_TAB2_TYPE) {
			AppCategory appCategory = appCategoryList.get(1);
			categoryListView = new CategoryList(getContext(), appCategory);
			categoryListView.initLoadleList(marketContext.getTaskMarkPool().getCategoryTask());

		} else if (viewMark == VIEW_TAB3_TYPE) {
			AppCategory appCategory = appCategoryList.get(2);
			categoryListView = new CategoryList(getContext(), appCategory);
			categoryListView.initLoadleList(marketContext.getTaskMarkPool().getCategoryTask());
		}
		return categoryListView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.ui.TabableAppView#getShowViewMark(android.view.View)
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

	// �б��б�
	private class CategoryList extends ListItemsBroswer {

		private AppCategory subCategory;

		public CategoryList(Context context, AppCategory subCategory) {
			super(context);
			this.subCategory = subCategory;
		}

		@Override
		protected AppImageTaskMark getImageTastMark(Iconable iconable) {
			int iconId = iconable.getIconId();
			if (!assertCacheManager.isItemIconExist(MImageType.CATEGORY_ICON, iconId)) {
				return marketContext.getTaskMarkPool().createAppImageTaskMark(iconId, iconable.getIconUrl(),
						iconable.getIconType());

			} else {
				return null;
			}
		}

		@Override
		protected void handleRealItemClick(Object item) {
			Message msg = Message.obtain();
			msg.what = Constants.M_SHOW_CATEGORY_APP;
			msg.obj = item;
			parentView.handleChainMessage(msg);
		}

		@Override
		protected BaseAdapter createItemAdapter() {
			return new CategoryListAdapter(subCategory.getSubList());
		}

		@Override
		protected void handleLoadNewItems(ATaskMark taskMark) {

		}

		// ����չ���������ͼ������
		private class CategoryListAdapter extends BaseAdapter {
			// �������
			List<AppCategory> appCategoryList = new ArrayList<AppCategory>();

			public CategoryListAdapter(List<AppCategory> appCategoryList) {
				this.appCategoryList = appCategoryList;
			}

			@Override
			public int getCount() {
				return appCategoryList.size();
			}

			@Override
			public Object getItem(int position) {
				return appCategoryList.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_item, null);
				}

				initItemBg(convertView, position);

				AppCategory categoryItem = (AppCategory) getItem(position);

				// ����
				TextView textField = (TextView) convertView.findViewById(R.id.summaryabel);
				textField.setText(categoryItem.getName());

				// �����
				textField = (TextView) convertView.findViewById(R.id.infoLabel);
				textField.setText(categoryItem.getTopAppName());

				// ͼ��
				Drawable icon = assertCacheManager.getCategoryIconFromCache(categoryItem.getId());
				if (icon == null) {
					((ImageView) convertView.findViewById(R.id.iconView)).setImageDrawable(marketContext.emptyAppIcon);
				} else {
					((ImageView) convertView.findViewById(R.id.iconView)).setImageDrawable(icon);
				}

				return convertView;
			}
		}
	}

}
