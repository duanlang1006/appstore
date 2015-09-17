package com.mit.applite.search.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;
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
import com.applite.common.BitmapHelper;
import com.applite.common.Constant;
import com.lidroid.xutils.BitmapUtils;
import com.mit.applite.search.R;
import com.mit.applite.search.bean.SearchBean;
import com.mit.applite.search.utils.SearchUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplChangeCallback;
import com.osgi.extra.OSGIServiceHost;

import java.io.File;
import java.util.List;

/**
 * Created by LSY on 15-5-27.
 */
public class SearchApkAdapter extends BaseAdapter {

    private final UpdateInatsllButtonText mListener;
    private BitmapUtils mBitmapUtil;
    private List<SearchBean> mSearchBeans;
    private Context mActivity;
    private ImplAgent implAgent;

    public interface UpdateInatsllButtonText {
        void updateText();
    }

    public SearchApkAdapter(Context context, List<SearchBean> mSearchBeans, UpdateInatsllButtonText listener) {
        mListener = listener;
        this.mSearchBeans = mSearchBeans;
        mActivity = context;
        mBitmapUtil = BitmapHelper.getBitmapUtils(mActivity.getApplicationContext());
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
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            convertView = inflater.inflate(R.layout.item_search_listview, parent, false);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }
        final SearchBean data = mSearchBeans.get(position);

        viewholder.initView(data);
        if (AppliteUtils.isLoadNetworkBitmap(mActivity))
            mBitmapUtil.display(viewholder.mImg, data.getmImgUrl());
        viewholder.mName.setText(data.getmName());
        viewholder.mApkSize.setText(AppliteUtils.bytes2kb(Long.parseLong(data.getmApkSize())));
        viewholder.mDownloadNumber.setText(
                SearchUtils.getDownloadNumber(mActivity, Integer.parseInt(data.getmDownloadNumber())) +
                        mActivity.getResources().getString(R.string.download_number));
        viewholder.mVersionName.setText(mActivity.getResources().getString(R.string.version) +
                data.getmVersionName());
        viewholder.mToDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OSGIServiceHost) mActivity).jumptoDetail(data.getmPackageName(), data.getmName(), data.getmImgUrl(), data.getmVersionCode(), true);
            }
        });
        viewholder.mBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ViewHolder vh = (ViewHolder) v.getTag();
                ImplHelper.onClick(mActivity,
                        vh.implInfo,
                        vh.bean.getmDownloadUrl(),
                        vh.bean.getmName(),
                        vh.bean.getmImgUrl(),
                        Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + vh.bean.getmName() + ".apk",
                        null,
                        vh);
            }
        });
        viewholder.mXing.setRating(Float.parseFloat(data.getmXing()) / 2.0f);
        return convertView;
    }

    public class ViewHolder implements ImplChangeCallback {
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

        public ViewHolder(View v) {
            this.mToDetail = (LinearLayout) v.findViewById(R.id.list_item_to_detail);
            this.mImg = (ImageView) v.findViewById(R.id.list_item_img);
            this.mName = (TextView) v.findViewById(R.id.list_item_name);
            this.mXing = (RatingBar) v.findViewById(R.id.list_item_xing);
            this.mDownloadNumber = (TextView) v.findViewById(R.id.list_item_number);
            this.mApkSize = (TextView) v.findViewById(R.id.list_item_size);
            this.mVersionName = (TextView) v.findViewById(R.id.list_item_versionname);
            this.mBt = (Button) v.findViewById(R.id.list_item_bt);
        }

        public void initView(SearchBean data) {
            this.bean = data;
            this.implInfo = implAgent.getImplInfo(data.getmPackageName(), data.getmPackageName(), data.getmVersionCode());
            if (null != this.implInfo) {
                this.implInfo.setDownloadUrl(data.getmDownloadUrl()).setIconUrl(data.getmImgUrl()).setTitle(data.getmName());
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
                    case ImplInfo.STATUS_PENDING:
                        mBt.setText(res.getActionText());
                        break;
                    case ImplInfo.STATUS_RUNNING:
                        mBt.setText(implInfo.getProgress() + "%");
                        break;
                    case ImplInfo.STATUS_PAUSED:
                        mBt.setText(res.getStatusText());
                        break;
                    default:
                        mBt.setText(res.getActionText());
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
