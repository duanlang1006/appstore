package com.mit.appliteupdate.adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
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
import com.lidroid.xutils.BitmapUtils;
import com.mit.appliteupdate.R;
import com.mit.appliteupdate.bean.ApkData;

import java.util.List;

/**
 * Created by LSY on 15-9-10.
 */
public class IgnoreAdapter extends BaseAdapter {
    private final Context mActivity;
    private final List<ApkData> mDatas;
    private final BitmapUtils mBitmapUtil;
    private final PackageManager mPackageManager;
    private final CancelIgnoreListener mListener;
    private int mCheckedItemPosition = -1;

    public interface CancelIgnoreListener {
        void cancelIgnoreListener(String PackageName);
    }

    public IgnoreAdapter(Context context, List<ApkData> mDatas, CancelIgnoreListener mListener) {
        this.mDatas = mDatas;
        this.mListener = mListener;
        mActivity = context;
        mPackageManager = mActivity.getPackageManager();
        mBitmapUtil = BitmapHelper.getBitmapUtils(mActivity.getApplicationContext());
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewholder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            convertView = inflater.inflate(R.layout.item_ignore_listview, parent, false);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }
        final ApkData data = mDatas.get(position);
        if (mCheckedItemPosition == position) {
            viewholder.mShowDefault.setVisibility(View.GONE);
            viewholder.mShrink.setVisibility(View.VISIBLE);
        } else {
            viewholder.mShowDefault.setVisibility(View.VISIBLE);
            viewholder.mShrink.setVisibility(View.GONE);
        }

        viewholder.mName.setText(data.getName());
        if (!TextUtils.isEmpty(data.getUpdateInfo())) {
            viewholder.mDefaultDetailTv.setText(data.getUpdateInfo());
            viewholder.mDetailAllDataTv.setText(data.getUpdateInfo());
        } else {
            if (!TextUtils.isEmpty(data.getUpdateTime())) {
                viewholder.mDefaultDetailTv.setText(data.getUpdateTime());
                viewholder.mDetailAllDataTv.setText(data.getUpdateInfo());
            } else {
                viewholder.mDefaultDetailTv.setText(null);
                viewholder.mDetailAllDataTv.setText(data.getUpdateInfo());
            }
        }
        mBitmapUtil.display(viewholder.mImg, data.getIconUrl());
        try {
            PackageInfo mPackageInfo = mPackageManager.getPackageInfo(data.getPackageName(), PackageManager.GET_ACTIVITIES);
            viewholder.mVersionName.setText(mPackageInfo.versionName + " -> " + data.getVersionName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        viewholder.mApkSize.setText(AppliteUtils.bytes2kb(data.getApkSize()));
        viewholder.mBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckedItemPosition = -1;
                mListener.cancelIgnoreListener(data.getPackageName());
            }
        });
        viewholder.mOpenDetailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowContent(viewholder, position);
            }
        });
        viewholder.mShowDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowContent(viewholder, position);
            }
        });
        viewholder.mShrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowContent(viewholder, position);
            }
        });
        return convertView;
    }

    /**
     * 显示详细内容
     *
     * @param viewholder
     */
    private void setShowContent(ViewHolder viewholder, int position) {
        if (mCheckedItemPosition == position) {
            mCheckedItemPosition = -1;
        } else {
            mCheckedItemPosition = position;
        }
        notifyDataSetChanged();
    }

    public class ViewHolder {
        private ImageView mImg;
        private TextView mName;
        private TextView mApkSize;
        private TextView mVersionName;
        private Button mBt;
        private LinearLayout mOpenDetailLayout;
        private LinearLayout mShrink;
        private LinearLayout mShowDefault;
        private TextView mDefaultDetailTv;
        private TextView mDetailAllDataTv;

        public ViewHolder(View v) {
            this.mImg = (ImageView) v.findViewById(R.id.item_ignore_img);
            this.mName = (TextView) v.findViewById(R.id.item_ignore_name);
            this.mApkSize = (TextView) v.findViewById(R.id.item_ignore_size);
            this.mVersionName = (TextView) v.findViewById(R.id.item_ignore_versionname);
            this.mBt = (Button) v.findViewById(R.id.item_ignore_button);
            this.mOpenDetailLayout = (LinearLayout) v.findViewById(R.id.item_ignore_click);
            this.mShrink = (LinearLayout) v.findViewById(R.id.item_ignore_shrink);
            this.mShowDefault = (LinearLayout) v.findViewById(R.id.item_ignore_default);
            this.mDefaultDetailTv = (TextView) v.findViewById(R.id.item_ignore_show_default_tv);
            this.mDetailAllDataTv = (TextView) v.findViewById(R.id.item_ignore_all_detail_tv);
        }

    }
}
