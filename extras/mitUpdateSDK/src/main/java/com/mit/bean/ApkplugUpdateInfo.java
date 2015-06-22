package com.mit.bean;

/**
 * Created by hxd on 15-5-29.
 */
public class ApkplugUpdateInfo implements java.io.Serializable{
    //插件package id
    public String appid=null;
    //插件 plugin.xml中版本
    public String bundlevarsion=null;
    //插件AndroidManifest.xml中版本
    public int versionCode=0;
    public ApkplugUpdateInfo(){}
    public ApkplugUpdateInfo(String appid,String bundlevarsion,int versionCode){
        this.appid=appid;
        this.bundlevarsion=bundlevarsion;
        this.versionCode=versionCode;
    }
}
