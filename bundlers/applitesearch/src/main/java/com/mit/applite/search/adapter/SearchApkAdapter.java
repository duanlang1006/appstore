package com.mit.applite.search.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.mit.applite.search.R;
import com.mit.applite.search.bean.SearchBean;
import com.mit.applite.search.main.BundleContextFactory;
import com.mit.applite.search.utils.SearchUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplListener;

import net.tsz.afinal.FinalBitmap;

import java.io.File;
import java.util.List;

/**
 * Created by LSY on 15-5-27.
 */
public class SearchApkAdapter extends BaseAdapter {

    private final FinalBitmap mFinalBitmap;
    private final UpdateInatsllButtonText mListener;
    private LayoutInflater mInflater;
    private Context context;
    public List<SearchBean> mSearchBeans;
    private Context mActivity;
    private ImplAgent implAgent;

    public interface UpdateInatsllButtonText {
        void updateText();
    }

    public SearchApkAdapter(Context context, List<SearchBean> mSearchBeans, UpdateInatsllButtonText listener) {
        mListener = listener;
        this.mSearchBeans = mSearchBeans;
        mFinalBitmap = FinalBitmap.create(context);
        mActivity = context;
        try {
            Context mContext = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            this.context = mContext;
            mInflater = LayoutInflater.from(mContext);
            mInflater = mInflater.cloneInContext(mContext);
        } catch (Exception e) {
            e.printStackTrace();
            mInflater = LayoutInflater.from(context);
            this.context = context;
        }
        implAgent = ImplAgent.getInstance(mActivity.getApplicationContext());
    }

    @Override
    public int getCount() {
        return mSearchBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return mSearchBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_search_listview, parent, false);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }
        final SearchBean data = mSearchBeans.get(position);
        mFinalBitmap.display(viewholder.mImg, data.getmImgUrl(), BitmapFactory.decodeResource(context.getResources(), R.drawable.buffer));
        viewholder.initView(data);

        viewholder.mBt.setOnClickListener(new View.OnClickListener() {

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
                        mActivity.startActivity(implAgent.getActionIntent(vh.implInfo));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        viewholder.mXing.setRating(Float.parseFloat(data.getmXing()) / 2.0f);
        return convertView;
    }

    public class ViewHolder {
        public LinearLayout mToDetail;
        public ImageView mImg;
        public RatingBar mXing;
        public TextView mName;
        public TextView mDownloadNumber;
        public TextView mApkSize;
        public TextView mVersionName;
        public Button mBt;
        private ImplInfo implInfo;
        private SearchBean bean;
        private ListImplCallback implCallback;

        public ViewHolder(View v) {
            this.mToDetail = (LinearLayout) v.findViewById(R.id.list_item_to_detail);
            this.mImg = (ImageView) v.findViewById(R.id.list_item_img);
            this.mName = (TextView) v.findViewById(R.id.list_item_name);
            this.mXing = (RatingBar) v.findViewById(R.id.list_item_xing);
            this.mDownloadNumber = (TextView) v.findViewById(R.id.list_item_number);
            this.mApkSize = (TextView) v.findViewById(R.id.list_item_size);
            this.mVersionName = (TextView) v.findViewById(R.id.list_item_versionname);
            this.mBt = (Button) v.findViewById(R.id.list_item_bt);
            this.implCallback = new ListImplCallback(this);
        }

        public void initView(SearchBean data) {
            this.bean = data;
            this.implInfo = implAgent.getImplInfo(data.getmPackageName(), data.getmPackageName(), data.getmVersionCode());
            ;
            if (null != this.implInfo) {
                this.implInfo.setDownloadUrl(data.getmDownloadUrl()).setIconUrl(data.getmImgUrl()).setTitle(data.getmName());
                implAgent.setImplCallback(implCallback, implInfo);
            }
            mBt.setTag(this);
            refresh();
        }

        public void refresh() {
            mName.setText(bean.getmName());
            mApkSize.setText(AppliteUtils.bytes2kb(Long.parseLong(bean.getmApkSize())));
            mDownloadNumber.setText(
                    SearchUtils.getDownloadNumber(context, Integer.parseInt(bean.getmDownloadNumber())) +
                            context.getResources().getString(R.string.download_number));
            mVersionName.setText(context.getResources().getString(R.string.version) +
                    bean.getmVersionName());
            mToDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SearchUtils.toDetailFragment(bean.getmPackageName(), bean.getmName(), bean.getmImgUrl());
                }
            });
//            mProgressButton.setText(implAgent.getProgress(implInfo) + "");
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

        public ImplInfo getImplInfo() {
            return implInfo;
        }
    }

    class ListImplCallback extends ImplListener {
        Object tag;

        ListImplCallback(Object tag) {
            super();
            this.tag = tag;
        }

        @Override
        public void onStart(ImplInfo info) {
            super.onStart(info);
            ViewHolder vh = (ViewHolder) tag;
            vh.refresh();
        }

        @Override
        public void onCancelled(ImplInfo info) {
            super.onCancelled(info);
            ViewHolder vh = (ViewHolder) tag;
            vh.refresh();
        }

        @Override
        public void onLoading(ImplInfo info, long total, long current, boolean isUploading) {
            super.onLoading(info, total, current, isUploading);
            ViewHolder vh = (ViewHolder) tag;
            vh.refresh();
        }

        @Override
        public void onSuccess(ImplInfo info, File file) {
            super.onSuccess(info, file);
            ViewHolder vh = (ViewHolder) tag;
            vh.refresh();
        }

        @Override
        public void onFailure(ImplInfo info, Throwable t, String msg) {
            super.onFailure(info, t, msg);
            ViewHolder vh = (ViewHolder) tag;
            vh.refresh();
        }

        @Override
        public void onInstallSuccess(ImplInfo info) {
            super.onInstallSuccess(info);
            ViewHolder vh = (ViewHolder) tag;
            vh.refresh();
        }

        @Override
        public void onInstalling(ImplInfo info) {
            super.onInstalling(info);
            ViewHolder vh = (ViewHolder) tag;
            vh.refresh();
        }

        @Override
        public void onInstallFailure(ImplInfo info, int errorCode) {
            super.onInstallFailure(info, errorCode);
            ViewHolder vh = (ViewHolder) tag;
            vh.refresh();
        }

        @Override
        public void onUninstallSuccess(ImplInfo info) {
            super.onUninstallSuccess(info);
            ViewHolder vh = (ViewHolder) tag;
            vh.refresh();
        }

        @Override
        public void onUninstalling(ImplInfo info) {
            super.onUninstalling(info);
            ViewHolder vh = (ViewHolder) tag;
            vh.refresh();
        }

        @Override
        public void onUninstallFailure(ImplInfo info, int errorCode) {
            super.onUninstallFailure(info, errorCode);
            ViewHolder vh = (ViewHolder) tag;
            vh.refresh();
        }
    }
}
