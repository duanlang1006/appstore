package com.mit.applite.search.adapter;

import android.content.Context;
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
import com.mit.applite.search.utils.SearchUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplInfo;
import com.osgi.extra.OSGIServiceHost;

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
        return convertView;
    }

    class ViewHolder {
        TextView mSize;
        TextView mName;
        ImageView mIcon;
        Button mBt;
        LinearLayout mClickItem;
        private SearchBean bean;
        private ListImplCallback implCallback;
        private ImplInfo implInfo;
        private int mPosition;

        public ViewHolder(View view) {
            this.mName = (TextView) view.findViewById(R.id.item_pre_name);
            this.mSize = (TextView) view.findViewById(R.id.item_pre_size);
            this.mIcon = (ImageView) view.findViewById(R.id.item_pre_icon_img);
            this.mBt = (Button) view.findViewById(R.id.item_pre_install_but);
            this.mClickItem = (LinearLayout) view.findViewById(R.id.item_pre_click_layout);
            this.implCallback = new ListImplCallback(this);
        }

        public void initView(SearchBean data, int position) {
            this.bean = data;
            this.mPosition = position;
            this.implInfo = implAgent.getImplInfo(data.getmPackageName(), data.getmPackageName(), data.getmVersionCode());
            if (null != this.implInfo) {
                this.implInfo.setDownloadUrl(data.getmDownloadUrl()).setIconUrl(data.getmImgUrl()).setTitle(data.getmName());
                implAgent.setImplCallback(implCallback, implInfo);
            }
            mBt.setTag(this);
            refresh();
        }

        public void refresh() {
            if (this.mPosition + 1 > SHOW_ICON_NUMBER) {
                mSize.setVisibility(View.GONE);
                mBt.setVisibility(View.GONE);
                mIcon.setVisibility(View.GONE);
                mClickItem.setClickable(false);
                mName.setPadding(20, 10, 10, 10);
            } else {
                mName.setPadding(0, 0, 0, 0);
                mSize.setVisibility(View.VISIBLE);
                mBt.setVisibility(View.VISIBLE);
                mIcon.setVisibility(View.VISIBLE);

                mBitmapUtil.display(mIcon, bean.getmImgUrl());
                mSize.setText(AppliteUtils.bytes2kb(Long.parseLong(bean.getmApkSize())));
                mClickItem.setClickable(true);
                mClickItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SearchUtils.toDetailFragment((OSGIServiceHost) mActivity,
                                bean.getmPackageName(), bean.getmName(), bean.getmImgUrl());
                    }
                });
                initButton();
            }
            mName.setText(bean.getmName());
        }

        void initButton() {
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
