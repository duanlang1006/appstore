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
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import android.widget.Toast;

import com.applite.common.Constant;
import com.applite.common.LogUtils;
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
        if (mListener.getFlag()) {//显示删除多选框
            vh.deleteCheckBox.setVisibility(View.VISIBLE);
            vh.deleteCheckBox.setChecked(mListener.getStatus(position));
            if (false == oldFlag && true == mListener.getFlag()) {
                vh.deleteCheckBox.startAnimation(vh.animaCheckBox);
                if (position == getCount() - 1) {
                    oldFlag = true;
                }
            }
            vh.actionBtn.setVisibility(View.GONE);
            vh.custompb.setVisibility(View.GONE);
        } else {//正常状态(没有删除的多选框)
            vh.deleteCheckBox.setVisibility(View.GONE);
            if (mListener.getStatusFlags() == 23) {
//            if (vh.flagDownLoading == 1) {
                vh.actionBtn.setVisibility(View.GONE);
                vh.custompb.setVisibility(View.VISIBLE);
            } else if (mListener.getStatusFlags() == -24) {
//            } else if (vh.flagDownLoading == 1) {
                vh.actionBtn.setVisibility(View.VISIBLE);
                vh.custompb.setVisibility(View.GONE);
            }
            vh.deleteCheckBox.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        ViewHolder vh = (ViewHolder) v.getTag();
//        if (R.id.button_op == v.getId()) {
        ImplHelper.onClick(mContext,
                vh.implInfo,
                vh.implInfo.getDownloadUrl(),
                vh.implInfo.getTitle(),
                vh.implInfo.getIconUrl(),
                Environment.getExternalStorageDirectory() + File.separator + Constant.extenStorageDirPath + vh.implInfo.getTitle() + ".apk",
                null,
                vh);
        vh.refresh();
    }

    class ViewHolder implements ImplChangeCallback {
        //        ProgressBar progressBar;
        CustomProgressBar custompb;
        TextView titleView;
        TextView descView;
        TextView statusView;
        TextView actionBtn;
        CheckBox deleteCheckBox;
        ImageView iconView;
        ImplInfo implInfo;
        Animation animaCheckBox;
        int progress = 0;
        ProgressThread progressThread = new ProgressThread();
        int flagDownLoading = 0;//1 下载中;2 下载完成


        ViewHolder(View view) {
            actionBtn = (TextView) view.findViewById(R.id.button_op);
            custompb = (CustomProgressBar) view.findViewById(R.id.cpb);
            deleteCheckBox = (CheckBox) view.findViewById(R.id.delete_checkBox);
//            progressBar = (ProgressBar) view.findViewById(android.R.id.progress);
            descView = (TextView) view.findViewById(R.id.size_text);
            titleView = (TextView) view.findViewById(R.id.download_title);
            statusView = (TextView) view.findViewById(R.id.domain);
            iconView = (ImageView) view.findViewById(R.id.download_icon);
            actionBtn.setTag(this);
            deleteCheckBox.setTag(this);
//            progressBar.setTag(this);
            custompb.setTag(this);


        }

        void initView(ImplInfo info) {
            this.implInfo = info;
            if (null == this.implInfo) {
                return;
            }
//            actionBtn.setText(implAgent.getActionText(implInfo));//??
//            descView.setText(implAgent.getDescText(implInfo));//??
            implAgent.setImplCallback(this, implInfo);
            String title = implInfo.getTitle();
            if (null == title || title.isEmpty()) {
                title = mContext.getResources().getString(R.string.missing_title);
            }
            titleView.setText(title);
            setIcon();
            refresh();
            animaCheckBox = AnimationUtils.loadAnimation(mContext, R.anim.checkbox_in);
//            progressBar.setIndeterminate(false);
//            progressBar.setMax(100);
        }

        void refresh() {
            if (null == this.implInfo) {
                return;
            }
            actionBtn.setText(ImplHelper.getStatusText(mContext, implInfo));
            actionBtn.setEnabled(true);
            switch (implInfo.getStatus()) {
                case ImplInfo.STATUS_PRIVATE_INSTALLING://静默安装
//                    actionBtn.setEnabled(false);
                    flagDownLoading = 2;
                    actionBtn.setVisibility(View.VISIBLE);
                    custompb.setVisibility(View.GONE);
                    break;
                case ImplInfo.ACTION_DOWNLOAD://下载
//                    LogUtils.d("wanghc", "下载");
                    actionBtn.setVisibility(View.GONE);
                    custompb.setVisibility(View.VISIBLE);
                    custompb.setImageResource(R.drawable.action_button_update_normal_light);
                    flagDownLoading = 1;
                    break;
                case ImplInfo.ACTION_INSTALL://安装过程   ------->这里有时下载也会显示安装过程!
//                    LogUtils.d("wanghc", "安装过程");
                    actionBtn.setVisibility(View.GONE);
                    custompb.setVisibility(View.VISIBLE);
                    custompb.setImageResource(R.drawable.action_button_update_normal_light);
                    flagDownLoading = 1;
                    break;
                case ImplInfo.ACTION_OPEN://打开下载文件或者运行应用程序
//                    LogUtils.d("wanghc", "打开/运行");
                    Toast.makeText(mContext, "打开/运行", Toast.LENGTH_SHORT).show();
                    break;
//                        case ImplInfo.STATUS_PENDING://下载等待中
//                Toast.makeText(mContext, implInfo.getStatus() + "", Toast.LENGTH_SHORT).show();
//                            break;
//                        case ImplInfo.STATUS_RUNNING://下载进行中
//                Toast.makeText(mContext, implInfo.getStatus() + "", Toast.LENGTH_SHORT).show();
//                            break;
                case ImplInfo.STATUS_PAUSED://下载暂停
//                    LogUtils.d("wanghc", "下载暂停");
                    actionBtn.setVisibility(View.GONE);
                    custompb.setVisibility(View.VISIBLE);
                    custompb.setImageResource(R.drawable.action_button_pause_normal_light);
                    flagDownLoading = 1;
                    break;
                case ImplInfo.STATUS_FAILED://下载失败
//                    LogUtils.d("wanghc", "下载失败");
                    Toast.makeText(mContext, "下载失败", Toast.LENGTH_SHORT).show();
                    break;
                case ImplInfo.STATUS_NORMAL_INSTALLING://普通安装
                    LogUtils.d("wanghc", "普通安装");
                    flagDownLoading = 2;
                    actionBtn.setVisibility(View.VISIBLE);
                    custompb.setVisibility(View.GONE);
                    break;
                case ImplInfo.STATUS_SUCCESSFUL://下载成功
                    flagDownLoading = 2;
                    actionBtn.setVisibility(View.VISIBLE);
                    custompb.setVisibility(View.GONE);
                    break;
                case ImplInfo.STATUS_INSTALLED: //已安装
//                    LogUtils.d("wanghc", "已安装");
                    Toast.makeText(mContext, "已安装", Toast.LENGTH_SHORT).show();
                    break;
                case ImplInfo.STATUS_INSTALL_FAILED: //安装失败
//                    LogUtils.d("wanghc", "安装失败");
                    Toast.makeText(mContext, "安装失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
//                    LogUtils.d("wanghc", "其他");
                    Toast.makeText(mContext, "其他", Toast.LENGTH_SHORT).show();
                    break;
            }
            statusView.setText(ImplHelper.getStatusText(mContext, implInfo));
            setProgress();
        }

        private void setIcon() {
            Bitmap resBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.file_type_apk);
            String url = implInfo.getIconUrl();
            if (null != url && !TextUtils.isEmpty(url)) {
            }
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
        }

        private void setProgress() {
//            progressBar.setProgress(ImplHelper.getProgress(mContext, implInfo));
//            progressBar.setVisibility(View.VISIBLE);
//            custompb.setProgress(ImplHelper.getProgress(mContext, implInfo));
            custompb.setVisibility(View.VISIBLE);
            if (!progressThread.isAlive() && 0 == progress) {
                progressThread.start();
            }
        }

        @Override
        public void onChange(ImplInfo info) {
            refresh();
        }

        private Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                // 进度
//                progressBar.setProgress(msg.what);
                custompb.setProgress(msg.what);
                descView.setText(ImplHelper.getDescText(mContext, implInfo));
            }
        };

        private class ProgressThread extends Thread {
            @Override
            public void run() {
//                super.run();
                while (100 != progress) {//这里应该是下载等待和下载中
                    Message msg = new Message();
                    progress = ImplHelper.getProgress(mContext, implInfo);
                    msg.what = progress;
                    handler.sendMessage(msg);
                    try {
                        sleep(300);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }


}
