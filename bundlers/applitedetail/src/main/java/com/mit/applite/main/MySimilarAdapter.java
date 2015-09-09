package com.mit.applite.main;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import com.applite.common.Constant;
import com.applite.similarview.SimilarAdapter;
import com.applite.similarview.SimilarBean;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;

import java.io.File;

/**
* Created by LSY on 15-8-12.
*/
public class MySimilarAdapter extends SimilarAdapter {
    private final ImplAgent implAgent;

    public MySimilarAdapter(Context context) {
        super(context);
        implAgent = ImplAgent.getInstance(getContext().getApplicationContext());
    }

    @Override
    public SimilarAdapter.ViewHolder newViewHolder(View convertView) {
        return new MyViewHolder(convertView);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    class MyViewHolder extends SimilarAdapter.ViewHolder implements ImplChangeCallback {
        private ImplInfo implInfo;

        public MyViewHolder(View view) {
            super(view);
            mTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImplHelper.onClick(getContext(),
                            implInfo,
                            bean.getrDownloadUrl(),
                            bean.getName(),
                            bean.getIconUrl(),
                            Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + bean.getName() + ".apk",
                            null,
                            (ImplChangeCallback) v.getTag());
                }
            });
        }

        @Override
        public void initView(SimilarBean data) {
            implInfo = implAgent.getImplInfo(data.getPackageName(), data.getPackageName(), data.getVersionCode());
            if (null != implInfo) {
                implAgent.setImplCallback(this, implInfo);
            }
            super.initView(data);
        }

        @Override
        public void refresh() {
            super.refresh();
            if (null != mTv && null != this.implInfo) {
                ImplHelper.ImplHelperRes res = ImplHelper.getImplRes(getContext(),implInfo);
                switch (implInfo.getStatus()) {
                    case ImplInfo.STATUS_PENDING:
                        mTv.setText(res.getActionText());
                        break;
                    case ImplInfo.STATUS_RUNNING:
                        mTv.setText(res.getProgress() + "%");
                        break;
                    case ImplInfo.STATUS_PAUSED:
                        mTv.setText(res.getStatusText());
                        break;
                    default:
                        mTv.setText(res.getActionText());
                        break;
                }
            }
        }
        //        public ViewHolder(View view) {
//            this.mImg = (ImageView) view.findViewById(R.id.item_similar_img);
//            this.mName = (TextView) view.findViewById(R.id.item_similar_name);
//            this.mTv = (TextView) view.findViewById(R.id.item_similar_install_tv);
//        }

//        public void initView(SimilarBean data) {
//            this.bean = data;
//            this.implInfo = implAgent.getImplInfo(data.getPackageName(), data.getPackageName(), data.getVersionCode());
//            if (null != this.implInfo) {
//                this.implInfo.setDownloadUrl(data.getrDownloadUrl()).setIconUrl(data.getIconUrl()).setTitle(data.getName());
//                implAgent.setImplCallback(this, implInfo);
//            }
//            mTv.setTag(this);
//            refresh();
//        }

//        public void refresh() {
//            initProgressButton();
//        }

//        void initProgressButton() {
//            if (null != mTv && null != this.implInfo) {
//                ImplHelper.ImplHelperRes res = ImplHelper.getImplRes(mContext,implInfo);
//                switch (implInfo.getStatus()) {
//                    case ImplInfo.STATUS_PENDING:
//                        mTv.setText(res.getActionText());
//                        break;
//                    case ImplInfo.STATUS_RUNNING:
//                        mTv.setText(res.getProgress() + "%");
//                        break;
//                    case ImplInfo.STATUS_PAUSED:
//                        mTv.setText(res.getStatusText());
//                        break;
//                    default:
//                        mTv.setText(res.getActionText());
//                        break;
//                }
//            }
//        }

        @Override
        public void onChange(ImplInfo info) {
            refresh();
        }
    }
}