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
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.applite.common.AppliteUtils;
import com.applite.common.BitmapHelper;
import com.applite.common.Constant;
import com.applite.common.IconCache;
import com.applite.common.LogUtils;
import com.applite.dm.utils.HostUtils;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplChangeCallback;
import com.osgi.extra.OSGIServiceHost;

import org.apkplug.Bundle.OSGIServiceAgent;
import org.osgi.framework.BundleContext;
import java.lang.reflect.Method;

public class DownloadAdapter extends CursorAdapter implements View.OnClickListener{
    private Context mContext;
    private Resources mResources;
    private LayoutInflater mInflater;
//    private int mCheckedItemPosition = -1;
    private int mStatusFlag;
    private BitmapUtils mBitmapHelper;
    private ImplAgent implAgent;

    public DownloadAdapter(Context context, Cursor cursor, int statusFlag) {
        super(context, cursor,true);
        mContext = context;
        mStatusFlag = statusFlag;
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
            viewHolder = new ViewHolder(view);
        }

        viewHolder.position = cursor.getPosition();
        viewHolder.actionBtn.setOnClickListener(this);
//        viewHolder.deleteButton.setOnClickListener(this);
//        viewHolder.detailButton.setOnClickListener(this);

        view.setOnClickListener(this);
        String key = cursor.getString(cursor.getColumnIndex("key"));
        String packageName = cursor.getString(cursor.getColumnIndex("packageName"));
        int versionCode = cursor.getInt(cursor.getColumnIndex("versionCode"));
        viewHolder.initView(implAgent.getImplInfo(key,packageName,versionCode));
//        if (mCheckedItemPosition == cursor.getPosition()){
//            viewHolder.extra.setVisibility(View.VISIBLE);
//        }else{
//            viewHolder.extra.setVisibility(View.GONE);
//        }
    }

    @Override
    public void onClick(View v) {
        ViewHolder vh = (ViewHolder)v.getTag();
        switch(v.getId()){
//            case R.id.button_delete:
//                implAgent.remove(vh.implInfo);
//                break;
//            case R.id.button_detail:
//                BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
//                OSGIServiceHost host = (OSGIServiceHost)mContext;
//                if (null != host){
//                    Bundle bundle = new Bundle();
//                    bundle.putString("packageName",vh.implInfo.getPackageName());
//                    bundle.putString("name",vh.implInfo.getTitle());
//                    bundle.putString("iconUrl",vh.implInfo.getIconUrl());
//                    AppliteUtils.putFgParams(bundle,Constant.OSGI_SERVICE_DM_FRAGMENT,"replace",true);
//                    host.jumpto(bundleContext,Constant.OSGI_SERVICE_DETAIL_FRAGMENT,null,bundle);
//                }
//                break;
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
//                if (mCheckedItemPosition == vh.position){
//                    mCheckedItemPosition = -1;
//                }else{
//                    mCheckedItemPosition = vh.position;
//                }
//                notifyDataSetInvalidated();
                HostUtils.launchDetail((OSGIServiceHost)mContext,
                        vh.implInfo.getPackageName(),
                        vh.implInfo.getTitle(),
                        vh.implInfo.getIconUrl());
                break;
        }
    }

    class ViewHolder {
        ProgressBar progressBar;
        TextView titleView;
        TextView descView;
        TextView statusView;
        TextView actionBtn;
//        TextView deleteButton;
//        TextView detailButton;
        ImageView iconView;
        ImplInfo implInfo;
//        View extra;
        ImplChangeCallback implCallback;
        int position;

        ViewHolder(View view) {
            actionBtn = (TextView) view.findViewById(R.id.button_op);
//            deleteButton = (TextView) view.findViewById(R.id.button_delete);
//            detailButton = (TextView) view.findViewById(R.id.button_detail);
            progressBar = (ProgressBar) view.findViewById(android.R.id.progress);
            descView = (TextView) view.findViewById(R.id.size_text);
            titleView = (TextView) view.findViewById(R.id.download_title);
            statusView = (TextView) view.findViewById(R.id.domain);
            iconView = (ImageView) view.findViewById(R.id.download_icon);
//            extra = view.findViewById(R.id.extra_line);
            view.setTag(this);
            actionBtn.setTag(this);
//            deleteButton.setTag(this);
//            detailButton.setTag(this);
            implCallback = new DownloadImplCallback(this);
        }

        void initView(ImplInfo info){
            this.implInfo = info;
            if (null == this.implInfo){
                return;
            }
            implAgent.setImplCallback(implCallback,implInfo);
            actionBtn.setText(implAgent.getActionText(implInfo));
            descView.setText(implAgent.getDescText(implInfo));
            String title = implInfo.getTitle();
            if(null == title || title.isEmpty()) {
                title = mResources.getString(R.string.missing_title);
            }
            titleView.setText(title);
            statusView.setText(implAgent.getStatusText(implInfo));
            setIcon();
            setProgress();
        }

        void refresh(){
            if (null == this.implInfo){
                return;
            }
            actionBtn.setText(implAgent.getActionText(implInfo));
            descView.setText(implAgent.getDescText(implInfo));
            statusView.setText(implAgent.getStatusText(implInfo));
            setProgress();
        }

        private void setIcon() {
            Drawable drawable = mResources.getDrawable(R.drawable.ic_download_misc_file_type);
            iconView.setImageDrawable(drawable);
            if(null != implInfo.getIconUrl()){
                mBitmapHelper.configDefaultLoadFailedImage(drawable);
                mBitmapHelper.configDefaultLoadingImage(drawable);
                mBitmapHelper.display(iconView, implInfo.getIconUrl(),new BitmapLoadCallBack<ImageView>() {
                    @Override
                    public void onLoadCompleted(ImageView imageView, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                        this.setBitmap(imageView,IconCache.getInstance(mContext).getIcon(implInfo.getPackageName(),bitmap));
                        Animation animation = bitmapDisplayConfig.getAnimation();
                        if (animation != null) {
                            animationDisplay(imageView, animation);
                        }
                    }

                    @Override
                    public void onLoadFailed(ImageView imageView, String s, Drawable drawable) {
                        this.setDrawable(imageView, drawable);
                    }

                    private void animationDisplay(ImageView container, Animation animation) {
                        try {
                            Method cloneMethod = Animation.class.getDeclaredMethod("clone");
                            cloneMethod.setAccessible(true);
                            container.startAnimation((Animation) cloneMethod.invoke(animation));
                        } catch (Throwable e) {
                            container.startAnimation(animation);
                        }
                    }
                });
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
            ViewHolder vh = (ViewHolder)tag;
            vh.refresh();
            if ((info.getStatus()&mStatusFlag) == 0){
                notifyDataSetChanged();
            }
        }
    }
}
