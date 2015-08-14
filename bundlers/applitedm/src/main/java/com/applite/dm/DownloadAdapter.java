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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.applite.common.BitmapHelper;
import com.applite.common.Constant;
import com.applite.common.IconCache;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplChangeCallback;
import com.mit.impl.ImplLog;

import java.util.Comparator;
import java.util.List;

public class DownloadAdapter extends ArrayAdapter implements View.OnClickListener{
    private Context mContext;
    private int mLayoutId;
    private BitmapUtils mBitmapHelper;
    private ImplAgent implAgent;

    public DownloadAdapter(Context context, int resource, List<ImplInfo> implInfoList,BitmapUtils bitmapHelper) {
        super(context, resource, implInfoList);
        mContext = context;
        mBitmapHelper = bitmapHelper;
        implAgent = ImplAgent.getInstance(context.getApplicationContext());
        this.mLayoutId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            view = LayoutInflater.from(mContext).inflate(mLayoutId,null);
            view.setTag(new ViewHolder(view));
        }
        ViewHolder vh = (ViewHolder)view.getTag();
        vh.initView((ImplInfo)getItem(position));
        vh.actionBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        ViewHolder vh = (ViewHolder)v.getTag();
        if (R.id.button_op == v.getId()) {
            if (ImplInfo.ACTION_DOWNLOAD == implAgent.getAction(vh.implInfo)) {
                switch (vh.implInfo.getStatus()) {
                    case Constant.STATUS_PENDING:
                        break;
                    case Constant.STATUS_RUNNING:
                        implAgent.pauseDownload(vh.implInfo);
                        break;
                    case Constant.STATUS_PAUSED:
                        implAgent.resumeDownload(vh.implInfo, vh.implCallback);
                        break;
                    default:
                        implAgent.newDownload(vh.implInfo,
                                Constant.extenStorageDirPath,
                                vh.implInfo.getTitle() + ".apk",
                                true,
                                vh.implCallback);
                        break;
                }
            } else {
                try {
                    mContext.startActivity(implAgent.getActionIntent(vh.implInfo));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ViewHolder {
        ProgressBar progressBar;
        TextView titleView;
        TextView descView;
        TextView statusView;
        TextView actionBtn;
        ImageView iconView;
        ImplInfo implInfo;
        ImplChangeCallback implCallback;

        ViewHolder(View view) {
            actionBtn = (TextView) view.findViewById(R.id.button_op);
            progressBar = (ProgressBar) view.findViewById(android.R.id.progress);
            descView = (TextView) view.findViewById(R.id.size_text);
            titleView = (TextView) view.findViewById(R.id.download_title);
            statusView = (TextView) view.findViewById(R.id.domain);
            iconView = (ImageView) view.findViewById(R.id.download_icon);
            actionBtn.setTag(this);
            progressBar.setTag(this);
            implCallback = new DownloadImplCallback(this);
        }

        void initView(ImplInfo info){
            this.implInfo = info;
            if (null == this.implInfo){
                return;
            }
            implAgent.setImplCallback(implCallback,implInfo);
            String title = implInfo.getTitle();
            if(null == title || title.isEmpty()) {
                title = mContext.getResources().getString(R.string.missing_title);
            }
            titleView.setText(title);
            setIcon();
            refresh();
        }

        void refresh(){
            if (null == this.implInfo){
                return;
            }
            actionBtn.setEnabled(true);
            switch (implInfo.getStatus()){
                case Constant.STATUS_PRIVATE_INSTALLING:
                    actionBtn.setText(implAgent.getStatusText(implInfo));
                    actionBtn.setEnabled(false);
                    break;
                default:
                    actionBtn.setText(implAgent.getActionText(implInfo));
                    break;
            }
            descView.setText(implAgent.getDescText(implInfo));
            statusView.setText(implAgent.getStatusText(implInfo));
            setProgress();
        }

        private void setIcon() {
            Bitmap resBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.file_type_apk);
            String url = implInfo.getIconUrl();
            if(null != url && !TextUtils.isEmpty(url)){
                int width = (int)mContext.getResources().getDimension(R.dimen.list_item_icon_size);
                int height = width;
                mBitmapHelper.configDefaultBitmapMaxSize(width,height);
                Bitmap cacheBitmap = mBitmapHelper.getBitmapFromMemCache(url,null);
                if (null != cacheBitmap){
                    iconView.setImageBitmap(cacheBitmap);
                }else {
                    mBitmapHelper.configDefaultLoadFailedImage(resBitmap);
                    mBitmapHelper.configDefaultLoadingImage(resBitmap);
                    mBitmapHelper.display(iconView, implInfo.getIconUrl());
                }
            }else{
                iconView.setImageBitmap(resBitmap);
            }
        }

        private void setProgress(){
            progressBar.setIndeterminate(false);
            progressBar.setMax(100);
            progressBar.setProgress(implAgent.getProgress(implInfo));
            progressBar.setVisibility(View.VISIBLE);
        }
    }


    class DownloadImplCallback implements ImplChangeCallback {
        Object tag ;

        DownloadImplCallback(Object tag) {
            this.tag = tag;
        }

        @Override
        public void onChange(ImplInfo info) {
            ImplLog.d(this.getClass().getSimpleName(),"onChange,"+info.getTitle()+","+info.getStatus());
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }
    }
}
