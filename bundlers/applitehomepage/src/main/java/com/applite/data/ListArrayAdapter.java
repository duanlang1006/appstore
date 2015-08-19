package com.applite.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplInfo;
import com.applite.homepage.R;
import com.mit.mitupdatesdk.MitMobclickAgent;

import net.tsz.afinal.FinalBitmap;
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
            viewHolder.initView(itemData,mData.getS_datatype());
        }
        return convertView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.list_item_progress_button){
            Object obj = v.getTag();
            if (obj instanceof ViewHolder) {
                ViewHolder vh = (ViewHolder) obj;
                if (ImplInfo.ACTION_DOWNLOAD == implAgent.getAction(vh.implInfo)) {
                    switch (vh.implInfo.getStatus()) {
                        case Constant.STATUS_PENDING:
                            break;
                        case Constant.STATUS_RUNNING:
                            implAgent.pauseDownload(vh.implInfo);
                            break;
                        case Constant.STATUS_PAUSED:
                            implAgent.resumeDownload(vh.implInfo, vh.implCallback);
                            break;
                        default:
                            implAgent.newDownload(vh.implInfo,
                                    Constant.extenStorageDirPath,
                                    vh.itemData.getName() + ".apk",
                                    true,
                                    vh.implCallback);
                            break;
                    }
                } else {
                    switch(implAgent.getAction(vh.implInfo)){
                        case ImplInfo.ACTION_INSTALL:
                            MitMobclickAgent.onEvent(mContext, "clickInstallApk");
                            break;
                        case ImplInfo.ACTION_OPEN:
                            MitMobclickAgent.onEvent(mContext, "clickOpenApk");
                            break;
                    }

                    try {
                        mContext.startActivity(implAgent.getActionIntent(vh.implInfo));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }



    public class ViewHolder {
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
        private ImplChangeCallback implCallback;
        private LinearLayout pullDownView;

        ImplInfo implInfo;
        HomePageApkData itemData;

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
            this.implCallback = new ListImplCallback(this);
            if (null != mProgressButton ){
                mProgressButton.setTag(this);
                mProgressButton.setOnClickListener(ListArrayAdapter.this);
            }
        }

        public void refresh() {
            initProgressButton();
        }

        public void initView(HomePageApkData itemData,String layout){
            this.itemData = itemData;
            this.layoutStr = layout;
            this.implInfo = implAgent.getImplInfo(itemData.getPackageName(), itemData.getPackageName(), itemData.getVersionCode());
            if (null != this.implInfo) {
                this.implInfo.setDownloadUrl(itemData.getrDownloadUrl())
                        .setTitle(itemData.getName())
                        .setIconUrl(itemData.getIconUrl());
                implAgent.setImplCallback(implCallback, implInfo);
            }

            if (null != this.mAppIcon && null != itemData.getIconUrl()){
                mFinalBitmap.display(this.mAppIcon, itemData.getIconUrl(), defaultLoadingIcon);
            }else {
                mAppIcon.setImageBitmap(defaultLoadingIcon);
            }
            if (null != this.mAppName && null != itemData.getName()) {
                this.mAppName.setText(itemData.getName());
            }
            if (null != this.mCategorySub && null != itemData.getCategorysub()) {
                this.mCategorySub.setText(itemData.getCategorysub());
            }
            if (null != this.mAppSize && null != itemData.getApkSize()) {
                String mSize = AppliteUtils.bytes2kb(Long.parseLong(itemData.getApkSize()));
                if (null != mSize) {
                    this.mAppSize.setText(mSize);
                }
            }
//            if (null != this.mExtentIcon) {
//                String s = itemData.getBoxLabel();
//                switch (s){
//                    case "1":
//                        this.mExtentIcon.setImageResource(R.drawable.iden_icon_image_type1);
//                        break;
//                    case "2":
//                        this.mExtentIcon.setImageResource(R.drawable.iden_icon_image_type2);
//                        break;
//                    case "3":
//                        this.mExtentIcon.setImageResource(R.drawable.iden_icon_image_type3);
//                        break;
//                    case "4":
//                        this.mExtentIcon.setImageResource(R.drawable.iden_icon_image_type4);
//                        break;
//                    case "5":
//                        this.mExtentIcon.setImageResource(R.drawable.iden_icon_image_type5);
//                        break;
//                    case "6":
//                        this.mExtentIcon.setImageResource(R.drawable.iden_icon_image_type6);
//                        break;
//                    case "7":
//                        this.mExtentIcon.setImageResource(R.drawable.iden_icon_image_type7);
//                        break;
//                    default:
//                        break;
//                }
//            }
            if (null != this.mExtentIcon && null != itemData.getBoxLabel()){
                mFinalBitmap.display(this.mExtentIcon, itemData.getBoxLabel());
            }

            if (null != this.mAppBrief){
                if(!TextUtils.isEmpty(itemData.getBrief())){
                    this.mAppBrief.setText(itemData.getBrief());
                    this.mAppBrief.setVisibility(View.VISIBLE);
                }else{
                    this.mAppBrief.setVisibility(View.GONE);
                }
            }
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

        void initProgressButton() {
            if (null != mProgressButton && null != this.implInfo){
                LogUtils.d(TAG,implInfo.getTitle()+","+implInfo.getStatus()+","+implAgent.getActionText(implInfo));
                mProgressButton.setEnabled(true);
                switch (implInfo.getStatus()){
                    case Constant.STATUS_PENDING:
                        mProgressButton.setText(implAgent.getActionText(implInfo));
                        break;
                    case Constant.STATUS_RUNNING:
                        mProgressButton.setText(implAgent.getProgress(implInfo)+"%");
                        break;
                    case Constant.STATUS_PAUSED:
                        mProgressButton.setText(implAgent.getStatusText(implInfo));
                        break;
                    case Constant.STATUS_PRIVATE_INSTALLING:
                        mProgressButton.setText(implAgent.getStatusText(implInfo));
                        mProgressButton.setEnabled(false);
                        break;
                    default:
                        mProgressButton.setText(implAgent.getActionText(implInfo));
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
    }

    class ListImplCallback implements ImplChangeCallback {
        Object tag ;

        ListImplCallback(Object tag) {
            super();
            this.tag = tag;
        }

        @Override
        public void onChange(ImplInfo info) {
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }
    }
}
