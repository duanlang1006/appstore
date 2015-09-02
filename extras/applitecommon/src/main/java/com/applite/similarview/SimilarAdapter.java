package com.applite.similarview;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.applite.common.BitmapHelper;
import com.applite.common.Constant;
import com.applite.common.R;
import com.lidroid.xutils.BitmapUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;

import java.io.File;
import java.util.List;

/**
 * Created by LSY on 15-8-12.
 */
public class SimilarAdapter extends BaseAdapter {

    private final Context mContext;
    private List<SimilarBean> mSimilarBeans;
    private final BitmapUtils mBitmapUtil;
    private final ImplAgent implAgent;
    private SimilarAPKDetailListener mSimilarAPKDetailListener;

    public interface SimilarAPKDetailListener {
        void refreshDetail(SimilarBean bean);
    }

    public SimilarAdapter(Context context) {
        mContext = context;
        mBitmapUtil = BitmapHelper.getBitmapUtils(mContext.getApplicationContext());
        implAgent = ImplAgent.getInstance(mContext.getApplicationContext());
    }

    @Override
    public int getCount() {
        return mSimilarBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return mSimilarBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.item_similar, parent, false);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }
        final SimilarBean data = mSimilarBeans.get(position);

        viewholder.initView(data);
        mBitmapUtil.display(viewholder.mImg, data.getIconUrl());
        viewholder.mName.setText(data.getName());
        viewholder.mImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSimilarAPKDetailListener.refreshDetail(data);
            }
        });
        viewholder.mTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ViewHolder vh = (ViewHolder) v.getTag();
                ImplHelper.onClick(mContext,
                        vh.implInfo,
                        data.getrDownloadUrl(),
                        data.getName(),
                        data.getIconUrl(),
                        Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + data.getName() + ".apk",
                        null,
                        vh);
//                if (ImplInfo.ACTION_DOWNLOAD == implAgent.getAction(vh.implInfo)) {
//                    switch (vh.implInfo.getStatus()) {
//                        case ImplInfo.STATUS_PENDING:
//                        case ImplInfo.STATUS_RUNNING:
//                            implAgent.pauseDownload(vh.implInfo);
//                            break;
//                        case ImplInfo.STATUS_PAUSED:
//                            implAgent.resumeDownload(vh.implInfo, vh);
//                            break;
//                        default:
//                            implAgent.newDownload(vh.implInfo,
//                                    data.getmDownloadUrl(),
//                                    data.getmName(),
//                                    data.getmImgUrl(),
//                                    Constant.extenStorageDirPath,
//                                    vh.bean.getmName() + ".apk",
//                                    true,
//                                    vh);
//                            break;
//                    }
//                } else {
//                    implAgent.startActivity(vh.implInfo);
//                }
            }
        });
        return convertView;
    }

    public void setData(List<SimilarBean> data, SimilarAPKDetailListener listener){
        mSimilarBeans = data;
        mSimilarAPKDetailListener = listener;
    }

    class ViewHolder implements ImplChangeCallback {
        private TextView mName;
        private ImageView mImg;
        private TextView mTv;
        private ImplInfo implInfo;
        private SimilarBean bean;

        public ViewHolder(View view) {
            this.mImg = (ImageView) view.findViewById(R.id.item_similar_img);
            this.mName = (TextView) view.findViewById(R.id.item_similar_name);
            this.mTv = (TextView) view.findViewById(R.id.item_similar_install_tv);
        }

        public void initView(SimilarBean data) {
            this.bean = data;
            this.implInfo = implAgent.getImplInfo(data.getPackageName(), data.getPackageName(), data.getVersionCode());
            if (null != this.implInfo) {
                this.implInfo.setDownloadUrl(data.getrDownloadUrl()).setIconUrl(data.getIconUrl()).setTitle(data.getName());
                implAgent.setImplCallback(this, implInfo);
            }
            mTv.setTag(this);
            refresh();
        }

        public void refresh() {
            initProgressButton();
        }

        void initProgressButton() {
            if (null != mTv && null != this.implInfo) {
                ImplHelper.ImplHelperRes res = ImplHelper.getImplRes(mContext,implInfo);
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

        @Override
        public void onChange(ImplInfo info) {
            refresh();
        }
    }
}
