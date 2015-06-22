package com.mit.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hxd on 15-5-29.
 */
public class ApkplugUpdateBean {
    //需要查询的插件版本信息  每次查询数量最多30个
    private List<ApkplugUpdateInfo> apps=null;
    public List<ApkplugUpdateInfo> getApps() {
        if(apps==null){
            apps=new ArrayList<ApkplugUpdateInfo>();
        }
        return apps;
    }
    public void setApps(List<ApkplugUpdateInfo> apps) {
        this.apps = apps;
    }
    public void addAppInfo(ApkplugUpdateInfo appinfo){
        this.getApps().add(appinfo);
    }
}
