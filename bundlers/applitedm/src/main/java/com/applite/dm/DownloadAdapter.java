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

package com.applite.dm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.lidroid.xutils.BitmapUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplChangeCallback;

import java.io.File;
import java.util.List;

public class DownloadAdapter extends ArrayAdapter implements View.OnClickListener {
    private Context mContext;
    private int mLayoutId;

    private BitmapUtils mBitmapHelper;
    private ImplAgent implAgent;

    private DownloadListener mListener;
    private ImplInfo implInfo;
    private int isDownloaded = ImplInfo.STATUS_PENDING | ImplInfo.STATUS_RUNNING | ImplInfo.STATUS_PAUSED | ImplInfo.STATUS_FAILED;

    public DownloadAdapter(Context context,
                           int resource,
                           List<ImplInfo> implInfoList,
                           BitmapUtils bitmapHelper,
                           DownloadListener listener) {
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
                vh.deleteCheckBox.startAnimation(vh.animaCheckBox);
                mListener.setFlag2(false);
            }
            vh.actionBtn.setVisibility(View.GONE);
            vh.custompb.setVisibility(View.GONE);
            vh.refresh();
        } else {//正常状态(没有删除的多选框)
            vh.deleteCheckBox.setVisibility(View.GONE);
            if (mListener.getStatusFlags() == isDownloaded) {
                vh.actionBtn.setVisibility(View.GONE);
                vh.custompb.setVisibility(View.VISIBLE);
            } else if (mListener.getStatusFlags() == ~isDownloaded) {
                vh.actionBtn.setVisibility(View.VISIBLE);
                vh.custompb.setVisibility(View.GONE);
            }
            vh.deleteCheckBox.setVisibility(View.GONE);
        }
        if (mListener.getStatusFlags() == ~isDownloaded) {
            vh.statusView.setVisibility(View.INVISIBLE);
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
                Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + vh.implInfo.getTitle() + ".apk",
                null,
                vh);
    }


    class ViewHolder implements ImplChangeCallback {
        CustomProgressBar custompb;
        TextView titleView;
        TextView descView;
        TextView statusView;
        TextView actionBtn;
        CheckBox deleteCheckBox;
        ImageView iconView;
        ImplInfo implInfo;
        Animation animaCheckBox;


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
            implAgent.setImplCallback(this, implInfo);
            String title = implInfo.getTitle();
            if (null == title || title.isEmpty()) {
                title = mContext.getResources().getString(R.string.missing_title);
            }
            titleView.setText(title);
            setIcon();
            refresh();
            animaCheckBox = AnimationUtils.loadAnimation(mContext, R.anim.checkbox_in);
        }

        void refresh() {
            if (null == this.implInfo) {
                return;
            }
            ImplHelper.ImplHelperRes res = ImplHelper.getImplRes(mContext, implInfo);
            actionBtn.setText(res.getActionText());
            actionBtn.setEnabled(true);
            switch (implInfo.getStatus()) {
//                case ImplInfo.STATUS_PRIVATE_INSTALLING://静默安装
////                    actionBtn.setEnabled(false);
//                    break;
//                case ImplInfo.ACTION_DOWNLOAD://下载
//                    custompb.setImageResource(R.drawable.download_status_pause);
//                    break;
//                case ImplInfo.ACTION_INSTALL://安装过程   ------->这里有时下载也会显示安装过程!
//                    custompb.setImageResource(R.drawable.download_status_pause);
//                    break;
                case ImplInfo.STATUS_RUNNING://下载中
                    custompb.setImageResource(R.drawable.download_status_running);
                    break;
                case ImplInfo.STATUS_PAUSED://下载暂停
                    custompb.setImageResource(R.drawable.download_status_pause);
                    break;
                case ImplInfo.STATUS_FAILED://下载失败
                    custompb.setImageResource(R.drawable.download_status_retry);
                    break;
                case ImplInfo.STATUS_INSTALL_FAILED: //安装失败
                    custompb.setImageResource(R.drawable.download_status_retry);
                    break;
                default:
//                    Toast.makeText(mContext, "其他", Toast.LENGTH_SHORT).show();
                    break;
            }
            descView.setText(res.getDescText());
            statusView.setText(res.getStatusText());
            setProgress(res);
        }

        private void setIcon() {
            Bitmap resBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.file_type_apk);
            String url = implInfo.getIconUrl();
            if (null != url && !TextUtils.isEmpty(url)) {
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


        private void setProgress(ImplHelper.ImplHelperRes res) {
            custompb.setVisibility(View.VISIBLE);
            custompb.setProgress(res.getProgress());
        }

        @Override
        public void onChange(ImplInfo info) {
            refresh();
        }
    }
}