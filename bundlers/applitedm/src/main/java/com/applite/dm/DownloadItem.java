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
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;


/**
 * This class customizes RelativeLayout to directly handle clicks on the left part of the view and
 * treat them at clicks on the checkbox. This makes rapid selection of many items easier. This class
 * also keeps an ID associated with the currently displayed download and notifies a listener upon
 * selection changes with that ID.
 */
public class DownloadItem extends LinearLayout implements View.OnClickListener{
    private Button mOpButton;
    private Button mDeleteButton;
    private Button mDetailButton;
    private DownloadSelectListener mListener;

    static interface DownloadSelectListener {
        public void onDownloadButtonClicked(DownloadItemTag tag);
        public void onDeleteButtonClicked(DownloadItemTag tag);
        public void onDetailButtonClicked(DownloadItemTag tag);
    }

    static class DownloadItemTag{
        final static int OP_OPEN = 0;       //打开下载文件
        final static int OP_DOWNLOAD = 1;   //下载
        final static int OP_LAUNCH = 2;     //运行
        final static int OP_INSTALLING = 3;     //安装中

        String key;
        int operate;
        int btnRes;
        int statusRes;
        Uri localUri;
        String mediaType;
        String versionName;
        String packageName;
        Intent intent;

        DownloadItemTag() {
            key = null;
            operate = -1;
            btnRes = 0;
            statusRes = 0;
            versionName = null;
            packageName = null;
            intent = null;
            mediaType = null;
            localUri = null;
        }
    }

    public DownloadItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DownloadItem(Context context) {
        super(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mOpButton = (Button)findViewById(R.id.button_op);
        mDeleteButton = (Button)findViewById(R.id.button_delete);
        mDetailButton = (Button)findViewById(R.id.button_detail);
        mOpButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
        mDetailButton.setOnClickListener(this);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadItem item = (DownloadItem)v;
                View extra = item.findViewById(R.id.extra_line);
                if (extra.getVisibility() == View.GONE){
                    extra.setVisibility(View.VISIBLE);
                }else if (extra.getVisibility() == View.VISIBLE){
                    extra.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_delete:
                mListener.onDeleteButtonClicked((DownloadItemTag)getTag());
                break;
            case R.id.button_detail:
                mListener.onDetailButtonClicked((DownloadItemTag)getTag());
                break;
            case R.id.button_op:
                mListener.onDownloadButtonClicked((DownloadItemTag)getTag());
                break;
        }
    }

    public void setSelectListener(DownloadSelectListener listener) {
        mListener = listener;
    }
}
