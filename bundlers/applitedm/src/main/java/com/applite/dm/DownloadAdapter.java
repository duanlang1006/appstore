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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.applite.common.Constant;
import com.lidroid.xutils.BitmapUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplHelper;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplLog;

import java.io.File;
import java.util.Comparator;
import java.util.List;

public class DownloadAdapter extends ArrayAdapter implements View.OnClickListener {
    private Context mContext;
    private int mLayoutId;

    private BitmapUtils mBitmapHelper;
    private ImplAgent implAgent;
    private Animation animaCheckBox;
    private boolean oldFlag = false;
    private DownloadListener mListener;

    public DownloadAdapter(Context context,
                           int resource,
                           List<ImplInfo> implInfoList,
                           BitmapUtils bitmapHelper,
                           DownloadListener listener
                           ) {
        super(context, resource, implInfoList);
        mContext = context;
        mBitmapHelper = bitmapHelper;
        mListener = listener;
        implAgent = ImplAgent.getInstance(context.getApplicationContext());
        this.mLayoutId = resource;
        animaCheckBox = AnimationUtils.loadAnimation(context, R.anim.checkbox_in);
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
        if (mListener.getFlag()) {
            vh.deleteCheckBox.setVisibility(View.VISIBLE);
            vh.deleteCheckBox.setChecked(mListener.getStatus(position));
            if (false == oldFlag && true == mListener.getFlag()) {
                vh.deleteCheckBox.startAnimation(animaCheckBox);
                if (position == getCount() - 1) {
                    oldFlag = true;
                }
            }
            vh.actionBtn.setVisibility(View.GONE);
        } else {
            vh.actionBtn.setVisibility(View.VISIBLE);
            vh.deleteCheckBox.setVisibility(View.GONE);
        }
        return view;
    }


    @Override
    public void onClick(View v) {
        ViewHolder vh = (ViewHolder) v.getTag();
        if (R.id.button_op == v.getId()) {
            ImplHelper.onClick(mContext,
                    vh.implInfo,
                    vh.implInfo.getDownloadUrl(),
                    vh.implInfo.getTitle(),
                    vh.implInfo.getIconUrl(),
                    Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + vh.implInfo.getTitle() + ".apk",
                    null,
                    vh);

//            if (ImplInfo.ACTION_DOWNLOAD == implAgent.getAction(vh.implInfo)) {
//                switch (vh.implInfo.getStatus()) {
//                    case ImplInfo.STATUS_PENDING:
//                        break;
//                    case ImplInfo.STATUS_RUNNING:
//                        implAgent.pauseDownload(vh.implInfo);
//                        break;
//                    case ImplInfo.STATUS_PAUSED:
//                        implAgent.resumeDownload(vh.implInfo, vh);
//                        break;
//                    default:
//                        implAgent.newDownload(vh.implInfo,
//                                vh.implInfo.getDownloadUrl(),
//                                vh.implInfo.getTitle(),
//                                vh.implInfo.getIconUrl(),
//                                Constant.extenStorageDirPath,
//                                vh.implInfo.getTitle() + ".apk",
//                                true,
//                                vh);
//                        break;
//                }
//            } else {
//                implAgent.startActivity(vh.implInfo);
//            }
        }
    }

    class ViewHolder implements ImplChangeCallback{
        ProgressBar progressBar;
        TextView titleView;
        TextView descView;
        TextView statusView;
        TextView actionBtn;
        ImageButton customImBtn;
        CheckBox deleteCheckBox;
        ImageView iconView;
        ImplInfo implInfo;

        ViewHolder(View view) {
            actionBtn = (TextView) view.findViewById(R.id.button_op);
//            customImBtn = (ImageButton) view.findViewById(R.id.cpb);
            deleteCheckBox = (CheckBox) view.findViewById(R.id.delete_checkBox);
            progressBar = (ProgressBar) view.findViewById(android.R.id.progress);
            descView = (TextView) view.findViewById(R.id.size_text);
            titleView = (TextView) view.findViewById(R.id.download_title);
            statusView = (TextView) view.findViewById(R.id.domain);
            iconView = (ImageView) view.findViewById(R.id.download_icon);
            actionBtn.setTag(this);
            deleteCheckBox.setTag(this);
            progressBar.setTag(this);
        }

        void initView(ImplInfo info) {
            this.implInfo = info;
            if (null == this.implInfo) {
                return;
            }
//            actionBtn.setText(implAgent.getActionText(implInfo));//??
//            descView.setText(implAgent.getDescText(implInfo));//??
            implAgent.setImplCallback(this,implInfo);
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
            actionBtn.setEnabled(true);
            switch (implInfo.getStatus()){
                case ImplInfo.STATUS_PRIVATE_INSTALLING:
                    actionBtn.setText(ImplHelper.getStatusText(mContext,implInfo));
                    actionBtn.setEnabled(false);
                    break;
                default:
                    actionBtn.setText(ImplHelper.getActionText(mContext, implInfo));
                    break;
            }
            descView.setText(ImplHelper.getDescText(mContext,implInfo));
            statusView.setText(ImplHelper.getStatusText(mContext,implInfo));
            setProgress();
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
            }else{
                iconView.setImageBitmap(resBitmap);
            }
        }

        private void setProgress() {
            progressBar.setIndeterminate(false);
            progressBar.setMax(100);
            progressBar.setProgress(ImplHelper.getProgress(mContext,implInfo));
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onChange(ImplInfo info) {
            refresh();
        }
    }
}
