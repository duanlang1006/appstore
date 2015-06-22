package com.mit.bean;

/**
 * Created by hxd on 15-5-29.
 */
public interface ApkplugUpdateCallback {
    /**
     * 获取成功
     */
    public final int success=0;
    /**
     * 数据解析失败
     */
    public final int msg_exp_fail=-1;
    /**
     * 网络连接失败
     */
    public final int net_fail=-2;
    /**
     * 服务返回错误消息
     */
    public final int msg_fail=-3;
    /**
     * 只有有新版本的app才会被传回
     * @param stutas
     * @param newapps 可更新app列表
     */
    public void onSuccess(int stutas,ApkplugQueryModel<ApkplugModel> aqm);
    public void onFailure(int errorNo ,String strMsg);
}