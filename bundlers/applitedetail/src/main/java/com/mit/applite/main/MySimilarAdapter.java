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
public class MySimilarAdapter extends SimilarAdapter<SimilarBean> {
    private final ImplAgent implAgent;

    public MySimilarAdapter(Context context) {
        super(context);
        implAgent = ImplAgent.getInstance(getContext().getApplicationContext());
    }

    @Override
    public ViewHolder newViewHolder(View convertView) {
        return new MyViewHolder(convertView);
    }

    class MyViewHolder extends SimilarAdapter.ViewHolder implements ImplChangeCallback {
        private ImplInfo implInfo;

        public MyViewHolder(View view) {
            super(view);
//            mTv.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    SimilarBean date = (SimilarBean)bean;
//                    ImplHelper.onClick(getContext(),
//                            implInfo,
//                            date.getrDownloadUrl(),
//                            date.getName(),
//                            date.getIconUrl(),
//                            Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + date.getName() + ".apk",
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
            super.refresh();
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