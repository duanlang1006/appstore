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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.dsc.downloads.DownloadManager;
import com.applite.common.Constant;
import com.applite.dm.DownloadItem.DownloadSelectListener;
import com.mit.impl.ImplConfig;
import com.mit.impl.ImplLog;

import net.tsz.afinal.FinalBitmap;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * List adapter for Cursors returned by {@link com.android.dsc.downloads.DownloadManager}.
 */
public class DownloadAdapter extends CursorAdapter {
    private Context mContext;
    private DownloadSelectListener mDownloadSelectionListener;
    private Resources mResources;
    private LayoutInflater mInflater;
    private DateFormat mDateFormat;
    private DateFormat mTimeFormat;

    private int mKeyColumnId;
    private int mTitleColumnId;
    private int mStatusColumnId;
    private int mReasonColumnId;
    private int mTotalBytesColumnId;
    private int mCurrentBytesColumnId;
    private int mMediaTypeColumnId;
    private int mLocalUriColumnId;
    private int mDateColumnId;
    private int mIdColumnId;

    private FinalBitmap mFinalBitmap;
    private PackageManager mPackageManager;

    public DownloadAdapter(Context context, Cursor cursor,
                           DownloadSelectListener selectionListener) {
        super(context, cursor,true);
        mContext = context;
        mFinalBitmap = FinalBitmap.create(mContext);
        mPackageManager = mContext.getPackageManager();
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

        mDownloadSelectionListener = selectionListener;
        mDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        mTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        mKeyColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_KEY);
        mIdColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_DOWNLOADID);
        mTitleColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_TITLE);
        mStatusColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_STATUS);
        mReasonColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_REASON);
        mTotalBytesColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_TOTAL_BYTES);
        mCurrentBytesColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_CURRENT_BYTES);
        mMediaTypeColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_MIMETYPE);
        mLocalUriColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_LOCALURI);
        mDateColumnId =
                cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_LAST_MODIFIED_TIMESTAMP);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        DownloadItem view = (DownloadItem) mInflater.inflate(R.layout.download_list_item, null);
        view.setSelectListener(mDownloadSelectionListener);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (!(view instanceof DownloadItem)) {
            return;
        }
        DownloadItem.DownloadItemTag tag = getItemTag();
        view.setTag(tag);

        Button opBtn = (Button) view.findViewById(R.id.button_op);
        opBtn.setFocusable(false);

        // Retrieve the icon for this download
        retrieveAndSetIcon(view);
        setProgress(view,android.R.id.progress);

        String title = cursor.getString(mTitleColumnId);
        if (title.isEmpty()) {
            title = mResources.getString(R.string.missing_title);
        }

        setTextForView(view, R.id.download_title, title);
        if (null != tag.versionName && tag.versionName.length()>0){
            setTextForView(view, R.id.size_text, tag.versionName);
        }else{
            setTextForView(view, R.id.size_text, getSizeText());
        }
        setTextForView(view, R.id.domain, mResources.getString(tag.statusRes));
        opBtn.setText(mResources.getText(tag.btnRes));
        if (tag.operate == DownloadItem.DownloadItemTag.OP_INSTALLING){
            opBtn.setEnabled(false);
        }else{
            opBtn.setEnabled(true);
        }
    }

    private DownloadItem.DownloadItemTag getItemTag(){
        DownloadItem.DownloadItemTag itemTag = new DownloadItem.DownloadItemTag();
        Cursor c = getCursor();
        itemTag.key = c.getString(mKeyColumnId);
        int reason = c.getInt(mReasonColumnId);
        int status = c.getInt(mStatusColumnId);
        ImplLog.d("impl_dm","bindView,["+itemTag.key+","+status+","+c.getInt(mCurrentBytesColumnId)+"/"+c.getInt(mTotalBytesColumnId)+"]");
        switch (status) {
            case Constant.STATUS_FAILED:
                itemTag.operate = DownloadItem.DownloadItemTag.OP_DOWNLOAD;
                if (DownloadManager.ERROR_INSUFFICIENT_SPACE == reason){
                    itemTag.statusRes = R.string.download_error_insufficient_space;
                }else{
                    itemTag.statusRes = R.string.download_error;
                }
                itemTag.btnRes = R.string.retry_download;
                break;

            case Constant.STATUS_SUCCESSFUL:
                itemTag.operate = DownloadItem.DownloadItemTag.OP_OPEN;
                itemTag.statusRes = R.string.download_success;
                itemTag.btnRes = R.string.open_download;
                itemTag.localUri = Uri.parse(c.getString(mLocalUriColumnId));
                itemTag.mediaType = c.getString(mMediaTypeColumnId);
                itemTag.intent = getOpenDownloadIntent(itemTag.localUri,itemTag.mediaType);
                if ("application/vnd.android.package-archive".equals(itemTag.mediaType)) {
                    PackageInfo archivePkg = mPackageManager.getPackageArchiveInfo(
                            itemTag.localUri.getPath(), PackageManager.GET_ACTIVITIES);
                    if (null != archivePkg){
                        itemTag.versionName = archivePkg.versionName;
                        itemTag.packageName = archivePkg.packageName;
                        try {
                            PackageInfo installPkg = mPackageManager.getPackageInfo(
                                    archivePkg.packageName,PackageManager.GET_ACTIVITIES);
                            if (installPkg.versionCode < archivePkg.versionCode){
                                //升级安装
                                itemTag.btnRes = R.string.upgrade;
                            }else{
                                //运行
                                itemTag.btnRes = R.string.open_download;
                                itemTag.operate = DownloadItem.DownloadItemTag.OP_LAUNCH;
                                itemTag.intent = getLaunchDownloadIntent(installPkg.packageName);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                            itemTag.btnRes = R.string.install;
                        }
                    }else{
                        //下载apk解析错误
                        itemTag.operate = DownloadItem.DownloadItemTag.OP_DOWNLOAD;
                        itemTag.statusRes = R.string.download_error;
                        itemTag.btnRes = R.string.retry_download;
                    }
                }
                break;

            case Constant.STATUS_PENDING:
            case Constant.STATUS_RUNNING:
                itemTag.operate = DownloadItem.DownloadItemTag.OP_DOWNLOAD;
                itemTag.statusRes = R.string.download_running;
                itemTag.btnRes = R.string.pause_download;
                break;

            case Constant.STATUS_PAUSED:
                itemTag.operate = DownloadItem.DownloadItemTag.OP_DOWNLOAD;
                itemTag.statusRes = R.string.download_running;
                itemTag.btnRes = R.string.pause_download;
                switch (reason) {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        itemTag.statusRes = R.string.download_queued;
                        break;
                    case DownloadManager.PAUSED_BY_APP:
                        itemTag.statusRes = R.string.download_paused;
                        itemTag.btnRes = R.string.conti_download;
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        itemTag.statusRes = R.string.download_waiting_for_network;
                        break;
                }
                break;
            case Constant.STATUS_INSTALLED:
                itemTag.operate = DownloadItem.DownloadItemTag.OP_LAUNCH;
                itemTag.statusRes = R.string.installed;
                itemTag.btnRes = R.string.open_download;
                break;
            case Constant.STATUS_PRIVATE_INSTALLING:
                itemTag.operate = DownloadItem.DownloadItemTag.OP_INSTALLING;
                itemTag.statusRes = R.string.installing;
                itemTag.btnRes = R.string.open_download;
                break;
            case Constant.STATUS_NORMAL_INSTALLING:
                itemTag.operate = DownloadItem.DownloadItemTag.OP_INSTALLING;
                itemTag.statusRes = R.string.installing;
                itemTag.btnRes = R.string.open_download;
                break;
            case Constant.STATUS_PACKAGE_INVALID:
                itemTag.operate = DownloadItem.DownloadItemTag.OP_DOWNLOAD;
                itemTag.statusRes = R.string.installing;
                itemTag.btnRes = R.string.open_download;
                break;
            case Constant.STATUS_INSTALL_FAILED:
                itemTag.operate = DownloadItem.DownloadItemTag.OP_DOWNLOAD;
                itemTag.statusRes = R.string.install_failed;
                itemTag.btnRes = R.string.open_download;
                break;
        }
        return itemTag;
    }

    private Intent getOpenDownloadIntent(Uri localUri,String mediaType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(localUri, mediaType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    private Intent getLaunchDownloadIntent(String packageName) {
        Intent intent = mPackageManager.getLaunchIntentForPackage(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return intent;
    }

    private String getDateString() {
        Date date = new Date(getCursor().getLong(mDateColumnId));
        if (date.before(getStartOfToday())) {
            return mDateFormat.format(date);
        } else {
            return mTimeFormat.format(date);
        }
    }

    private Date getStartOfToday() {
        Calendar today = new GregorianCalendar();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTime();
    }

    private String getSizeText() {
        long totalBytes = getCursor().getLong(mTotalBytesColumnId);
        long currentBytes = getCursor().getLong(mCurrentBytesColumnId);
        StringBuffer sizeText = new StringBuffer();
        if (totalBytes >= 0) {
            sizeText.append(Formatter.formatFileSize(mContext, currentBytes));
            sizeText.append("/");
            sizeText.append(Formatter.formatFileSize(mContext, totalBytes));
        }
        return sizeText.toString();
    }


    private void retrieveAndSetIcon(View convertView) {
        String mediaType = getCursor().getString(mMediaTypeColumnId);
        ImageView iconView = (ImageView) convertView.findViewById(R.id.download_icon);
        iconView.setVisibility(View.INVISIBLE);
        if (mediaType == null) {
            return;
        }

        Cursor c = getCursor();
        String iconUrl = c.getString(c.getColumnIndex(ImplConfig.COLUMN_ICON_URL));
        if(null != iconUrl){
            mFinalBitmap.display(iconView, iconUrl);
        }else if ("application/vnd.oma.drm.message".equalsIgnoreCase(mediaType)) {
            iconView.setImageDrawable(mResources.getDrawable(R.drawable.ic_launcher_drm_file));
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromParts("file", "", null), mediaType);
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
        }
        iconView.setVisibility(View.VISIBLE);
    }

    private void setTextForView(View parent, int textViewId, String text) {
        TextView view = (TextView) parent.findViewById(textViewId);
        view.setText(text);
    }

    private void setProgress(View parent,int viewId){
        Cursor c = getCursor();
        long totalBytes = c.getLong(mTotalBytesColumnId);
        long currentBytes = c.getLong(mCurrentBytesColumnId);
        ProgressBar p = (ProgressBar)parent.findViewById(viewId);
        if (null == p){
            return;
        }
        if (totalBytes > 0) {
            final int percent = (int) ((currentBytes * 100) / totalBytes);
            p.setIndeterminate(false);
            p.setMax(100);
            p.setProgress(percent);
        }else{
            p.setIndeterminate(false);
            p.setMax(100);
            p.setProgress(0);
        }
        p.setVisibility(View.VISIBLE);
    }
}
