package com.kapps.market.ui.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.kapps.market.bean.AppItem;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.ui.ListItemsBroswer;
import com.kapps.market.ui.RatingView;
import com.kapps.market.util.Constants;
import com.kapps.market.util.Util;

/**
 * 应锟斤拷锟叫憋拷 锟斤拷锟�锟斤拷list锟斤拷锟斤拷锟角达拷锟斤拷锟斤拷锟节诧拷锟斤拷list锟斤拷<br>
 * 锟斤拷锏揭伙拷锟斤拷效锟斤拷通锟斤拷锟斤拷应锟斤拷锟斤拷募锟斤拷亍锟�
 * 
 * @author Administrator
 * 
 */
public abstract class AppListView extends ListItemsBroswer {

	public static final String TAG = "AppListView";

	/**
	 * @param context
	 */
	public AppListView(Context context) {
		super(context);
	}

	/**
	 * @param context
	 */
	public AppListView(Context context, boolean useLongClick) {
		super(context, useLongClick);
	}

	@Override
	protected BaseAdapter createItemAdapter() {
		BaseAdapter appListAdapter = new AppListApdapter();
		return appListAdapter;
	}

	// 锟斤拷锟斤拷锟斤拷
	protected class AppListApdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return appCahceManager.getAppItemCount(mTaskMark);
		}

		@Override
		public AppItem getItem(int position) {
			// TODO Auto-generated method stub
			return appCahceManager.getAppItemByMarkIndex(mTaskMark, position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		/*
		 * (non-Javadoc) 锟斤拷锟斤拷只锟斤拷app count 锟斤拷为0锟斤拷时锟斤拷呕岜伙拷锟斤拷谩锟�
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// 锟斤拷锟斤拷欠锟揭拷锟斤拷锟斤拷碌锟斤拷锟�
			if (position == (getCount() - 1)) {
				tryQueryNewItems();
			}
			// 锟斤拷前位锟斤拷锟斤拷图
			if (convertView == null) {
				convertView = createItemView(parent);
			}
			// 锟斤拷贸锟绞硷拷锟斤拷玫锟斤拷锟酵
			AppItem appItem = getItem(position);
			if (appItem != null) {
				initItemView(convertView, appItem, position);
			}

			return convertView;
		}
	}

	// 锟斤拷锟斤拷锟斤拷锟斤拷图
	protected View createItemView(ViewGroup parent) {
		View convertView = LayoutInflater.from(getContext()).inflate(
				R.layout.app_item, parent, false);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.iconView = ((ImageView) convertView
				.findViewById(R.id.appIconView));
		viewHolder.ratingView = ((RatingView) convertView
				.findViewById(R.id.appRatingView));
		viewHolder.nameLabel = (TextView) convertView
				.findViewById(R.id.appNameLabel);
		viewHolder.develpoerView = (TextView) convertView
				.findViewById(R.id.appDeveloperLabel);
		viewHolder.versionView = (TextView) convertView
				.findViewById(R.id.appVersionLabel);
		viewHolder.sizeView = (TextView) convertView.findViewById(R.id.appSizeLabel);
		viewHolder.appPaymentView = (Button) convertView
				.findViewById(R.id.appPaymentLabel);
		viewHolder.descriptionView = (TextView) convertView.findViewById(R.id.appDescriptionLabel);
		//viewHolder.openFunButton = (Button) convertView.findViewById(R.id.openFunButton);
		convertView.setTag(viewHolder);

		return convertView;
	}

	// 锟斤拷始锟斤拷锟斤拷锟斤拷图
	protected void initItemView(View convertView, AppItem appItem, int position) {
		ViewHolder viewHolder = (ViewHolder) convertView.getTag();

		// 锟斤拷谋锟斤拷锟
		initItemBg(convertView, position);

		// 锟斤拷锟斤拷息
		initBaseItemInfo(viewHolder, appItem);

		// 状态, 锟斤拷锟斤拷锟斤拷时锟津不达拷锟斤拷锟皆憋拷锟斤拷锟斤拷锟斤拷锟斤拷
		/*
        if (scrolling)
        {
			if (appItem.isFree()) {
				viewHolder.appPaymentView.setText(getResources().getString(
						R.string.free));

			} else {
				viewHolder.appPaymentView.setText(getResources().getString(R.string.price_colon) + appItem.getPrice());
			}

		}
        else */
        {
			String viweing = marketManager.getAppViewDescribeForNextAction(appItem);
			if (viweing != null) {
				viewHolder.appPaymentView.setText(viweing);

			} else if (appItem.isFree()) {
				viewHolder.appPaymentView.setText(getResources().getString(
						R.string.free));

			} else {
				viewHolder.appPaymentView.setText(getResources().getString(R.string.price_colon) + appItem.getPrice());
			}
		}

        String author = appItem.getAuthorName();
        if (author != null && author.equalsIgnoreCase("google play")) {
            viewHolder.appPaymentView.setText("");
            viewHolder.appPaymentView.setBackgroundResource(R.drawable.google_play_128);
        }
        else {
            viewHolder.appPaymentView.setBackgroundResource(R.drawable.common_btn);
        }

		// 图锟斤拷
		Drawable icon = assertCacheManager.getAppIconFromCache(appItem.getId());
		if (icon == null) {
			viewHolder.iconView.setImageDrawable(marketContext.emptyAppIcon);
		} else {
			viewHolder.iconView.setImageDrawable(icon);
		}

        registerAppPaymentButton(viewHolder.appPaymentView, appItem);
	}
	
	protected void registerAppPaymentButton(final Button merButton, final AppItem appItem) {
		merButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(appItem.getAuthorName().equals("google play")){
					final String packageName = appItem.getPackageName();
		            marketManager.launchMarket(packageName);
		            return;
				}
				int realstate = marketManager.getJointSoftwareState(appItem);
				if (realstate == BaseApp.APP_INSTALLED) {
					marketManager.openSoftware(appItem.getPackageName());
				} else if (realstate == BaseApp.APP_DOWNLOADED) {
					String apkPath = marketManager.getJointApkSavePath(appItem.getPackageName(), appItem.getVersionCode());
					if (apkPath != null) {
						marketManager.installSoftware(apkPath);
					}
				} else {
					// BaseApp.APP_NEW:
					// BaseApp.APP_DOWNLOAD_STOP:
					merButton.setEnabled(false);
                    merButton.setText(R.string.downloading);
					boolean ok = marketManager.checkSDCardStateAndNote();
					if (ok) {
						Message message = Message.obtain();
						message.what = Constants.M_DOWNLOAD_ACCEPT;
						message.obj = appItem;
						message.arg1 = getMessageMark();
						marketContext.handleMarketMessage(message);
					}
                    merButton.setEnabled(true);
				}
			}
		});
	}

	// 锟斤拷锟斤拷息
	protected void initBaseItemInfo(ViewHolder viewHolder, AppItem appItem) {
		// 锟斤拷锟斤拷
		viewHolder.nameLabel.setText(appItem.getName());

        //
        final String author = appItem.getAuthorName();
		if (author != null && author.length() > 0 && !author.equalsIgnoreCase("google play")) {
			viewHolder.develpoerView.setVisibility(View.VISIBLE);
			viewHolder.develpoerView.setText(author);
		}
		else {
			viewHolder.develpoerView.setVisibility(View.GONE);
		}

		// Version
		viewHolder.versionView.setText(getResources().getString(
				R.string.version_colon)
				+ appItem.getVersion());
		
		viewHolder.sizeView.setText(getResources().getString(
				R.string.size_colon) + Util.getSizeDes(appItem));
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

	/**
	 * 只为锟斤拷锟斤拷锟斤拷锟斤拷锟叫憋拷锟筋背锟斤拷<br>
	 * 锟斤拷锟斤拷之锟矫ｏ拷锟斤拷实锟斤拷
	 */
	@Override
	protected void initItemBg(View convertView, int position) {
		if (position % 2 == 0) {
			convertView.setBackgroundResource(R.drawable.list_item_even_bg);

		} else {
			convertView.setBackgroundResource(R.drawable.list_item_odd_bg);
		}

	}

	// 锟斤拷图锟斤拷锟�
	protected class ViewHolder {
		public ImageView iconView;
		public RatingView ratingView;
		public TextView nameLabel;
		public TextView versionView;
		public TextView sizeView;
		public TextView develpoerView;
		public Button appPaymentView;
		public TextView descriptionView;
	}

	@Override
	protected void handleRealItemClick(Object item) {
        AppItem appItem = (AppItem) item;
        String author = appItem.getAuthorName();
        if (author != null && author.equalsIgnoreCase("google play")) {
            final String packageName = appItem.getPackageName();
            marketManager.launchMarket(packageName);
            return;
        }
		marketContext.handleShowAppDetail(appItem.getId());
	}

	@Override
	protected void handleRealItemLongClick(Object item) {
		Dialog dialog = null;
		final AppItem appItem = (AppItem) item;
		// 锟斤拷锟斤拷直锟斤拷锟斤拷锟截碉拷锟斤拷锟�
		/*google play forbidden long click*/
		if(appItem.getAuthorName().equals("google play"))
			return;
		
		if (appItem.isFree()) {
			dialog = new AlertDialog.Builder(getContext())
					.setTitle(appItem.getName())
					.setItems(R.array.app_can_download_long_click,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									if (whichButton == 0) {
										Message message = Message.obtain();
										message.what = Constants.M_FAVOR_ADDED;
										message.arg1 = getMessageMark();
										message.obj = appItem;
										marketContext
												.handleMarketMessage(message);

									} else {
										Message message = Message.obtain();
										message.what = Constants.M_QUICK_DOWNLOAD_APP;
										message.obj = appItem;
										message.arg1 = getMessageMark();
										marketContext
												.handleMarketMessage(message);

									}
									dialog.dismiss();
								}
							}).create();
			dialog.show();
		}
	}

}
