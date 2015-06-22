/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.applite.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;

import android.content.ComponentName;
import android.util.SparseArray;

/**
 * Stores the list of all applications for the all apps view.
 */
class AllAppsList {
    static final int ALL_CHANGED = 1;
    static final int ADDED_CHANGED = 2;
    static final int REMOVED_CHANGED = 3;
    static final int MODIFIED_CHANGED = 4;
    
    class AppSet extends Observable{
        final boolean recommend; 
        ArrayList<IAppInfo> data = new ArrayList<IAppInfo>();
        ArrayList<IAppInfo> added = new ArrayList<IAppInfo>();
        ArrayList<IAppInfo> removed = new ArrayList<IAppInfo>();
        ArrayList<IAppInfo> modified = new ArrayList<IAppInfo>();
        AppSet next ;
        
        public AppSet( boolean rec) {
            recommend = rec;
        }
        
        void add(IAppInfo item) {
            data.add(item);
            added.add(item);
//            notifyObservers(ADDED_CHANGED);
        }
        void remove(IAppInfo item) {
            removed.add(item);
            data.remove(item);
//            notifyObservers(REMOVED_CHANGED);
        }
        
        void update(IAppInfo item) {
            modified.add(item);
//            notifyObservers(MODIFIED_CHANGED);
//            mIconCache.remove(item.getComponentName());
        }
        
        void clear(){
            data.clear();
            added.clear();
            removed.clear();
            modified.clear();
        }
    }
    private SparseArray<AppSet> mAppsMap;
    private IconCache mIconCache;

    /**
     * Boring constructor.
     */
    public AllAppsList(IconCache iconCache) {
        mIconCache = iconCache;
        
        mAppsMap = new SparseArray<AppSet>();
        AppSet onlineList = new AppSet(true);
        AppSet offlineList = new AppSet(false);
        onlineList.next = offlineList;
        offlineList.next = onlineList;

        mAppsMap.put(IAppInfo.AppOnline, onlineList);
        mAppsMap.put(IAppInfo.AppMore, onlineList);
        mAppsMap.put(IAppInfo.AppOffline, offlineList);
        mAppsMap.put(IAppInfo.AppInstalled, offlineList);
        mAppsMap.put(IAppInfo.AppUpgrade, offlineList);
    }

    /**
     * Add the supplied IAppInfo objects to the list, and enqueue it into the
     * list to broadcast when notify() is called.
     *
     * If the app is already in the list, doesn't add it.
     */
    public void add(IAppInfo item) {
        final AppSet apps = mAppsMap.get(item.getItemType());
        if (null == containIAppInfo(apps.data,item.getId())){
            apps.add(item);
        }
    }
    
    /**
     * Remove the apps for the given apk identified by packageName.
     */
    public void remove(IAppInfo item) {
        final AppSet apps = mAppsMap.get(item.getItemType());
        final IAppInfo info = containIAppInfo(apps.data,item.getId());
        if (null != info){
            apps.remove(info);
        }
        // This is more aggressive than it needs to be.
//        mIconCache.flush();
    }
    
    /**
     * Add and remove icons for this package which has been updated.
     */
    public void update(IAppInfo item) {
        final AppSet apps = mAppsMap.get(item.getItemType());
        final AppSet others = apps.next;
        final IAppInfo info = containIAppInfo(apps.data,item.getId());
        if (null != info){
            apps.update(info);
        }else{
            apps.add(item);
            others.remove(item);
        }
    }
    public void clear() {
        for (AppSet apps:getSets()){
            apps.clear();
        }
    }
    
    public AppSet getRecommendSet(){
        return mAppsMap.get(IAppInfo.AppOnline);
    }
    
    public AppSet getOfflineSet(){
        return mAppsMap.get(IAppInfo.AppOffline);
    }

    public Collection<AppSet> getSets(){
        HashSet<AppSet> h = new HashSet<AppSet>();
        for (int i = 0; i<mAppsMap.size();i++){
            h.add(mAppsMap.valueAt(i));
        }
        return h;
    }
    
    public ArrayList<IAppInfo> getAllData(){
        ArrayList<IAppInfo> data = new ArrayList<IAppInfo>();
        for (AppSet app : getSets()){
            data.addAll(app.data);
        }
        return data;
    }

    public IAppInfo getIAppInfo(String packageName){
        for (AppSet apps:getSets()){
            final List<IAppInfo> list = apps.data;
            for (int i = list.size() - 1; i >= 0; i--) {
                IAppInfo info = list.get(i);
                final ComponentName component = info.getComponentName();
                if (packageName.equals(component.getPackageName())) {
                    return info;
                }
            }
        }
        return null;
    }
    
    public IAppInfo getIAppInfoWithId(String id){
        for (AppSet apps:getSets()){
            final List<IAppInfo> list = apps.data;
            for (int i = list.size() - 1; i >= 0; i--) {
                IAppInfo info = list.get(i);
                if (id.equals(info.getId())) {
                    return info;
                }
            }
        }
        return null;
    }
    
    public IAppInfo getIAppInfoWithDownloadId(long id){
        if (id < 0) return null;
        for (AppSet apps:getSets()){
            final List<IAppInfo> list = apps.data;
            for (int i = list.size() - 1; i >= 0; i--) {
                ApplicationInfo info = (ApplicationInfo)list.get(i);
                if (id == info.downloadId){
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * Returns whether <em>apps</em> contains <em>component</em>.
     */
    private static IAppInfo containIAppInfo(
            ArrayList<IAppInfo> list, String id) {
        for (int i = list.size() - 1; i >= 0; i--) {
            IAppInfo info = list.get(i);
            if (info.getId().equals(id)) {
            	return info;
            }
        }
        return null;
    }
}
