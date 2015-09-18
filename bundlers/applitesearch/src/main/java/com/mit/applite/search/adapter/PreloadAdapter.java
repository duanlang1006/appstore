package com.mit.applite.search.adapter;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applite.common.AppliteUtils;
import com.applite.common.BitmapHelper;
import com.applite.common.Constant;
import com.lidroid.xutils.BitmapUtils;
import com.mit.applite.search.R;
import com.mit.applite.search.bean.SearchBean;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;
import com.osgi.extra.OSGIServiceHost;

import java.io.File;
import java.util.List;

/**
 * Created by LSY on 15-6-11.
 */
public class PreloadAdapter extends BaseAdapter {

    private final Context mActivity;
    private BitmapUtils mBitmapUtil;
    private ImplAgent implAgent;
    private int SHOW_ICON_NUMBER;
    private LayoutInflater mInflater;
    private List<SearchBean> mPreloadData;

    public PreloadAdapter(Context context, List<SearchBean> data, int i) {
        mPreloadData = data;
        mActivity = context;
        mInflater = LayoutInflater.from(context);
        SHOW_ICON_NUMBER = i;
        implAgent = ImplAgent.getInstance(mActivity.getApplicationContext());
        mBitmapUtil = BitmapHelper.getBitmapUtils(mActivity.getApplicationContext());
    }

    @Override
    public int getCount() {
        return mPreloadData.size();
    }

    @Override
    public Object getItem(int position) {
        return mPreloadData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;
        /* 将convertView封装在ViewHodler中，减少系统内存占用 */
        if (convertView == null) {
            /* convertView为空则初始化 */
            convertView = mInflater.inflate(R.layout.item_preload_listview, parent, false);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            // 不为空则直接使用已有的封装类
            viewholder = (ViewHolder) convertView.getTag();
        }
        final SearchBean data = mPreloadData.get(position);
        viewholder.initView(data, position);

        if (position + 1 > SHOW_ICON_NUMBER) {
            viewholder.mSize.setVisibility(View.GONE);
            viewholder.mBt.setVisibility(View.GONE);
            viewholder.mIcon.setVisibility(View.GONE);
            viewholder.mClickItem.setClickable(false);
            viewholder.mName.setPadding(20, 10, 10, 10);
        } else {
            viewholder.mName.setPadding(0, 0, 0, 0);
            viewholder.mSize.setVisibility(View.VISIBLE);
            viewholder.mBt.setVisibility(View.VISIBLE);
            viewholder.mIcon.setVisibility(View.VISIBLE);

            if (AppliteUtils.isLoadNetworkBitmap(mActivity))
                mBitmapUtil.display(viewholder.mIcon, data.getmImgUrl());
            viewholder.mSize.setText(AppliteUtils.bytes2kb(Long.parseLong(data.getmApkSize())));
            viewholder.mClickItem.setClickable(true);
            viewholder.mClickItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((OSGIServiceHost) mActivity).jumptoDetail(data.getmPackageName(),
                            data.getmName(),
                            data.getmImgUrl(),
                            data.getmVersionCode(),
                            null,
                            true);
                }
            });
        }
        viewholder.mName.setText(data.getmName());
        viewholder.mBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ViewHolder vh = (ViewHolder) v.getTag();
                ImplHelper.onClick(mActivity, vh.implInfo, vh.bean.getmDownloadUrl(),
                        vh.bean.getmName(),
                        vh.bean.getmImgUrl(),
                        Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + vh.bean.getmName() + ".apk",
                        null,
                        vh);
            }
        });
        return convertView;
    }

    class ViewHolder implements ImplChangeCallback {
        TextView mSize;
        TextView mName;
        ImageView mIcon;
        Button mBt;
        LinearLayout mClickItem;
        private SearchBean bean;
        private ImplInfo implInfo;
        private int mPosition;

        public ViewHolder(View view) {
            this.mName = (TextView) view.findViewById(R.id.item_pre_name);
            this.mSize = (TextView) view.findViewById(R.id.item_pre_size);
            this.mIcon = (ImageView) view.findViewById(R.id.item_pre_icon_img);
            this.mBt = (Button) view.findViewById(R.id.item_pre_install_but);
            this.mClickItem = (LinearLayout) view.findViewById(R.id.item_pre_click_layout);
        }

        public void initView(SearchBean data, int position) {
            this.bean = data;
            this.mPosition = position;
            this.implInfo = implAgent.getImplInfo(data.getmPackageName(), data.getmPackageName(), data.getmVersionCode());
            if (null != this.implInfo) {
                this.implInfo.setDownloadUrl(data.getmDownloadUrl()).setIconUrl(data.getmImgUrl()).setTitle(data.getmName());
                implAgent.bindImplCallback(this, implInfo);
            }
            mBt.setTag(this);
            refresh();
        }

        public void refresh() {
            if (this.mPosition + 1 <= SHOW_ICON_NUMBER) {
                initButton();
            }
        }

        void initButton() {
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

        public ImplInfo getImplInfo() {
            return implInfo;
        }

        @Override
        public void onChange(ImplInfo info) {
            refresh();
        }
    }
}
