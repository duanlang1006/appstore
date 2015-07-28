package com.applite.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.applite.bean.HomePageApkData;
import com.applite.bean.SubjectData;
import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.homepage.BundleContextFactory;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.applite.homepage.R;
import com.mit.impl.ImplListener;

import net.tsz.afinal.FinalBitmap;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by yuzhimin on 6/17/15.
 */
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
            Context mcontext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            mInflater = LayoutInflater.from(mcontext);
            mInflater = mInflater.cloneInContext(mcontext);
            mResource = mcontext.getResources();
        }catch (Exception e){
            e.printStackTrace();
        }
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
        private RatingBar mRatingBar;
        private Button mProgressButton;
        private ImageView mCategoryListArrow;
        private String layoutStr;
        private ListImplCallback implCallback;
        ImplInfo implInfo;
        HomePageApkData itemData;

        ViewHolder(View mView){
            this.mAppIcon = (ImageView) mView.findViewById(R.id.imageViewName);
            this.mAppName = (TextView) mView.findViewById(R.id.apkName);
            this.mCategorySub = (TextView) mView.findViewById(R.id.categorySub);
            this.mAppSize = (TextView) mView.findViewById(R.id.apkSize);
            this.mRatingBar = (RatingBar) mView.findViewById(R.id.ratingbar_Indicator);
            this.mProgressButton = (Button) mView.findViewById(R.id.list_item_progress_button);
            this.mCategoryListArrow = (ImageView) mView.findViewById(R.id.categoryListArrow);
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
                switch (implInfo.getStatus()){
                    case Constant.STATUS_PENDING:
                        mProgressButton.setEnabled(false);
                        mProgressButton.setText(implAgent.getActionText(implInfo));
                        break;
                    case Constant.STATUS_RUNNING:
                        mProgressButton.setEnabled(false);
                        mProgressButton.setText(implAgent.getProgress(implInfo)+"%");
                        break;
                    case Constant.STATUS_PAUSED:
                        mProgressButton.setEnabled(false);
                        mProgressButton.setText(implAgent.getStatusText(implInfo));
                        break;
                    default:
                        mProgressButton.setBackground(mResource.getDrawable(R.drawable.item_button_bg));
                        mProgressButton.setEnabled(true);
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

    class ListImplCallback extends ImplListener {
        Object tag ;

        ListImplCallback(Object tag) {
            super();
            this.tag = tag;
        }

        @Override
        public void onStart(ImplInfo info) {
            super.onStart(info);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onCancelled(ImplInfo info) {
            super.onCancelled(info);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onLoading(ImplInfo info, long total, long current, boolean isUploading) {
            super.onLoading(info, total, current, isUploading);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onSuccess(ImplInfo info, File file) {
            super.onSuccess(info, file);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onFailure(ImplInfo info, Throwable t, String msg) {
            super.onFailure(info, t, msg);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onInstallSuccess(ImplInfo info) {
            super.onInstallSuccess(info);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onInstalling(ImplInfo info) {
            super.onInstalling(info);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onInstallFailure(ImplInfo info, int errorCode) {
            super.onInstallFailure(info, errorCode);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onUninstallSuccess(ImplInfo info) {
            super.onUninstallSuccess(info);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onUninstalling(ImplInfo info) {
            super.onUninstalling(info);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onUninstallFailure(ImplInfo info, int errorCode) {
            super.onUninstallFailure(info, errorCode);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }
    }
}
