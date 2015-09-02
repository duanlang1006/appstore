package com.mit.appliteupdate.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
        ViewHolder viewholder;
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

        viewholder.mName.setText(data.getName());
        mBitmapUtil.display(viewholder.mImg, data.getIconUrl());
        try {
            final PackageInfo mPackageInfo = mPackageManager.getPackageInfo(data.getPackageName(), PackageManager.GET_ACTIVITIES);
            viewholder.mVersionName.setText(mPackageInfo.versionName + " -> " + data.getVersionName());

            viewholder.mApkSize.setText(AppliteUtils.bytes2kb(data.getApkSize()));
            viewholder.mBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewHolder vh = (ViewHolder) v.getTag();
                    MitMobclickAgent.onEvent(mActivity, "onClickButton" + vh.position);

                    String path = Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + vh.bean.getName() + ".apk";
                    LogUtils.d("UpdateFragment", "APK路径," + path);
                    if (AppliteUtils.isPackageOk(mActivity, path)) {
                        LogUtils.d("UpdateFragment", "已有完善的更新包,安装已下载的APK," + vh.bean.getName());
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
                        mActivity.startActivity(intent);
                    } else {
                        if (vh.implInfo.getStatus() == ImplInfo.STATUS_INSTALLED && mPackageInfo.versionCode < data.getVersionCode()) {
                            LogUtils.d("UpdateFragment", "更新");
                            implAgent.newDownload(vh.implInfo,
                                    vh.bean.getrDownloadUrl(),
                                    vh.bean.getName(),
                                    vh.bean.getIconUrl(),
                                    path,
                                    vh.bean.getApkMd5(),
                                    true,
                                    vh);
                        } else {
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
                    }
//                if (ImplInfo.ACTION_DOWNLOAD == implAgent.getAction(vh.implInfo)) {
//                    switch (vh.implInfo.getStatus()) {
//                        case ImplInfo.STATUS_PENDING:
//                        case ImplInfo.STATUS_RUNNING:
//                            implAgent.pauseDownload(vh.implInfo);
//                            break;
//                        case ImplInfo.STATUS_PAUSED:
//                            implAgent.resumeDownload(vh.implInfo, vh.implCallback);
//                            break;
//                        default:
//                            implAgent.newDownload(vh.implInfo,
//                                    vh.bean.getrDownloadUrl(),
//                                    vh.bean.getmName(),
//                                    vh.bean.getIconUrl(),
//                                    Constant.extenStorageDirPath,
//                                    vh.bean.getName() + ".apk",
//                                    true,
//                                    vh.implCallback);
//                            break;
//                    }
//                } else {
//                    implAgent.startActivity(vh.implInfo);
//                }
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
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
        private PackageInfo mPackageInfo;

        public ViewHolder(View v) {
            this.mImg = (ImageView) v.findViewById(R.id.item_update_img);
            this.mName = (TextView) v.findViewById(R.id.item_update_name);
            this.mApkSize = (TextView) v.findViewById(R.id.item_update_size);
            this.mVersionName = (TextView) v.findViewById(R.id.item_update_versionname);
            this.mBt = (Button) v.findViewById(R.id.item_update_button);
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
                    case ImplInfo.STATUS_INSTALLED:
                        String path = Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + bean.getName() + ".apk";
                        if (AppliteUtils.isPackageOk(mActivity, path)) {
                            LogUtils.d("updateFragment", "已有更新包");
                            mBt.setText(mActivity.getResources().getString(R.string.open));
                        } else {
                            mBt.setText(mActivity.getResources().getString(R.string.update));
                        }
                        try {
                            mPackageInfo = mPackageManager.getPackageInfo(bean.getPackageName(), PackageManager.GET_ACTIVITIES);
                            if (mPackageInfo.versionCode == bean.getVersionCode()) {
//                                LogUtils.d("updateFragment", "删除已更新的条目");
//                                mDatas.remove(position);
//                                notifyDataSetChanged();
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;
                    case ImplInfo.STATUS_PENDING:
                        mBt.setText(res.getActionText());
                        break;
                    case ImplInfo.STATUS_RUNNING:
                        mBt.setText(res.getProgress() + "%");
                        break;
                    case ImplInfo.STATUS_PAUSED:
                        mBt.setText(res.getStatusText());
                        break;
                    default:
                        mBt.setText(res.getActionText());
                        break;
                }
            }
        }

        @Override
        public void onChange(ImplInfo info) {
            refresh();
        }
    }
}
