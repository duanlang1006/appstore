package com.mit.appliteupdate.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applite.common.AppliteUtils;
import com.applite.common.BitmapHelper;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.lidroid.xutils.BitmapUtils;
import com.mit.appliteupdate.R;
import com.mit.appliteupdate.bean.ApkData;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplChangeCallback;
import com.mit.mitupdatesdk.MitMobclickAgent;
import com.osgi.extra.OSGIServiceHost;

import java.io.File;
import java.util.List;

/**
 * Created by LSY on 15-6-23.
 */
public class UpdateAdapter extends BaseAdapter {

    private final BitmapUtils mBitmapUtil;
    private final PackageManager mPackageManager;
    private Context mActivity;
    private List<ApkData> mDatas;
    private ImplAgent implAgent;
    private int mCheckedItemPosition = -1;

    public UpdateAdapter(Context context, List<ApkData> mDatas) {
        this.mDatas = mDatas;
        mActivity = context;
        mPackageManager = mActivity.getPackageManager();
        mBitmapUtil = BitmapHelper.getBitmapUtils(mActivity.getApplicationContext());
        implAgent = ImplAgent.getInstance(mActivity.getApplicationContext());
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewholder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            convertView = inflater.inflate(R.layout.item_update_listview, parent, false);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }
        final ApkData data = mDatas.get(position);
        viewholder.initView(data, position);
        if (mCheckedItemPosition == position) {
            viewholder.mButLayout.setVisibility(View.VISIBLE);
            viewholder.mDetailStateImg.setBackground(mActivity.getResources().getDrawable(R.drawable.desc_less));
            viewholder.mDetailDataTv.setVisibility(View.GONE);
            viewholder.mDetailAllDataTv.setVisibility(View.VISIBLE);
        } else {
            viewholder.mButLayout.setVisibility(View.GONE);
            viewholder.mDetailStateImg.setBackground(mActivity.getResources().getDrawable(R.drawable.desc_more));
            viewholder.mDetailDataTv.setVisibility(View.VISIBLE);
            viewholder.mDetailAllDataTv.setVisibility(View.GONE);
        }

        viewholder.mName.setText(data.getName());
        if (!TextUtils.isEmpty(data.getUpdateInfo())) {
            viewholder.mDetailDataTv.setText(data.getUpdateInfo());
            viewholder.mDetailAllDataTv.setText(data.getUpdateInfo());
        } else {
            if (!TextUtils.isEmpty(data.getUpdateTime())) {
                viewholder.mDetailDataTv.setText(data.getUpdateTime());
                viewholder.mDetailAllDataTv.setText(data.getUpdateInfo());
            } else {
                viewholder.mDetailDataTv.setText(null);
                viewholder.mDetailAllDataTv.setText(data.getUpdateInfo());
            }
        }
        mBitmapUtil.display(viewholder.mImg, data.getIconUrl());
        try {
            PackageInfo mPackageInfo = mPackageManager.getPackageInfo(data.getPackageName(), PackageManager.GET_ACTIVITIES);
            viewholder.mVersionName.setText(mPackageInfo.versionName + " -> " + data.getVersionName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        viewholder.mApkSize.setText(AppliteUtils.bytes2kb(data.getApkSize()));
        viewholder.mBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder vh = (ViewHolder) v.getTag();
                MitMobclickAgent.onEvent(mActivity, "onClickButton" + vh.position);

                String path = Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + vh.bean.getName() + ".apk";
                LogUtils.d("UpdateFragment", "打开");
                ImplHelper.onClick(mActivity,
                        vh.implInfo,
                        vh.bean.getrDownloadUrl(),
                        vh.bean.getName(),
                        vh.bean.getIconUrl(),
                        path,
                        null,
                        vh);
            }
        });
        viewholder.mOpenDetailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckedItemPosition == viewholder.position) {
                    mCheckedItemPosition = -1;
                } else {
                    mCheckedItemPosition = viewholder.position;
                }
                notifyDataSetChanged();
            }
        });
        viewholder.mOpenDetailLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckedItemPosition == viewholder.position) {
                    mCheckedItemPosition = -1;
                } else {
                    mCheckedItemPosition = viewholder.position;
                }
                notifyDataSetChanged();
            }
        });
        viewholder.mUninstallTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("package:" + data.getPackageName());
                Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                mActivity.startActivity(intent);
            }
        });
        viewholder.mIgnoreTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        viewholder.mToDetailTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OSGIServiceHost) mActivity).jumptoDetail(data.getPackageName(), data.getName(), data.getIconUrl(), true);
            }
        });

        return convertView;
    }

    public class ViewHolder implements ImplChangeCallback {
        private ImageView mImg;
        private TextView mName;
        private TextView mApkSize;
        private TextView mVersionName;
        private Button mBt;
        private ApkData bean;
        private ImplInfo implInfo;
        private int position;

        private LinearLayout mOpenDetailLayout;
        private LinearLayout mOpenDetailLayout1;
        private TextView mDetailDataTv;
        private TextView mDetailAllDataTv;
        private LinearLayout mButLayout;
        private TextView mUninstallTv;
        private TextView mIgnoreTv;
        private TextView mToDetailTv;
        private ImageView mDetailStateImg;

        public ViewHolder(View v) {
            this.mImg = (ImageView) v.findViewById(R.id.item_update_img);
            this.mName = (TextView) v.findViewById(R.id.item_update_name);
            this.mApkSize = (TextView) v.findViewById(R.id.item_update_size);
            this.mVersionName = (TextView) v.findViewById(R.id.item_update_versionname);
            this.mBt = (Button) v.findViewById(R.id.item_update_button);

            this.mOpenDetailLayout = (LinearLayout) v.findViewById(R.id.item_update_open);
            this.mOpenDetailLayout1 = (LinearLayout) v.findViewById(R.id.item_update_open1);
            this.mButLayout = (LinearLayout) v.findViewById(R.id.item_update_but_layout);
            this.mDetailDataTv = (TextView) v.findViewById(R.id.item_update_show_detail_tv);
            this.mDetailAllDataTv = (TextView) v.findViewById(R.id.item_update_show_all_detail_tv);
            this.mDetailStateImg = (ImageView) v.findViewById(R.id.item_update_detail_state_img);
            this.mUninstallTv = (TextView) v.findViewById(R.id.item_update_uninstall);
            this.mIgnoreTv = (TextView) v.findViewById(R.id.item_update_ignore);
            this.mToDetailTv = (TextView) v.findViewById(R.id.item_update_detail_but);
        }

        public void initView(ApkData bean, int position) {
            this.bean = bean;
            this.position = position;
            this.implInfo = implAgent.getImplInfo(bean.getPackageName(), bean.getPackageName(), bean.getVersionCode());
            if (null != this.implInfo) {
                this.implInfo.setDownloadUrl(bean.getrDownloadUrl()).setIconUrl(bean.getIconUrl()).setTitle(bean.getName());
                implAgent.setImplCallback(this, implInfo);
            }
            mBt.setTag(this);
            refresh();
        }

        public void refresh() {
            initProgressButton();
        }

        void initProgressButton() {
            if (null != mBt && null != this.implInfo) {
                ImplHelper.ImplHelperRes res = ImplHelper.getImplRes(mActivity, implInfo);
                switch (implInfo.getStatus()) {
                    case ImplInfo.STATUS_RUNNING:
                        mBt.setText(res.getProgress() + "%");
                        break;
                    default:
                        mBt.setText(res.getActionText());
                        break;
                }
            }
        }

        @Override
        public void onChange(ImplInfo info) {
            LogUtils.d("UpdateFragment", "onChange," + info.getTitle() + "," + info.getStatus() + "," + info.getState());
            refresh();
        }
    }
}
