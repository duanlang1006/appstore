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

import com.applite.common.Constant;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import net.tsz.afinal.FinalBitmap;

import org.apkplug.Bundle.ApkplugOSGIService;
import org.apkplug.Bundle.OSGIServiceAgent;
import org.osgi.framework.BundleContext;

import java.util.List;

/**
 * List adapter for Cursors returned by {@link com.android.dsc.downloads.DownloadManager}.
 */
public class DownloadAdapter extends CursorAdapter implements View.OnClickListener{
    private Context mContext;
    private Resources mResources;
    private LayoutInflater mInflater;
    private int mCheckedItemPosition = -1;
    private FinalBitmap mFinalBitmap;

    public DownloadAdapter(Context context, Cursor cursor) {
        super(context, cursor,true);
        mContext = context;
        mFinalBitmap = FinalBitmap.create(mContext);
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
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.download_list_item, null);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        DownloadItemViewHolder viewHolder = (DownloadItemViewHolder)view.getTag();
        if (null == viewHolder) {
            viewHolder = new DownloadItemViewHolder();
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
        viewHolder.setImplInfo(ImplInfo.from(cursor));
        if (mCheckedItemPosition == cursor.getPosition()){
            viewHolder.extra.setVisibility(View.VISIBLE);
        }else{
            viewHolder.extra.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        DownloadAdapter.DownloadItemViewHolder viewHoler = (DownloadAdapter.DownloadItemViewHolder)v.getTag();
        switch(v.getId()){
            case R.id.button_delete:
                ImplAgent.requestDownloadDelete(mContext,viewHoler.implInfo.getKey());
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
                            viewHoler.implInfo.getPackageName(),
                            viewHoler.implInfo.getTitle(),
                            viewHoler.implInfo.getIconUrl(),
                            Constant.OSGI_SERVICE_DM_FRAGMENT);
                } catch (Exception e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                }
                break;
            case R.id.button_op:
                switch(viewHoler.implInfo.getAction(mContext)){
                    case ImplInfo.ACTION_DOWNLOAD:
                        if (null != viewHoler.implInfo){
                            ImplAgent.downloadToggle(mContext, viewHoler.implInfo.getKey());
                        }
                        break;
                    default:
                        try {
                            mContext.startActivity(viewHoler.implInfo.getActionIntent(mContext));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
                break;
            default:
                if (mCheckedItemPosition == viewHoler.position){
                    mCheckedItemPosition = -1;
                }else{
                    mCheckedItemPosition = viewHoler.position;
                }
                notifyDataSetInvalidated();
                break;
        }
    }

    private void setIcon(ImplInfo info,ImageView iconView) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromParts("file", "", null), info.getMimeType());
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
        if(null != info.getIconUrl()){
            mFinalBitmap.display(iconView, info.getIconUrl());
        }
    }

    private void setProgress(ProgressBar progressBar,int percent){
        progressBar.setIndeterminate(false);
        progressBar.setMax(100);
        progressBar.setProgress(percent);
        progressBar.setVisibility(View.VISIBLE);
    }

    class DownloadItemViewHolder{
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

        void setImplInfo(ImplInfo info){
            this.implInfo = info;
            actionBtn.setText(implInfo.getActionText(mContext));
//            if (implInfo.getAction(mContext) == ImplInfo.ACTION_INSTALL){
//                actionBtn.setEnabled(false);
//            }else{
//                actionBtn.setEnabled(true);
//            }
            descView.setText(implInfo.getDescText(mContext));
            String title = implInfo.getTitle();
            if(null == title && title.isEmpty()) {
                title = mResources.getString(R.string.missing_title);
            }
            titleView.setText(title);
            statusView.setText(implInfo.getStatusText(mContext));
            setIcon(info,iconView);
            setProgress(progressBar,implInfo.getProgress());
        }
    }
}
