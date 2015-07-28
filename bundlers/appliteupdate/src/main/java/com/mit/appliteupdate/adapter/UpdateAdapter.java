package com.mit.appliteupdate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.mit.appliteupdate.main.BundleContextFactory;
import com.mit.appliteupdate.R;
import com.mit.appliteupdate.bean.DataBean;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplListener;

import net.tsz.afinal.FinalBitmap;

import java.io.File;
import java.util.List;

/**
 * Created by LSY on 15-6-23.
 */
public class UpdateAdapter extends BaseAdapter {

    private Context mActivity;
    private FinalBitmap mFinalBitmap;
    private Context mContext;
    private List<DataBean> mDatas;
    private LayoutInflater mInflater;
    private ImplAgent implAgent;

    public UpdateAdapter(Context context, List<DataBean> mDatas) {
        this.mDatas = mDatas;
        mFinalBitmap = FinalBitmap.create(context);
        mActivity = context;
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
                    ViewHolder vh = (ViewHolder)v.getTag();
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

        public void initView(DataBean bean){
            this.bean = bean;
            this.implInfo = implAgent.getImplInfo(bean.getmPackageName(),bean.getmPackageName(),bean.getmVersionCode());
            this.implInfo.setDownloadUrl(bean.getmUrl()).setIconUrl(bean.getmImgUrl()).setTitle(bean.getmName());
            implAgent.setImplCallback(implCallback, implInfo);
            refresh();
        }

        public void refresh(){
            mName.setText(bean.getmName());
            mFinalBitmap.display(mImg, bean.getmImgUrl());
            mVersionName.setText("V " + bean.getmVersionName());
            mApkSize.setText(AppliteUtils.bytes2kb(bean.getmSize()));
            initProgressButton();
        }

        void initProgressButton() {
            if (null != mBt ){
                switch (implInfo.getStatus()){
                    case Constant.STATUS_PENDING:
                        mBt.setText(implAgent.getActionText(implInfo));
                        break;
                    case Constant.STATUS_RUNNING:
                    case Constant.STATUS_PAUSED:
                        mBt.setText(implAgent.getProgress(implInfo)+"%");
                        break;
                    default:
                        mBt.setText(implAgent.getActionText(implInfo));
                        break;
                }
            }
        }
    }

    class ListImplCallback extends ImplListener {
        Object tag ;

        ListImplCallback(Object tag) {
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
