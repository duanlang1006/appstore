package com.android.applite.plugin;

import android.content.Intent;

import com.android.applite.model.IAppInfo;

public interface IAppLiteOperator {
    public void startActivitySafely(Intent intent, Object tag);
    public void onOnlineAppClick(IAppInfo info);
    public void onOfflineAppClick(IAppInfo info);
    public void onRemoveAppClick(IAppInfo info);
    public void onShowIndication(IAppInfo info);
    public void onAppOnLongClick();
}
