package com.kapps.market.ui.search;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kapps.market.MApplication;
import com.kapps.market.R;
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.KeyWord;
import com.kapps.market.bean.PageInfo;
import com.kapps.market.cache.AppCahceManager;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.mark.APageTaskMark;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.AppSearchTaskMark;
import com.kapps.market.ui.CommonView;
import com.kapps.market.ui.app.AppListView;
import com.kapps.market.util.ResourceEnum;
/**
 * 应锟斤拷锟斤拷锟斤拷页锟斤拷 <br>
 * 锟斤拷锟斤拷锟斤拷锟解部锟斤拷锟斤拷协锟斤拷<br>
 * 锟斤拷锟斤拷锟斤拷郑锟絟imarket://search?q=name:" + key <br>
 * 锟斤拷锟斤拷锟斤拷锟斤拷锟絟imarket://search?q=des:" + key <br>
 * 锟斤拷锟斤拷锟斤拷撸锟絟imarket://search?q=pub:" + key <br>
 * 锟斤拷锟斤拷锟斤拷锟絟imarket://search?q=pname:" + key
 * 
 * @author admin
 * 
 */
public class AppSearchPage extends CommonView implements OnClickListener{

	public static final String TAG = "AppSearchPage";

	private List<String> keywords = new ArrayList<String>();
	//
	private List<String> nameList = new ArrayList<String>();
	private List<String> authorList = new ArrayList<String>();
	private MApplication application = MApplication.getInstance();
	private AppCahceManager appCahceManager = application.getAppCahceManager();
	private List<String> fix_author=new ArrayList<String>();
	private List<String> fix_name=new ArrayList<String>();
	// 锟斤拷锟斤拷斜锟�
	private SearchResultList searchResultList;
	public EditText keyField;
	private TextView stateLabel;
	private ViewGroup viewGroup;

	// 锟斤拷询锟斤拷锟斤拷
	private AlertDialog searchConfigDialog;
	// 锟斤拷前锟斤拷询锟斤拷key
	private String key;
	// 锟斤拷锟侥革拷锟街讹拷
	private int type = ResourceEnum.SEARCH_NAME;
	private Animation anim[];
	private AnimationListener al;
	



	/**
	 * @param context
	 */
	public AppSearchPage(Context context,EditText appname) {
		super(context);

		addView(R.layout.item_search_page);

		viewGroup = (ViewGroup) findViewById(R.id.contentFrame);
		searchResultList = new SearchResultList(getContext());
		viewGroup.addView(searchResultList);
		viewGroup.setVisibility(View.GONE);
		keyField = appname;
		stateLabel = (TextView) findViewById(R.id.resultStateLabel);
		stateLabel.setVisibility(GONE);

		application.setSearch(AppSearchPage.this);
		handSerachApp(keyField.getText().toString(), type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.searchImageButton:
			handSerachApp(keyField.getText().toString(), type);
			break;
		case R.id.resultStateLabel:
			break;
		default:
			break;
		}
	}

	/**
	 * 锟斤拷锟�始锟斤拷锟斤拷一锟斤拷锟斤拷锟斤拷锟解部应锟斤拷
	 * 
	 * @param uri
	 */
	public void quickAppSearch(Uri uri) {
		try {
			String query = uri.getQueryParameter("q");
			LogUtil.d(TAG, "quickAppSearch query: " + query);
			String[] args = query.split(":");
			String qType = args[0];
			String qKey = args[1];
			boolean canSearch = false;
			if ("pub".equals(qType)) { // 锟斤拷锟斤拷锟斤拷锟�
				canSearch = handSerachApp(qKey, ResourceEnum.SEARCH_AUTHOR);

			} else if ("pname".equals(qType)) { // 锟斤拷锟斤拷锟斤拷锟�
				canSearch = handSerachApp(qKey, ResourceEnum.SEARCH_PACKAGE);

			} else if ("name".equals(qType)) { // 锟斤拷锟斤拷锟斤拷
				canSearch = handSerachApp(qKey, ResourceEnum.SEARCH_NAME);

			} else if ("des".equals(qType)) { // 锟斤拷锟斤拷
				canSearch = handSerachApp(qKey, ResourceEnum.SEARCH_INTRODUCE);
			}

			if (canSearch) {
				keyField.setText(key);
				keyField.setHint(getSearchHint(type));
				// 锟斤拷锟斤拷频锟侥┪�
				keyField.setSelection(key.length());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 锟斤拷锟斤拷锟斤拷锟接︼拷锟�
	public boolean handSerachApp(String key, int type) {
		viewGroup.setVisibility(VISIBLE);
		stateLabel.setVisibility(VISIBLE);
		if (key != null && key.trim().length() != 0) {
			this.key = key;
			this.type = type;
			InputMethodManager imm = (InputMethodManager) getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(keyField.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);

			ATaskMark taskMark = marketContext.getTaskMarkPool()
					.getAppSearchTaskMark(type, key);
			if (taskMark != searchResultList.getTaskMark()) {
				// 锟斤拷取锟斤拷锟斤拷锟斤拷锟侥斤拷锟斤拷
				marketContext.getServiceWraper().forceDiscardReceiveTask(
						taskMark);
				// 锟斤拷锟斤拷锟铰的诧拷询锟斤拷锟斤拷
				searchResultList.initLoadleList(taskMark);
			}

			return true;

		} else {
			Toast.makeText(getContext(),
					getResources().getString(R.string.inpust_key_word), 80)
					.show();
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ck.market.ui.CommonAppView#flushView(int)
	 */
	@Override
	public void flushView(int what) {
		/*if (keyField != null) {
			keyField.clearFocus();
		}
		handSerachApp(keyField.getText().toString(), type);*/
        searchResultList.flushView(1);
	}

	// 锟斤拷锟斤拷锟斤拷示
	private String getSearchHint(int type) {
		switch (type) {
		case ResourceEnum.SEARCH_NAME:
			return getResources().getString(R.string.search_by_software_name);

		case ResourceEnum.SEARCH_INTRODUCE:
			return getResources().getString(
					R.string.search_by_software_introduce);

		case ResourceEnum.SEARCH_AUTHOR:
			return getResources().getString(R.string.search_by_software_author);

		case ResourceEnum.SEARCH_PACKAGE:
			return getResources()
					.getString(R.string.search_by_software_package);

		default:
			return "";
		}
	}

	private void fixList() {
		List<KeyWord> lists = appCahceManager.getKeyWordList();
		nameList.removeAll(nameList);
		authorList.removeAll(authorList);
		if(lists.size()>0)
		{
			for (KeyWord keyWord : lists) {
	
				if (1 == keyWord.getType()) {
					nameList.add(keyWord.getKeyword());
				} else if (2 == keyWord.getType()) {
					authorList.add(keyWord.getKeyword());
				}
			}
		}	
		else 
			{
				for(int i=0;i<14;i++)
				{
					nameList.add(fix_name.get(i));
					authorList.add(fix_author.get(i));
				}
			}
	
		}

	// 锟斤拷锟揭斤拷锟斤拷斜锟�
	private class SearchResultList extends AppListView {

		/**
		 * @param context
		 */
		public SearchResultList(Context context) {
			super(context, true);
			setLayoutParams(new FrameLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT));
		}

		@Override
		protected void handleLoadNewItems(ATaskMark taskMark) {
			AppSearchTaskMark searchTaskMark = (AppSearchTaskMark) taskMark;
			PageInfo pageInfo = searchTaskMark.getPageInfo();
			serviceWraper.searchAppByCondition(this, searchTaskMark, type, key,
					pageInfo.getNextPageIndex(), pageInfo.getPageSize());
		}

		// 锟斤拷锟斤拷息
		@Override
		protected void initBaseItemInfo(ViewHolder viewHolder, AppItem appItem) {
			// 锟斤拷锟斤拷
			if (type == ResourceEnum.SEARCH_NAME) {
				String name = appItem.getName();
				int index = name.toLowerCase().indexOf(key.toLowerCase());
				if (index >= 0) {
					SpannableStringBuilder nameStyle = SpannableStringBuilder
							.valueOf(name);
					nameStyle.setSpan(new ForegroundColorSpan(getResources()
							.getColor(R.color.app_search_highlight)), index,
							index + key.length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					viewHolder.nameLabel.setText(nameStyle);
				} else {
					viewHolder.nameLabel.setText(name);
				}

			} else {
				viewHolder.nameLabel.setText(appItem.getName());
			}

			// 锟斤拷锟斤拷锟斤拷
			if (type == ResourceEnum.SEARCH_AUTHOR) {
				String author = appItem.getAuthorName();
				int index = author.toLowerCase().indexOf(key.toLowerCase());
				if (index >= 0) {
					SpannableStringBuilder authorStyle = SpannableStringBuilder
							.valueOf(author);
					authorStyle.setSpan(new ForegroundColorSpan(getResources()
							.getColor(R.color.app_search_highlight)), index,
							index + key.length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					viewHolder.develpoerView.setText(authorStyle);
				} else {
					viewHolder.develpoerView.setText(author);
				}

			} else {
				viewHolder.develpoerView.setText(appItem.getAuthorName());
			}

			// 锟芥本
			viewHolder.versionView.setText(getResources().getString(
					R.string.version_colon)
					+ appItem.getVersion());
			// 锟斤拷锟斤拷
			viewHolder.ratingView.setRating(appItem.getRating());
			
			//new added.
			if (appItem.getAppDetail() != null) {
				viewHolder.descriptionView.setText(appItem.getAppDetail().getDescribeSplit());
			}
			else {
				viewHolder.descriptionView.setVisibility(View.GONE);
			}
			//
			//updateFunBandState(viewHolder.openFunButton, appItem);
		}
		
		@Override
		protected void updateViewStatus(ATaskMark mark, ViewStatus viewStatus) {
			super.updateViewStatus(mark, viewStatus);
			Log.d("++++ ", "market: " + mark + " viewStatus:" + viewStatus);

			APageTaskMark pageTaskMark = (APageTaskMark) mark;
			int status = pageTaskMark.getTaskStatus();
			if (status == ATaskMark.HANDLE_DOING) {
				stateLabel.setText(getResources().getString(
						R.string.search_for_key_word_colon)
						+ key);

			} else if (status == ATaskMark.HANDLE_OVER) {
				stateLabel.setText(String.format(
						getResources().getString(R.string.search_result_total),
						pageTaskMark.getPageInfo().getRecordNum()));

			} else if (status == ATaskMark.HANDLE_ERROR) {
				stateLabel.setText(String.format(
						getResources().getString(
								R.string.searching_key_word_fail), key));
			}
		}
	}


	/**
	 * 
	 * @return
	 */
	public void setEditContent(String s)
	{
		 keyField.setText(s);
	}
	public int getType()
	{
		return type;
	}
}
