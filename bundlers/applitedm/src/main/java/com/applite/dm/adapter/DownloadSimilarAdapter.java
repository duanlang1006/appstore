package com.applite.dm.adapter;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;

import com.applite.dm.R;
import com.applite.common.Constant;
import com.applite.similarview.SimilarAdapter;
import com.applite.similarview.SimilarBean;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;

import java.io.File;

/**
 * Created by wanghaochen on 15-9-11.
 */
public class DownloadSimilarAdapter extends SimilarAdapter {
    private final ImplAgent implAgent;

    public DownloadSimilarAdapter(Context context) {
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
//            mTv.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    ImplHelper.onClick(getContext(),
//                            implInfo,
//                            bean.getrDownloadUrl(),
//                            bean.getName(),
//                            bean.getIconUrl(),
//                            Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + bean.getName() + ".apk",
//                            null,
//                            (ImplChangeCallback) v.getTag());
//                }
//            });
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.item_similar_img) {
                mSimilarAPKDetailListener.onClickIcon(bean);
            } else if (v.getId() == R.id.item_similar_name) {
                mSimilarAPKDetailListener.onClickName(bean);
            } else if (v.getId() == R.id.item_similar_install_tv) {
                SimilarBean date = (SimilarBean) bean;
                mSimilarAPKDetailListener.onClickButton(implInfo, date, (ImplChangeCallback) v.getTag());
            }
        }

        @Override
        public void initView(Object data) {
            SimilarBean bean = (SimilarBean) data;
            implInfo = implAgent.getImplInfo(bean.getPackageName(), bean.getPackageName(), bean.getVersionCode());
            if (null != implInfo) {
                implAgent.bindImplCallback(this, implInfo);
            }
            super.initView(data);
        }

        @Override
        public void refresh() {
//            super.refresh();
            if (null != mTv && null != this.implInfo) {
                ImplInfo.ImplRes res = implInfo.getImplRes();
                switch (implInfo.getStatus()) {
                    case ImplInfo.STATUS_PENDING:
                        mTv.setText(res.getActionText());
                        break;
                    case ImplInfo.STATUS_RUNNING:
                        mTv.setText(implInfo.getProgress() + "%");
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

        @Override
        public void onChange(ImplInfo info) {
            refresh();
        }
    }
}