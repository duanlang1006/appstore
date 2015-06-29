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
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.mit.impl.ImplStatusTag;
import com.applite.dm.DownloadItem.DownloadSelectListener;
import com.mit.impl.ImplConfig;
import net.tsz.afinal.FinalBitmap;
import java.text.DateFormat;
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
    private int mPackageColumnId;

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
        mTitleColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_TITLE);
        mStatusColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_STATUS);
        mReasonColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_REASON);
        mTotalBytesColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_TOTAL_BYTES);
        mCurrentBytesColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_CURRENT_BYTES);
        mMediaTypeColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_MIMETYPE);
        mLocalUriColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_LOCALURI);
        mDateColumnId =
                cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_LAST_MODIFIED_TIMESTAMP);
        mPackageColumnId = cursor.getColumnIndexOrThrow(ImplConfig.COLUMN_PACKAGENAME);
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
        DownloadItemViewHolder viewHolder = (DownloadItemViewHolder)view.getTag();
        if (null == viewHolder) {
            viewHolder = new DownloadItemViewHolder();
            viewHolder.actionBtn = (Button) view.findViewById(R.id.button_op);
            viewHolder.progressBar = (ProgressBar) view.findViewById(android.R.id.progress);
            viewHolder.descView = (TextView) view.findViewById(R.id.size_text);
            viewHolder.titleView = (TextView) view.findViewById(R.id.download_title);
            viewHolder.statusView = (TextView) view.findViewById(R.id.domain);
            viewHolder.iconView = (ImageView) view.findViewById(R.id.download_icon);
            view.setTag(viewHolder);
        }
        Cursor c = getCursor();
        String localUri = c.getString(mLocalUriColumnId);
        viewHolder.statusTag = ImplStatusTag.generateTag(mContext,
                c.getString(mKeyColumnId),
                c.getString(mPackageColumnId),
                c.getInt(mStatusColumnId),
                c.getInt(mReasonColumnId),
                c.getLong(mCurrentBytesColumnId),
                c.getLong(mTotalBytesColumnId),
                (null == localUri) ? null : Uri.parse(localUri),
                c.getString(mMediaTypeColumnId));

        viewHolder.actionBtn.setText(viewHolder.statusTag.getActionText());
        if (viewHolder.statusTag.getAction() == ImplStatusTag.ACTION_INSTALL){
            viewHolder.actionBtn.setEnabled(false);
        }else{
            viewHolder.actionBtn.setEnabled(true);
        }
        viewHolder.descView.setText(viewHolder.statusTag.getDescText());
        String title = cursor.getString(mTitleColumnId);
        if (title.isEmpty()) {
            title = mResources.getString(R.string.missing_title);
        }
        viewHolder.titleView.setText(title);
        viewHolder.statusView.setText(viewHolder.statusTag.getStatusText());
        setIcon(viewHolder.iconView);
        setProgress(viewHolder.progressBar,viewHolder.statusTag.getPercent());
    }

//    private ImplStatusTag getItemTag(){
//        Cursor c = getCursor();
//        String key = c.getString(mKeyColumnId);
//        int reason = c.getInt(mReasonColumnId);
//        int status = c.getInt(mStatusColumnId);
//        ImplStatusTag tag = new ImplStatusTag(key,c.getString(mPackageColumnId));
//        switch (status) {
//            case Constant.STATUS_FAILED:
//                tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
//                tag.setActionString(mResources.getString(R.string.retry_download));
//                if (DownloadManager.ERROR_INSUFFICIENT_SPACE == reason){
//                    tag.setStatusString(mResources.getString(R.string.download_error_insufficient_space));
//                }else{
//                    tag.setStatusString(mResources.getString(R.string.download_error));
//                }
//                tag.setDescString(getSizeText());
//                break;
//
//            case Constant.STATUS_SUCCESSFUL:
//                tag.setAction(ImplStatusTag.ACTION_OPEN);
//                tag.setActionString(mResources.getString(R.string.open_download));
//                tag.setStatusString(mResources.getString(R.string.download_success));
//                tag.setDescString(getSizeText());
//                Uri localUri = Uri.parse(c.getString(mLocalUriColumnId));
//                String mediaType = c.getString(mMediaTypeColumnId);
//                tag.setIntent(getOpenDownloadIntent(localUri,mediaType));
//                if ("application/vnd.android.package-archive".equals(mediaType)) {
//                    PackageInfo archivePkg = mPackageManager.getPackageArchiveInfo(
//                            localUri.getPath(), PackageManager.GET_ACTIVITIES);
//                    if (null != archivePkg){
//                        tag.setAction(ImplStatusTag.ACTION_OPEN);
//                        tag.setDescString(String.format(mResources.getString(R.string.apk_version),archivePkg.versionName));
//                        Intent intent = getLaunchDownloadIntent(archivePkg.packageName);
//                        tag.setIntent(intent);
//                        if (null == intent){
//                            tag.setActionString(mResources.getString(R.string.install));
//                        }else{
//                            tag.setActionString(mResources.getString(R.string.run));
//                        }
//                    }else{//下载apk解析错误
//                        tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
//                        tag.setStatusString(mResources.getString(R.string.apk_invalid));
//                        tag.setActionString(mResources.getString(R.string.retry_download));
//                        tag.setDescString(getSizeText());
//                        tag.setIntent(null);
//                    }
//                }
//                break;
//
//            case Constant.STATUS_PENDING:
//            case Constant.STATUS_RUNNING:
//                tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
//                tag.setStatusString(mResources.getString(R.string.download_running));
//                tag.setActionString(mResources.getString(R.string.pause_download));
//                tag.setDescString(getSizeText());
//                tag.setIntent(null);
//                break;
//
//            case Constant.STATUS_PAUSED:
//                tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
//                switch (reason) {
//                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
//                        tag.setStatusString(mResources.getString(R.string.download_queued));
//                        tag.setActionString(mResources.getString(R.string.pause_download));
//                        break;
//                    case DownloadManager.PAUSED_BY_APP:
//                        tag.setStatusString(mResources.getString(R.string.download_paused));
//                        tag.setActionString(mResources.getString(R.string.conti_download));
//                        break;
//                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
//                        tag.setStatusString(mResources.getString(R.string.download_waiting_for_network));
//                        tag.setActionString(mResources.getString(R.string.pause_download));
//                        break;
//                    default:
//                        tag.setStatusString(mResources.getString(R.string.download_running));
//                        tag.setActionString(mResources.getString(R.string.pause_download));
//                        break;
//                }
//                tag.setDescString(getSizeText());
//                tag.setIntent(null);
//                break;
//            case Constant.STATUS_INSTALLED:
//                try {
//                    PackageInfo installPkg = mPackageManager.getPackageInfo(tag.getPackageName(),PackageManager.GET_ACTIVITIES);
//                    tag.setAction(ImplStatusTag.ACTION_OPEN);
//                    tag.setStatusString(mResources.getString(R.string.installed));
//                    tag.setActionString(mResources.getString(R.string.apk_run));
//                    tag.setIntent(getLaunchDownloadIntent(tag.getPackageName()));
//                    tag.setDescString(String.format(mResources.getString(R.string.apk_version),installPkg.versionName));
//                } catch (PackageManager.NameNotFoundException e) {
//                    e.printStackTrace();
//                    tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
//                    tag.setStatusString(mResources.getString(R.string.apk_packagename_invalid));
//                    tag.setActionString(mResources.getString(R.string.retry_download));
//                    tag.setDescString(getSizeText());
//                    tag.setIntent(null);
//                }
//                break;
//            case Constant.STATUS_PRIVATE_INSTALLING:
//                tag.setAction(ImplStatusTag.ACTION_INSTALL);
//                tag.setStatusString(mResources.getString(R.string.installing));
//                tag.setActionString(mResources.getString(R.string.open_download));
//                tag.setDescString(getSizeText());
//                tag.setIntent(null);
//                break;
//            case Constant.STATUS_NORMAL_INSTALLING:
//                tag.setAction(ImplStatusTag.ACTION_INSTALL);
//                tag.setStatusString(mResources.getString(R.string.installing));
//                tag.setActionString(mResources.getString(R.string.open_download));
//                tag.setDescString(getSizeText());
//                tag.setIntent(null);
//                break;
//            case Constant.STATUS_PACKAGE_INVALID:
//                tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
//                tag.setStatusString(mResources.getString(R.string.package_invalid));
//                tag.setActionString(mResources.getString(R.string.open_download));
//                tag.setDescString(getSizeText());
//                tag.setIntent(null);
//                break;
//            case Constant.STATUS_INSTALL_FAILED:
//                tag.setAction(ImplStatusTag.ACTION_DOWNLOAD);
//                tag.setStatusString(mResources.getString(R.string.install_failed));
//                tag.setActionString(mResources.getString(R.string.open_download));
//                tag.setDescString(getSizeText());
//                tag.setIntent(null);
//                break;
//        }
//        return tag;
//    }

//    private Intent getOpenDownloadIntent(Uri localUri,String mediaType) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(localUri, mediaType);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        return intent;
//    }
//
//    private Intent getLaunchDownloadIntent(String packageName) {
//        Intent intent = mPackageManager.getLaunchIntentForPackage(packageName);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//        return intent;
//    }
//
//    private String getDateString() {
//        Date date = new Date(getCursor().getLong(mDateColumnId));
//        if (date.before(getStartOfToday())) {
//            return mDateFormat.format(date);
//        } else {
//            return mTimeFormat.format(date);
//        }
//    }

//    private Date getStartOfToday() {
//        Calendar today = new GregorianCalendar();
//        today.set(Calendar.HOUR_OF_DAY, 0);
//        today.set(Calendar.MINUTE, 0);
//        today.set(Calendar.SECOND, 0);
//        today.set(Calendar.MILLISECOND, 0);
//        return today.getTime();
//    }
//
//    private String getSizeText() {
//        long totalBytes = getCursor().getLong(mTotalBytesColumnId);
//        long currentBytes = getCursor().getLong(mCurrentBytesColumnId);
//        StringBuffer sizeText = new StringBuffer();
//        if (totalBytes >= 0) {
//            sizeText.append(Formatter.formatFileSize(mContext, currentBytes));
//            sizeText.append("/");
//            sizeText.append(Formatter.formatFileSize(mContext, totalBytes));
//        }
//        return sizeText.toString();
//    }


    private void setIcon(ImageView iconView) {
        Cursor c = getCursor();
        String mediaType = c.getString(mMediaTypeColumnId);
        String iconUrl = c.getString(c.getColumnIndex(ImplConfig.COLUMN_ICON_URL));

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
        if(null != iconUrl){
            mFinalBitmap.display(iconView, iconUrl);
        }
    }

//    private void setTextForView(View parent, int textViewId, String text) {
//        TextView view = (TextView) parent.findViewById(textViewId);
//        view.setText(text);
//    }
//
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
        Button actionBtn;
        ImageView iconView;
        ImplStatusTag statusTag;
    }
}
