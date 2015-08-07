package com.mit.appliteupdate.adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.lidroid.xutils.BitmapUtils;
import com.mit.appliteupdate.main.BundleContextFactory;
import com.mit.appliteupdate.R;
import com.mit.appliteupdate.bean.DataBean;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplChangeCallback;

import java.io.File;
import java.util.List;

/**
 * Created by LSY on 15-6-23.
 */
public class UpdateAdapter extends BaseAdapter {

    private final BitmapUtils mBitmapUtil;
    private final PackageManager mPackageManager;
    private Context mActivity;
    private Context mContext;
    private List<DataBean> mDatas;
    private LayoutInflater mInflater;
    private ImplAgent implAgent;

    public UpdateAdapter(Context context, List<DataBean> mDatas) {
        this.mDatas = mDatas;
        mActivity = context;
        mBitmapUtil = BitmapHelper.getBitmapUtils(mActivity.getApplicationContext());
        mPackageManager = mActivity.getPackageManager();
        try {
            Context mContext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            this.mContext = mContext;
            mInflater = LayoutInflater.from(mContext);
            mInflater = mInflater.cloneInContext(mContext);
        } catch (Exception e) {
            e.printStackTrace();
            mInflater = LayoutInflater.from(context);
            this.mContext = context;
        }
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
            convertView = mInflater.inflate(R.layout.item_update_listview, parent, false);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }
        final DataBean data = mDatas.get(position);
        viewholder.initView(data);
        return convertView;
    }

    public class ViewHolder {
        private ImageView mImg;
        private TextView mName;
        private TextView mApkSize;
        private TextView mVersionName;
        private Button mBt;
        private DataBean bean;
        private ImplInfo implInfo;
        private ListImplCallback implCallback;

        public ViewHolder(View v) {
            this.mImg = (ImageView) v.findViewById(R.id.item_update_img);
            this.mName = (TextView) v.findViewById(R.id.item_update_name);
            this.mApkSize = (TextView) v.findViewById(R.id.item_update_size);
            this.mVersionName = (TextView) v.findViewById(R.id.item_update_versionname);
            this.mBt = (Button) v.findViewById(R.id.item_update_button);
            this.implCallback = new ListImplCallback(this);
            mBt.setTag(this);
            mBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewHolder vh = (ViewHolder) v.getTag();
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
                                        vh.bean.getmName() + ".apk",
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
            });
        }

        public void initView(DataBean bean) {
            this.bean = bean;
            this.implInfo = implAgent.getImplInfo(bean.getmPackageName(), bean.getmPackageName(), bean.getmVersionCode());
            if (null != this.implInfo) {
                this.implInfo.setDownloadUrl(bean.getmUrl()).setIconUrl(bean.getmImgUrl()).setTitle(bean.getmName());
                implAgent.setImplCallback(implCallback, implInfo);
            }
            refresh();
        }

        public void refresh() {
            mName.setText(bean.getmName());

            mBitmapUtil.configDefaultLoadingImage(mContext.getDrawable(R.drawable.apk_icon_defailt_img));
            mBitmapUtil.configDefaultLoadFailedImage(mContext.getDrawable(R.drawable.apk_icon_defailt_img));
            mBitmapUtil.display(mImg, bean.getmImgUrl());

            try {
                PackageInfo pakageinfo = mPackageManager.getPackageInfo(bean.getmPackageName(), PackageManager.GET_ACTIVITIES);
                mVersionName.setText(pakageinfo.versionName + " -> " + bean.getmVersionName());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            mApkSize.setText(AppliteUtils.bytes2kb(bean.getmSize()));
            initProgressButton();
        }

        void initProgressButton() {
            if (null != mBt && null != this.implInfo) {
                switch (implInfo.getStatus()) {
                    case Constant.STATUS_PENDING:
                        mBt.setText(implAgent.getActionText(implInfo));
                        break;
                    case Constant.STATUS_RUNNING:
                        mBt.setText(implAgent.getProgress(implInfo) + "%");
                        break;
                    case Constant.STATUS_PAUSED:
                        mBt.setText(implAgent.getStatusText(implInfo));
                        break;
                    default:
                        mBt.setText(implAgent.getActionText(implInfo));
                        break;
                }
            }
        }
    }

    class ListImplCallback implements ImplChangeCallback {
        Object tag;

        ListImplCallback(Object tag) {
            this.tag = tag;
        }

        @Override
        public void onChange(ImplInfo info) {
            ViewHolder vh = (ViewHolder) tag;
            vh.refresh();
        }
    }
}
