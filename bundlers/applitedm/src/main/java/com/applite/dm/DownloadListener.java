package com.applite.dm;

/**
 * Created by wanghaochen on 15-8-25.
 */
public interface DownloadListener {
    boolean getFlag1();
    boolean getStatus(int position);
    int getTitleId();
    boolean getFlag2();
    void setFlag2(boolean b);
}