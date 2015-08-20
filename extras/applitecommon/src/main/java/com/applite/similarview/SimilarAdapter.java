package com.applite.similarview;

import android.content.Context;
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
import com.mit.impl.ImplInfo;

import java.util.List;

/**
 * Created by LSY on 15-8-12.
 */
public class SimilarAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<SimilarBean> mSimilarBeans;
    private final BitmapUtils mBitmapUtil;
    private final ImplAgent implAgent;
    private final SimilarAPKDetailListener mSimilarAPKDetailListener;

    public interface SimilarAPKDetailListener {
        void refreshDetail(SimilarBean bean);
    }

    public SimilarAdapter(Context context, List<SimilarBean> data, SimilarAPKDetailListener listener) {
        mContext = context;
        mSimilarBeans = data;
        mSimilarAPKDetailListener = listener;
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
        mBitmapUtil.display(viewholder.mImg, data.getmImgUrl());
        viewholder.mName.setText(data.getmName());
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

    class ViewHolder {
        private TextView mName;
        private ImageView mImg;
        private TextView mTv;
        private ImplInfo implInfo;
        private SimilarBean bean;
        private ListImplCallback implCallback;

        public ViewHolder(View view) {
            this.mImg = (ImageView) view.findViewById(R.id.item_similar_img);
            this.mName = (TextView) view.findViewById(R.id.item_similar_name);
            this.mTv = (TextView) view.findViewById(R.id.item_similar_install_tv);
            this.implCallback = new ListImplCallback(this);
        }

        public void initView(SimilarBean data) {
            this.bean = data;
            this.implInfo = implAgent.getImplInfo(data.getmPackageName(), data.getmPackageName(), data.getmVersionCode());
            ;
            if (null != this.implInfo) {
                this.implInfo.setDownloadUrl(data.getmDownloadUrl()).setIconUrl(data.getmImgUrl()).setTitle(data.getmName());
                implAgent.setImplCallback(implCallback, implInfo);
            }
            mTv.setTag(this);
            refresh();
        }

        public void refresh() {
            initProgressButton();
        }

        void initProgressButton() {
            if (null != mTv && null != this.implInfo) {
                switch (implInfo.getStatus()) {
                    case Constant.STATUS_PENDING:
                        mTv.setText(implAgent.getActionText(implInfo));
                        break;
                    case Constant.STATUS_RUNNING:
                        mTv.setText(implAgent.getProgress(implInfo) + "%");
                        break;
                    case Constant.STATUS_PAUSED:
                        mTv.setText(implAgent.getStatusText(implInfo));
                        break;
                    default:
                        mTv.setText(implAgent.getActionText(implInfo));
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
