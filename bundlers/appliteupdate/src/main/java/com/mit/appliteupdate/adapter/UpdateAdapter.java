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
import com.mit.appliteupdate.R;
import com.mit.appliteupdate.bean.DataBean;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplChangeCallback;
import com.mit.mitupdatesdk.MitMobclickAgent;

import java.util.List;

/**
 * Created by LSY on 15-6-23.
 */
public class UpdateAdapter extends BaseAdapter {

    private final BitmapUtils mBitmapUtil;
    private final PackageManager mPackageManager;
    private Context mActivity;
    private List<DataBean> mDatas;
    private ImplAgent implAgent;

    public UpdateAdapter(Context context, List<DataBean> mDatas) {
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
        final DataBean data = mDatas.get(position);
        viewholder.initView(data,position);

        viewholder.mName.setText(data.getmName());
        mBitmapUtil.display(viewholder.mImg, data.getmImgUrl());
        try {
            PackageInfo mPackageInfo = mPackageManager.getPackageInfo(data.getmPackageName(), PackageManager.GET_ACTIVITIES);
            viewholder.mVersionName.setText(mPackageInfo.versionName + " -> " + data.getmVersionName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        viewholder.mApkSize.setText(AppliteUtils.bytes2kb(data.getmSize()));
        viewholder.mBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder vh = (ViewHolder) v.getTag();
                MitMobclickAgent.onEvent(mActivity, "onClickButton" + vh.position);
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
                    implAgent.startActivity(vh.implInfo);
                }
            }
        });
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
        private int position;

        public ViewHolder(View v) {
            this.mImg = (ImageView) v.findViewById(R.id.item_update_img);
            this.mName = (TextView) v.findViewById(R.id.item_update_name);
            this.mApkSize = (TextView) v.findViewById(R.id.item_update_size);
            this.mVersionName = (TextView) v.findViewById(R.id.item_update_versionname);
            this.mBt = (Button) v.findViewById(R.id.item_update_button);
            this.implCallback = new ListImplCallback(this);
        }

        public void initView(DataBean bean,int position) {
            this.bean = bean;
            this.position = position;
            this.implInfo = implAgent.getImplInfo(bean.getmPackageName(), bean.getmPackageName(), bean.getmVersionCode());
            if (null != this.implInfo) {
                this.implInfo.setDownloadUrl(bean.getmUrl()).setIconUrl(bean.getmImgUrl()).setTitle(bean.getmName());
                implAgent.setImplCallback(implCallback, implInfo);
            }
            mBt.setTag(this);
            refresh();
        }

        public void refresh() {
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
