/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.applite.dm.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.applite.common.AppliteUtils;
import com.applite.dm.R;
import com.applite.dm.main.DownloadListFragment;
import com.applite.view.CustomProgressBar;
import com.lidroid.xutils.BitmapUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplLog;

import java.util.List;

public class DownloadAdapter extends ArrayAdapter implements View.OnClickListener {
    private Context mContext;
    private int mLayoutId;
    private BitmapUtils mBitmapHelper;
    private ImplAgent implAgent;
    private DownloadListFragment.DownloadListener mListener;

    public DownloadAdapter(Context context,
                           int resource,
                           List<ImplInfo> implInfoList,
                           BitmapUtils bitmapHelper,
                           DownloadListFragment.DownloadListener listener) {
        super(context, resource, implInfoList);
        mContext = context;
        mBitmapHelper = bitmapHelper;
        mListener = listener;
        implAgent = ImplAgent.getInstance(context.getApplicationContext());
        this.mLayoutId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            view = LayoutInflater.from(mContext).inflate(mLayoutId, null);
            view.setTag(new ViewHolder(view));
        }
        ViewHolder vh = (ViewHolder) view.getTag();
        vh.initView((ImplInfo) getItem(position));
        vh.actionBtn.setOnClickListener(this);
        vh.custompb.setOnClickListener(this);
        if (mListener.getFlag1()) {//显示删除多选框
            vh.deleteCheckBox.setVisibility(View.VISIBLE);
            vh.deleteCheckBox.setChecked(mListener.getStatus(position));
            if (true == mListener.getFlag2()) {
                mListener.setFlag2(false);
            }
            vh.actionBtn.setVisibility(View.GONE);
            vh.custompb.setVisibility(View.GONE);
        } else {//正常状态(没有删除的多选框)
            vh.deleteCheckBox.setVisibility(View.GONE);
            if (mListener.getTitleId() == R.string.dm_downloading) {
                vh.actionBtn.setVisibility(View.GONE);
                vh.custompb.setVisibility(View.VISIBLE);
                vh.statusView.setVisibility(View.VISIBLE);
            } else if (mListener.getTitleId() == R.string.dm_downloaded) {
                vh.actionBtn.setVisibility(View.VISIBLE);
                vh.custompb.setVisibility(View.GONE);
                vh.statusView.setVisibility(View.INVISIBLE);
            }
            vh.deleteCheckBox.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        ViewHolder vh = (ViewHolder) v.getTag();
        ImplHelper.onClick(mContext,
                vh.implInfo,
                vh.implInfo.getDownloadUrl(),
                vh.implInfo.getTitle(),
                vh.implInfo.getIconUrl(),
                vh.implInfo.getFileSavePath(),
                null,
                vh);
    }


    public class ViewHolder implements ImplChangeCallback {
        CustomProgressBar custompb;
        TextView titleView;
        TextView descView;
        TextView statusView;
        TextView actionBtn;
        CheckBox deleteCheckBox;
        ImageView iconView;
        public ImplInfo implInfo;


        ViewHolder(View view) {
            actionBtn = (TextView) view.findViewById(R.id.button_op);
            custompb = (CustomProgressBar) view.findViewById(R.id.cpb);
            deleteCheckBox = (CheckBox) view.findViewById(R.id.delete_checkBox);
            descView = (TextView) view.findViewById(R.id.size_text);
            titleView = (TextView) view.findViewById(R.id.download_title);
            statusView = (TextView) view.findViewById(R.id.domain);
            iconView = (ImageView) view.findViewById(R.id.download_icon);
            actionBtn.setTag(this);
            deleteCheckBox.setTag(this);
            custompb.setTag(this);
        }

        void initView(ImplInfo info) {
            this.implInfo = info;
            if (null == this.implInfo) {
                return;
            }
            implAgent.bindImplCallback(this, implInfo);
            String title = implInfo.getTitle();
            if (null == title || title.isEmpty()) {
                title = mContext.getResources().getString(R.string.missing_title);
            }
            titleView.setText(title);
            setIcon();
            refresh();
        }

        void refresh() {
            if (null == this.implInfo) {
                return;
            }
            ImplInfo.ImplRes implRes = implInfo.getImplRes();
            actionBtn.setText(implRes.getActionText());
            actionBtn.setEnabled(true);
            switch (implInfo.getStatus()) {
                case ImplInfo.STATUS_PENDING://下载等待
                    custompb.setImageResource(R.drawable.download_status_pause);
                    break;
                case ImplInfo.STATUS_RUNNING://下载中
                    custompb.setImageResource(R.drawable.download_status_running);
                    descView.setText("0.00MB/???MB");
                    break;
                case ImplInfo.STATUS_PAUSED://下载暂停
                    custompb.setImageResource(R.drawable.download_status_pause);
                    break;
                case ImplInfo.STATUS_PACKAGE_INVALID:
                case ImplInfo.STATUS_INSTALL_FAILED: //安装失败
                case ImplInfo.STATUS_FAILED://下载失败
                    custompb.setImageResource(R.drawable.download_status_retry);
                    break;
                default:
                    break;
            }
            descView.setText(implRes.getDescText());
            descView.invalidate();
            statusView.setText(implRes.getStatusText());
            statusView.invalidate();
            custompb.setProgress(implInfo.getProgress());
            ImplLog.d("impl_dm", implInfo.getTitle() + "," + implRes.getStatusText() + "," + implRes.getDescText() + "," + implInfo.getProgress());
        }

        private void setIcon() {
            Bitmap resBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.file_type_apk);
            String url = implInfo.getIconUrl();
            if (null != url && !TextUtils.isEmpty(url)&& AppliteUtils.isLoadNetworkBitmap(mContext)) {
                int width = (int) mContext.getResources().getDimension(R.dimen.list_item_icon_size);
                int height = width;
                mBitmapHelper.configDefaultBitmapMaxSize(width, height);
                Bitmap cacheBitmap = mBitmapHelper.getBitmapFromMemCache(url, null);
                if (null != cacheBitmap) {
                    iconView.setImageBitmap(cacheBitmap);
                } else {
                    mBitmapHelper.configDefaultLoadFailedImage(resBitmap);
                    mBitmapHelper.configDefaultLoadingImage(resBitmap);
                    mBitmapHelper.display(iconView, implInfo.getIconUrl());
                }
            } else {
                iconView.setImageBitmap(resBitmap);
            }
        }

        @Override
        public void onChange(ImplInfo info) {
            refresh();
            ImplInfo.ImplRes implRes = implInfo.getImplRes();
            ImplLog.d("impl_dm", "onChange," + implInfo.getTitle() + "," + implRes.getStatusText() + "," + implRes.getDescText() + "," + (info == implInfo));
        }
    }
}