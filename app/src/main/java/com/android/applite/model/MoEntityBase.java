package com.android.applite.model;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class MoEntityBase {
    private static final long serialVersionUID = 1L;
    private String funcName;   //功能名

    private String uuid;
    private String userId;      //第三方DSC系统分配的user id
    private String versionName; //本应用的version
    private String ipAddress;
    private String deviceName;
    private String deviceSwVersion;
    private String dataTimestamp;
    private int category;
    private String detail;
    public MoEntityBase(String funcName) {
        super();
        this.funcName = funcName;
    }

    public String getFuncName() {
        return funcName;
    }
    
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getVersionName() {
        return versionName;
    }
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
    
    public String getDataTimestamp() {
        return dataTimestamp;
    }

    public void setDataTimestamp(String dataTimestamp) {
        this.dataTimestamp = dataTimestamp;
    }
    
    public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDeviceSwVersion() {
		return deviceSwVersion;
	}

	public void setDeviceSwVersion(String deviceSwVersion) {
		this.deviceSwVersion = deviceSwVersion;
	}

	public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
    
    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public List<NameValuePair> toNameValuePair(){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.clear();
        return toNameValuePair(params);
    }

    public List<NameValuePair> toNameValuePair(List<NameValuePair> params){
        params.add(new BasicNameValuePair("func", (null==funcName)?"":funcName));
        params.add(new BasicNameValuePair("uuid", (null==uuid)?"":uuid));
        params.add(new BasicNameValuePair("bduserid", (null == userId)?"":userId));
        params.add(new BasicNameValuePair("swver", (null==versionName)?"":versionName));
        params.add(new BasicNameValuePair("data_timestamp", (null==dataTimestamp)?"":dataTimestamp));
        params.add(new BasicNameValuePair("category", Integer.toString(category)));
        params.add(new BasicNameValuePair("detail", (null == detail)?"":detail));
        return params;
    }
}
