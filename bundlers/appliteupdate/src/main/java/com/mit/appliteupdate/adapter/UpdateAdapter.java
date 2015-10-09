package com.mit.appliteupdate.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import com.applite.bean.ApkBean;
import com.applite.common.AppliteUtils;
import com.applite.common.BitmapHelper;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.lidroid.xutils.BitmapUtils;
import com.mit.appliteupdate.R;
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
    private UpdateSuccessListener mListener;
    private Context mActivity;
    private List<ApkBean> mDatas;
    private ImplAgent implAgent;
    private int mCheckedItemPosition = -1;

    public interface UpdateSuccessListener {
        void removeDataPosition(String packageName);//删除已更新条目的源数据

        void ignoreDataPosition(String packageName);//忽略数据
    }

    public UpdateAdapter(Context context, List<ApkBean> mDatas, UpdateSuccessListener mListener) {
        this.mDatas = mDatas;
        this.mListener = mListener;
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
        final ApkBean data = mDatas.get(position);
        viewholder.initView(data, position);
        if (mCheckedItemPosition == position) {
            viewholder.mShowDefault.setVisibility(View.GONE);
            viewholder.mShowAll.setVisibility(View.VISIBLE);
        } else {
            viewholder.mShowDefault.setVisibility(View.VISIBLE);
            viewholder.mShowAll.setVisibility(View.GONE);
        }

        viewholder.mName.setText(data.getName());
        if (!TextUtils.isEmpty(data.getUpdateInfo())) {
            viewholder.mDefaultDetailTv.setText(data.getUpdateInfo());
            viewholder.mDetailAllDataTv.setText(data.getUpdateInfo());
        } else {
            if (!TextUtils.isEmpty(data.getUpdateTime())) {
                viewholder.mDefaultDetailTv.setText(data.getUpdateTime());
                viewholder.mDetailAllDataTv.setText(data.getUpdateInfo());
            } else {
                viewholder.mDefaultDetailTv.setText(null);
                viewholder.mDetailAllDataTv.setText(data.getUpdateInfo());
            }
        }
        if (AppliteUtils.isLoadNetworkBitmap(mActivity))
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
                ImplHelper.onClick(mActivity,
                        vh.implInfo,
                        vh.bean.getrDownloadUrl(),
                        vh.bean.getName(),
                        vh.bean.getIconUrl(),
                        path,
                        vh.bean.getApkMd5(),
                        vh);
            }
        });
        viewholder.mOpenDetailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowContent(viewholder);
            }
        });
        viewholder.mShowDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowContent(viewholder);
            }
        });
        viewholder.mShrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowContent(viewholder);
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
                mListener.ignoreDataPosition(data.getPackageName());
                mCheckedItemPosition = -1;
                AppliteSPUtils.put(mActivity, data.getPackageName(), data.getVersionCode());
            }
        });
        viewholder.mToDetailTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OSGIServiceHost) mActivity).jumptoDetail(data.getPackageName(), data.getName(), data.getIconUrl(), data.getVersionCode(), null, true);
            }
        });

        return convertView;
    }

    /**
     * 显示详细内容
     *
     * @param viewholder
     */
    private void setShowContent(ViewHolder viewholder) {
        if (mCheckedItemPosition == viewholder.position) {
            mCheckedItemPosition = -1;
        } else {
            mCheckedItemPosition = viewholder.position;
        }
        notifyDataSetChanged();
    }

    public class ViewHolder implements ImplChangeCallback {
        private ImageView mImg;
        private TextView mName;
        private TextView mApkSize;
        private TextView mVersionName;
        private Button mBt;
        private ApkBean bean;
        private ImplInfo implInfo;
        private int position;

        private LinearLayout mOpenDetailLayout;
        private LinearLayout mShrink;
        private LinearLayout mShowDefault;
        private LinearLayout mShowAll;
        private TextView mDefaultDetailTv;
        private TextView mDetailAllDataTv;
        private TextView mUninstallTv;
        private TextView mIgnoreTv;
        private TextView mToDetailTv;
        private ImageView mDetailStateImg;
        private Toast toast;

        public ViewHolder(View v) {
            this.mImg = (ImageView) v.findViewById(R.id.item_update_img);
            this.mName = (TextView) v.findViewById(R.id.item_update_name);
            this.mApkSize = (TextView) v.findViewById(R.id.item_update_size);
            this.mVersionName = (TextView) v.findViewById(R.id.item_update_versionname);
            this.mBt = (Button) v.findViewById(R.id.item_update_button);

            this.mOpenDetailLayout = (LinearLayout) v.findViewById(R.id.item_update_click);
            this.mShrink = (LinearLayout) v.findViewById(R.id.item_update_shrink);
            this.mShowDefault = (LinearLayout) v.findViewById(R.id.item_update_default);
            this.mShowAll = (LinearLayout) v.findViewById(R.id.item_update_show_all);
            this.mDefaultDetailTv = (TextView) v.findViewById(R.id.item_update_show_default_tv);
            this.mDetailAllDataTv = (TextView) v.findViewById(R.id.item_update_all_detail_tv);
            this.mUninstallTv = (TextView) v.findViewById(R.id.item_update_uninstall);
            this.mIgnoreTv = (TextView) v.findViewById(R.id.item_update_ignore);
            this.mToDetailTv = (TextView) v.findViewById(R.id.item_update_detail_but);
        }

        public void initView(ApkBean bean, int position) {
            this.bean = bean;
            this.position = position;
            this.implInfo = implAgent.getImplInfo(bean.getPackageName(), bean.getPackageName(), bean.getVersionCode());
            if (null != this.implInfo) {
                this.implInfo.setDownloadUrl(bean.getrDownloadUrl()).setIconUrl(bean.getIconUrl()).setTitle(bean.getName());
                implAgent.bindImplCallback(this, implInfo);
            }
            mBt.setTag(this);
            refresh();
        }

        public void refresh() {
            initProgressButton();
        }

        void initProgressButton() {
            if (null != mBt && null != this.implInfo) {
                ImplInfo.ImplRes res = implInfo.getImplRes();
                switch (implInfo.getStatus()) {
                    case ImplInfo.STATUS_RUNNING:
                        mBt.setText(implInfo.getProgress() + "%");
                        break;
                    case ImplInfo.STATUS_FAILED:
                        if (null == toast) {
                            toast = Toast.makeText(mActivity, "网络连接服务器发生错误或存储空间异常，请稍候重试！", Toast.LENGTH_SHORT);
                        } else {
                            toast.setText("网络连接服务器发生错误或存储空间异常，请稍候重试！");
                        }
                        toast.setGravity(Gravity.BOTTOM, 0, 80);
                        toast.show();
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
            if (info.getStatus() == ImplInfo.STATUS_INSTALLED) {
                mListener.removeDataPosition(info.getPackageName());
            }
        }
    }
}
