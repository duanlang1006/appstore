package com.applite.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import com.applite.bean.HomePageApkData;
import com.applite.bean.SubjectData;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;
import com.applite.homepage.R;
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
        }catch(Exception e){
            e.printStackTrace();
        }
        defaultLoadingIcon = BitmapFactory.decodeResource(mResource,R.drawable.buffer);
        implAgent = ImplAgent.getInstance(mContext.getApplicationContext());
    }

    @Override
    public int getCount() {
        List<HomePageApkData> apkList = mData.getData();
        if(null != apkList) {
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
            return position-1;
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
            viewHolder.initView(itemData,mData.getS_datatype(),position);
        }
        return convertView;
    }

    private String luckydrawicon = "http://www.fuli365.net/applite_content_console/image/iden_icon_image_type15.png";
    private String boxlabel;
    private String luckytype;
    private Boolean luckyflag = false;
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.list_item_progress_button){
            Object obj = v.getTag();
            if (obj instanceof ViewHolder) {
                ViewHolder vh = (ViewHolder) obj;
                MitMobclickAgent.onEvent(mContext, "onClickButton" + vh.getItemPosition());

                boxlabel = vh.itemData.getBoxLabel();
                luckytype = boxlabel.substring(boxlabel.length() - 6, boxlabel.length() - 4);
                LogUtils.i(TAG, "luckytype = "+luckytype);

                if(vh.itemData.getBoxLabel().equals(luckydrawicon)){
                    LogUtils.i(TAG, "youjiangxiazai");
                    luckyflag = true;
                }else{
                    luckyflag = false;
                }
                ImplHelper.onClick(mContext,
                        vh.implInfo,
                        vh.itemData.getrDownloadUrl(),
                        vh.itemData.getName(),
                        vh.itemData.getIconUrl(),
                        Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + vh.itemData.getName() + ".apk",
                        null,
                        vh);
            }
        }
    }

    public class ViewHolder implements ImplChangeCallback{
        //不变控件
        private ImageView mAppIcon;
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

        //可变数据
        private ImplInfo implInfo;
        private HomePageApkData itemData;
        private int position;

        ViewHolder(View mView){
            this.pullDownView = (LinearLayout)mView.findViewById(R.id.pullDownView);
            this.mAppIcon = (ImageView) mView.findViewById(R.id.imageViewName);
            this.mAppName = (TextView) mView.findViewById(R.id.apkName);
            this.mCategorySub = (TextView) mView.findViewById(R.id.categorySub);
            this.mAppSize = (TextView) mView.findViewById(R.id.apkSize);
            this.mRatingBar = (RatingBar) mView.findViewById(R.id.ratingbar_Indicator);
            this.mProgressButton = (Button) mView.findViewById(R.id.list_item_progress_button);
            this.mCategoryListArrow = (ImageView) mView.findViewById(R.id.categoryListArrow);
            this.mExtentIcon = (ImageView) mView.findViewById(R.id.extentIcon);
            this.mAppBrief = (TextView) mView.findViewById(R.id.apkBrief);
            if (null != mProgressButton ){
                mProgressButton.setTag(this);
                mProgressButton.setOnClickListener(ListArrayAdapter.this);
            }
        }

        public void refresh() {
            initProgressButton();
        }

        public void initView(HomePageApkData itemData,String layout,int position){
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
            if ((null != this.mAppIcon) && !TextUtils.isEmpty(itemData.getIconUrl())){
                mFinalBitmap.display(this.mAppIcon, itemData.getIconUrl(), defaultLoadingIcon);
            }else {
                mAppIcon.setImageBitmap(defaultLoadingIcon);
            }

            //app名称
            if ((null != this.mAppName) && !TextUtils.isEmpty(itemData.getName())) {
                this.mAppName.setText(itemData.getName());
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
            if ((null != this.mExtentIcon) && !TextUtils.isEmpty(itemData.getBoxLabel())){
                mFinalBitmap.display(this.mExtentIcon, itemData.getBoxLabel());
            }

            //app介绍
            if (null != this.mAppBrief){
                if(!TextUtils.isEmpty(itemData.getBrief())){
                    this.mAppBrief.setText(itemData.getBrief());
                    this.mAppBrief.setVisibility(View.VISIBLE);
                }else{
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
                this.mRatingBar.setRating(star/2.0f);
            }

            if(null != mCategoryListArrow) {
                mCategoryListArrow.setImageResource(R.drawable.back);
            }
            initProgressButton();
        }

        @Override
        public void onChange(ImplInfo info) {
            this.refresh();
        }

        void initProgressButton() {
            if (null != mProgressButton && null != this.implInfo){
                ImplInfo.ImplRes res = implInfo.getImplRes();
                LogUtils.d(TAG, implInfo.getTitle() + "," + implInfo.getStatus() + "," + res.getActionText());
                mProgressButton.setEnabled(true);
                if((implInfo.getStatus() == implInfo.STATUS_INSTALLED) && luckyflag){
                    luckyflag = false;
                    int mLuckyPonints = (int) AppliteSPUtils.get(mContext, AppliteSPUtils.LUCKY_POINTS, 0);
                    mLuckyPonints = MitMobclickAgent.calDrawPoints(mLuckyPonints, "download");
                    AppliteSPUtils.put(mContext, AppliteSPUtils.LUCKY_POINTS, mLuckyPonints);
                }
                switch (implInfo.getStatus()){
                    case ImplInfo.STATUS_PENDING:
                        mProgressButton.setText(res.getActionText());
                        break;
                    case ImplInfo.STATUS_RUNNING:
                        mProgressButton.setText(implInfo.getProgress()+"%");
                        break;
                    case ImplInfo.STATUS_PAUSED:
                        mProgressButton.setText(res.getStatusText());
                        break;
                    case ImplInfo.STATUS_PRIVATE_INSTALLING:
                        mProgressButton.setText(res.getStatusText());
                        mProgressButton.setEnabled(false);
                        break;
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
    }
}
