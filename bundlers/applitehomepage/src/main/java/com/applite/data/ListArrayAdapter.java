package com.applite.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.bean.HomePageApkData;
import com.applite.bean.SubjectData;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.homepage.R;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;
import com.mit.mitupdatesdk.MitMobclickAgent;

import net.tsz.afinal.FinalBitmap;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

public class ListArrayAdapter extends BaseAdapter implements View.OnClickListener {
    private static final String TAG = "homepage_ListArrayAdapter";
    private LayoutInflater mInflater = null;
    private Resources mResource = null;
    private Context mContext = null;
    private SubjectData mData = null;
    private FinalBitmap mFinalBitmap;
    private Bitmap defaultLoadingIcon;
    private Bitmap defaultExternIcon;
    private ImplAgent implAgent;

    int layoutResourceId = 0;

    public ListArrayAdapter(Context context, SubjectData data) {
        this.mContext = context;
        this.mData = data;
        mFinalBitmap = FinalBitmap.create(context);

        mResource = context.getResources();
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {
            Field field = R.layout.class.getField(mData.getS_datatype());
            layoutResourceId = field.getInt(new R.layout());
        } catch (Exception e) {
            e.printStackTrace();
        }
        defaultLoadingIcon = BitmapFactory.decodeResource(mResource, R.drawable.buffer);
        defaultExternIcon = BitmapFactory.decodeResource(mResource, R.drawable.extern_bg);

        LogUtils.i(TAG, mData + "");

        implAgent = ImplAgent.getInstance(mContext.getApplicationContext());
    }

    @Override
    public int getCount() {
        List<HomePageApkData> apkList = mData.getData();
        if (null != apkList) {
            return apkList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position == 0)
            return 0;
        else
            return position - 1;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResourceId, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (null != mData && null != mData.getData()) {

            HomePageApkData itemData = mData.getData().get(position);
            viewHolder.initView(itemData, mData.getS_datatype(), position);
            if (mData.getS_name().equals("排行")) {
                viewHolder.setAppIdVisible();
            } else {
                viewHolder.setAppIdInVisible();
            }
        }
        return convertView;
    }

    private String boxLabel_value;
    private int points;
    private boolean luckyflag = false;
    private boolean pressbutton = false;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.list_item_progress_button) {
            Object obj = v.getTag();
            if (obj instanceof ViewHolder) {
                ViewHolder vh = (ViewHolder) obj;
                MitMobclickAgent.onEvent(mContext, "onClickButton" + vh.getItemPosition());

                pressbutton = true;

                boxLabel_value = vh.itemData.getBoxLabelvale();
                LogUtils.d(TAG, "boxLabel_value = " + boxLabel_value);

                if (!TextUtils.isEmpty(boxLabel_value)) {
                    luckyflag = true;
                    points = Integer.parseInt(boxLabel_value);
                } else
                    luckyflag = false;
                LogUtils.d(TAG, "points = " + points + " luckyflag = " + luckyflag);
                LogUtils.d("duanlang", "vh.itemData.getMd5() = " + vh.itemData.getMd5());

                ImplHelper.onClick(mContext,
                        vh.implInfo,
                        vh.itemData.getrDownloadUrl(),
                        vh.itemData.getName(),
                        vh.itemData.getIconUrl(),
                        Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + vh.itemData.getName() + ".apk",
                        vh.itemData.getMd5(),
                        vh);
            }
        }
    }

    public class ViewHolder implements ImplChangeCallback {
        //不变控件
        private ImageView mAppIcon;
        private TextView mAppId;
        private TextView mAppName;
        private TextView mCategorySub;
        private TextView mAppSize;
        private TextView mAppBrief;
        private RatingBar mRatingBar;
        private Button mProgressButton;
        private ImageView mCategoryListArrow;
        private ImageView mExtentIcon;
        private String layoutStr;
        private LinearLayout pullDownView;
        private LinearLayout apkidarea;

        //可变数据
        private ImplInfo implInfo;
        private HomePageApkData itemData;
        private int position;

        ViewHolder(View mView) {
            this.pullDownView = (LinearLayout) mView.findViewById(R.id.pullDownView);
            this.apkidarea = (LinearLayout) mView.findViewById(R.id.apkidarea);
            this.mAppIcon = (ImageView) mView.findViewById(R.id.imageViewName);
            this.mAppId = (TextView) mView.findViewById(R.id.apkId);
            this.mAppName = (TextView) mView.findViewById(R.id.apkName);
            this.mCategorySub = (TextView) mView.findViewById(R.id.categorySub);
            this.mAppSize = (TextView) mView.findViewById(R.id.apkSize);
            this.mRatingBar = (RatingBar) mView.findViewById(R.id.ratingbar_Indicator);
            this.mProgressButton = (Button) mView.findViewById(R.id.list_item_progress_button);
            this.mCategoryListArrow = (ImageView) mView.findViewById(R.id.categoryListArrow);
            this.mExtentIcon = (ImageView) mView.findViewById(R.id.extentIcon);
            this.mAppBrief = (TextView) mView.findViewById(R.id.apkBrief);
            if (null != mProgressButton) {
                mProgressButton.setTag(this);
                mProgressButton.setOnClickListener(ListArrayAdapter.this);
            }
        }

        public void refresh() {
            initProgressButton();
        }

        public void initView(HomePageApkData itemData, String layout, int position) {
            this.itemData = itemData;
            this.layoutStr = layout;
            this.position = position;
            this.implInfo = implAgent.getImplInfo(itemData.getPackageName(), itemData.getPackageName(), itemData.getVersionCode());
            if (null != this.implInfo) {
                this.implInfo.setDownloadUrl(itemData.getrDownloadUrl())
                        .setTitle(itemData.getName())
                        .setIconUrl(itemData.getIconUrl());
                implAgent.bindImplCallback(this, implInfo);
            }

            //app图标
            if ((null != this.mAppIcon) && !TextUtils.isEmpty(itemData.getIconUrl()) && AppliteUtils.isLoadNetworkBitmap(mContext)) {
                mFinalBitmap.display(this.mAppIcon, itemData.getIconUrl(), defaultLoadingIcon);
            } else {
                mAppIcon.setImageBitmap(defaultLoadingIcon);
            }

            //app名称
            if ((null != this.mAppName) && !TextUtils.isEmpty(itemData.getName())) {
                this.mAppName.setText(itemData.getName());
                if (null != this.mAppId) {
                    setAppIdVisible();
                    if ((position == 0) || (position == 1) || (position == 2)) {
                        this.mAppId.setBackgroundResource(R.drawable.ranking_top_bg);
                    } else {
                        this.mAppId.setBackgroundResource(R.drawable.ranking_normal_bg);
                    }
                    this.mAppId.setText(String.valueOf(position + 1));
                }
            }

            //app分类
            if ((null != this.mCategorySub) && !TextUtils.isEmpty(itemData.getCategorysub())) {
                this.mCategorySub.setText(itemData.getCategorysub());
            }

            //app大小
            if ((null != this.mAppSize) && !TextUtils.isEmpty(itemData.getApkSize())) {
                String mSize = AppliteUtils.bytes2kb(Long.parseLong(itemData.getApkSize()));
                if (null != mSize) {
                    this.mAppSize.setText(mSize);
                }
            }

            //app角标
            if (null != this.mExtentIcon) {
                if (!TextUtils.isEmpty(itemData.getBoxLabel())) {
                    mFinalBitmap.display(this.mExtentIcon, itemData.getBoxLabel(), defaultExternIcon);
                } else {
                    mExtentIcon.setImageBitmap(defaultExternIcon);
                }
            }

            //app介绍
            if (null != this.mAppBrief) {
                if (!TextUtils.isEmpty(itemData.getBrief())) {
                    this.mAppBrief.setText(itemData.getBrief());
                    this.mAppBrief.setVisibility(View.VISIBLE);
                } else {
                    this.mAppBrief.setVisibility(View.GONE);
                }
            }

            //app评分
            if (null != mRatingBar) {
                float star = 0.0f;
                try {
                    star = Float.parseFloat(itemData.getRating());
                } catch (Exception e) {
                }
                this.mRatingBar.setRating(star / 2.0f);
            }

            if (null != mCategoryListArrow) {
                mCategoryListArrow.setImageResource(R.drawable.back);
            }
            initProgressButton();
        }

        @Override
        public void onChange(ImplInfo info) {
            this.refresh();
        }

        void initProgressButton() {
            if (null != mProgressButton && null != this.implInfo) {
                ImplInfo.ImplRes res = implInfo.getImplRes();
                LogUtils.d(TAG, implInfo.getTitle() + "," + implInfo.getStatus() + "," + res.getActionText());
                mProgressButton.setEnabled(true);
                if ((implInfo.getStatus() == implInfo.STATUS_INSTALLED) && luckyflag) {
                    luckyflag = false;
                    int mLuckyPonints = (int) AppliteSPUtils.get(mContext, AppliteSPUtils.LUCKY_POINTS, 0);
                    mLuckyPonints += points;
                    AppliteSPUtils.put(mContext, AppliteSPUtils.LUCKY_POINTS, mLuckyPonints);
                    Toast toast = Toast.makeText(mContext, "成功下载安装有奖应用" + implInfo.getTitle() + ", 获得奖励 " + points + " 积分", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                switch (implInfo.getStatus()) {
                    case ImplInfo.STATUS_PENDING:
                        mProgressButton.setText(res.getActionText());
                        break;
                    case ImplInfo.STATUS_RUNNING:
                        mProgressButton.setText(implInfo.getProgress() + "%");
                        break;
                    case ImplInfo.STATUS_PAUSED:
                        mProgressButton.setText(res.getStatusText());
                        break;
                    case ImplInfo.STATUS_PRIVATE_INSTALLING:
                        mProgressButton.setText(res.getStatusText());
                        mProgressButton.setEnabled(false);
                        break;
                    case ImplInfo.STATUS_FAILED:
                        if (pressbutton) {
                            pressbutton = false;
                            if (implInfo.getCause() == ImplInfo.CAUSE_FAILED_BY_SPACE_NOT_ENOUGH) {
                                Toast toast = Toast.makeText(mContext, "存储空间不足，请释放空间！", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM, 0, 80);
                                toast.show();
                            }
                        }
                    default:
                        mProgressButton.setText(res.getActionText());
                        break;
                }
            }
        }

        public String getLayoutStr() {
            return layoutStr;
        }

        public HomePageApkData getItemData() {
            return itemData;
        }

        public int getItemPosition() {
            return this.position;
        }

        public void setAppIdVisible() {
            if (null != apkidarea) {
                this.apkidarea.setVisibility(View.VISIBLE);
            }
        }

        public void setAppIdInVisible() {
            if (null != apkidarea) {
                this.apkidarea.setVisibility(View.GONE);
            }
        }
    }
}
