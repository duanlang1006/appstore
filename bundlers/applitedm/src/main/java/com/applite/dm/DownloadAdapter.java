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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.applite.common.BitmapHelper;
import com.applite.common.Constant;
import com.lidroid.xutils.BitmapUtils;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplListener;
import org.apkplug.Bundle.ApkplugOSGIService;
import org.apkplug.Bundle.OSGIServiceAgent;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.util.List;

/**
 * List adapter for Cursors returned by {@link com.android.dsc.downloads.DownloadManager}.
 */
public class DownloadAdapter extends CursorAdapter implements View.OnClickListener{
    private Context mContext;
    private Resources mResources;
    private LayoutInflater mInflater;
    private int mCheckedItemPosition = -1;
    private BitmapUtils mBitmapHelper;
    private ImplAgent implAgent;

    public DownloadAdapter(Context context, Cursor cursor) {
        super(context, cursor,true);
        mContext = context;
        mBitmapHelper = BitmapHelper.getBitmapUtils(mContext.getApplicationContext());
        mResources = mContext.getResources();
        mInflater = LayoutInflater.from(mContext);
        try {
            Context cxt = BundleContextFactory.getInstance().getBundleContext().getBundleContext();
            if (null != cxt) {
                mResources = cxt.getResources();
                mInflater = LayoutInflater.from(cxt);
                mInflater = mInflater.cloneInContext(cxt);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        implAgent = ImplAgent.getInstance(context.getApplicationContext());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.download_list_item, null);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();
        if (null == viewHolder) {
            viewHolder = new ViewHolder();
            viewHolder.actionBtn = (TextView) view.findViewById(R.id.button_op);
            viewHolder.deleteButton = (TextView) view.findViewById(R.id.button_delete);
            viewHolder.detailButton = (TextView) view.findViewById(R.id.button_detail);
            viewHolder.progressBar = (ProgressBar) view.findViewById(android.R.id.progress);
            viewHolder.descView = (TextView) view.findViewById(R.id.size_text);
            viewHolder.titleView = (TextView) view.findViewById(R.id.download_title);
            viewHolder.statusView = (TextView) view.findViewById(R.id.domain);
            viewHolder.iconView = (ImageView) view.findViewById(R.id.download_icon);
            viewHolder.extra = view.findViewById(R.id.extra_line);
            view.setTag(viewHolder);
            viewHolder.actionBtn.setTag(viewHolder);
            viewHolder.deleteButton.setTag(viewHolder);
            viewHolder.detailButton.setTag(viewHolder);
        }

        viewHolder.position = cursor.getPosition();
        viewHolder.actionBtn.setOnClickListener(this);
        viewHolder.deleteButton.setOnClickListener(this);
        viewHolder.detailButton.setOnClickListener(this);

        view.setOnClickListener(this);
        String key = cursor.getString(cursor.getColumnIndex("key"));
        String packageName = cursor.getString(cursor.getColumnIndex("packageName"));
        int versionCode = cursor.getInt(cursor.getColumnIndex("versionCode"));
        viewHolder.initView(implAgent.getImplInfo(key,packageName,versionCode));
        if (mCheckedItemPosition == cursor.getPosition()){
            viewHolder.extra.setVisibility(View.VISIBLE);
        }else{
            viewHolder.extra.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        ViewHolder vh = (ViewHolder)v.getTag();
        switch(v.getId()){
            case R.id.button_delete:
                implAgent.remove(vh.implInfo);
                break;
            case R.id.button_detail:
                try {
                    BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
                    OSGIServiceAgent<ApkplugOSGIService> agent = new OSGIServiceAgent<ApkplugOSGIService>(
                            bundleContext, ApkplugOSGIService.class,
                            "(serviceName="+ Constant.OSGI_SERVICE_HOST_OPT+")", //服务查询条件
                            OSGIServiceAgent.real_time);   //每次都重新查询
                    agent.getService().ApkplugOSGIService(bundleContext,
                            Constant.OSGI_SERVICE_DM_FRAGMENT,
                            0, Constant.OSGI_SERVICE_DETAIL_FRAGMENT,
                            vh.implInfo.getPackageName(),
                            vh.implInfo.getTitle(),
                            vh.implInfo.getIconUrl(),
                            Constant.OSGI_SERVICE_DM_FRAGMENT);
                } catch (Exception e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                }
                break;
            case R.id.button_op:
                if (ImplInfo.ACTION_DOWNLOAD == implAgent.getAction(vh.implInfo)) {
                    switch (vh.implInfo.getStatus()) {
                        case Constant.STATUS_PENDING:
                        case Constant.STATUS_RUNNING:
                            implAgent.pauseDownload(vh.implInfo);
                            break;
                        case Constant.STATUS_PAUSED:
                            implAgent.resumeDownload(vh.implInfo, new DownloadImplCallback(vh));
                            break;
                        default:
                            break;
                    }
                } else {
                    try {
                        mContext.startActivity(implAgent.getActionIntent(vh.implInfo));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                if (mCheckedItemPosition == vh.position){
                    mCheckedItemPosition = -1;
                }else{
                    mCheckedItemPosition = vh.position;
                }
                notifyDataSetInvalidated();
                break;
        }
    }

    class ViewHolder {
        ProgressBar progressBar;
        TextView titleView;
        TextView descView;
        TextView statusView;
        TextView actionBtn;
        TextView deleteButton;
        TextView detailButton;
        ImageView iconView;
        ImplInfo implInfo;
        View extra;
        int position;

        void initView(ImplInfo info){
            this.implInfo = info;
            actionBtn.setText(implAgent.getActionText(implInfo));
//            if (implAgent.getAction(mContext) == ImplInfo.ACTION_INSTALL){
//                actionBtn.setEnabled(false);
//            }else{
//                actionBtn.setEnabled(true);
//            }
            descView.setText(implAgent.getDescText(implInfo));
            String title = implInfo.getTitle();
            if(null == title && title.isEmpty()) {
                title = mResources.getString(R.string.missing_title);
            }
            titleView.setText(title);
            statusView.setText(implAgent.getStatusText(implInfo));
            setIcon();
            setProgress();
        }

        void refresh(){
            actionBtn.setText(implAgent.getActionText(implInfo));
            descView.setText(implAgent.getDescText(implInfo));
            statusView.setText(implAgent.getStatusText(implInfo));
            setProgress();
        }

        private void setIcon() {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromParts("file", "", null), implInfo.getMimeType());
            PackageManager pm = mContext.getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            if (list.size() == 0) {
                // no icon found for this mediatype. use "unknown" icon
                iconView.setImageDrawable(mResources.getDrawable(R.drawable.ic_download_misc_file_type));
            } else {
                Drawable icon = list.get(0).activityInfo.loadIcon(pm);
                iconView.setImageDrawable(icon);
            }
            if(null != implInfo.getIconUrl()){
                mBitmapHelper.display(iconView, implInfo.getIconUrl());
            }
        }

        private void setProgress(){
            progressBar.setIndeterminate(false);
            progressBar.setMax(100);
            progressBar.setProgress(implAgent.getProgress(implInfo));
            progressBar.setVisibility(View.VISIBLE);
        }
    }


    class DownloadImplCallback extends ImplListener {
        Object tag ;

        DownloadImplCallback(Object tag) {
            this.tag = tag;
        }

        @Override
        public void onStart(ImplInfo info) {
            super.onStart(info);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onCancelled(ImplInfo info) {
            super.onCancelled(info);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onLoading(ImplInfo info, long total, long current, boolean isUploading) {
            super.onLoading(info, total, current, isUploading);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onSuccess(ImplInfo info, File file) {
            super.onSuccess(info, file);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onFailure(ImplInfo info, Throwable t, String msg) {
            super.onFailure(info, t, msg);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onInstallSuccess(ImplInfo info) {
            super.onInstallSuccess(info);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onInstalling(ImplInfo info) {
            super.onInstalling(info);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onInstallFailure(ImplInfo info, int errorCode) {
            super.onInstallFailure(info, errorCode);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onUninstallSuccess(ImplInfo info) {
            super.onUninstallSuccess(info);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onUninstalling(ImplInfo info) {
            super.onUninstalling(info);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }

        @Override
        public void onUninstallFailure(ImplInfo info, int errorCode) {
            super.onUninstallFailure(info, errorCode);
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
        }
    }
}
