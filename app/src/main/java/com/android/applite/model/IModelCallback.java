package com.android.applite.model;

import java.util.ArrayList;
import java.util.List;

public interface IModelCallback {
    public boolean setLoadOnResume();
    
    public void startRefresh();
    public void refreshing();
    public void finishRefresh();
    
    public void bindAll(final boolean recommend,final ArrayList<IAppInfo> apps);
    public void bindAdded(final boolean recommend,final ArrayList<IAppInfo> apps);
    public void bindUpdated(final boolean recommend,final ArrayList<IAppInfo> apps);
    public void bindRemoved(final boolean recommend,final ArrayList<IAppInfo> apps, boolean permanent);
    
    public void downloadStatusFailed(final String str,final long downloadId);
    public void getDataFailed();
    
    public void wakeDetail(IAppInfo info);
		
}
