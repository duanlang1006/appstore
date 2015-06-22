package com.kapps.market.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.kapps.market.bean.UserInfo;
import com.kapps.market.util.Constants;

/**
 * 2010-6-13 喜锟斤拷锟斤拷锟斤拷锟斤拷锟�
 * 
 * @author admin
 * 
 */
public class SharedPrefManager {

	// 锟矫伙拷锟斤拷息锟斤拷锟斤拷
	private SharedPreferences userInfoPref;
	// 锟叫筹拷锟斤拷锟斤拷
	private SharedPreferences marketConfigPref;

	public SharedPrefManager(Context context) {
		userInfoPref = context.getSharedPreferences(Constants.USER_INFO_PREF, Context.MODE_PRIVATE);
		marketConfigPref = context.getSharedPreferences(Constants.MARKET_CONFIG_PREF, Context.MODE_PRIVATE);
	}

	/**
	 * @return the userInfoPref
	 */
	public SharedPreferences getUserInfoPref() {
		return userInfoPref;
	}

	/**
	 * @return the marketConfigPref
	 */
	public SharedPreferences getMarketConfigPref() {
		return marketConfigPref;
	}

	/**
	 * 锟斤拷锟斤拷没锟斤拷锟较�
	 * 
	 * @param context
	 * @return
	 */
	public UserInfo getUserInfo() {
		SharedPreferences prefer = getUserInfoPref();
		UserInfo userInfo = new UserInfo();
		userInfo.setName(prefer.getString(Constants.USER_NAME, null));
		userInfo.setPassword(prefer.getString(Constants.USER_PASSWORD, null));
		userInfo.setEmail(prefer.getString(Constants.USER_EMAIL, null));
		return userInfo;
	}

	/**
	 * 锟斤拷锟斤拷锟矫伙拷锟斤拷息
	 * 
	 * @param context
	 * @return
	 */
	public void saveUserInfo(UserInfo userInfo) {
		SharedPreferences prefer = getUserInfoPref();
		Editor editor = prefer.edit();
		editor.putString(Constants.USER_NAME, userInfo.getName());
		editor.putString(Constants.USER_PASSWORD, userInfo.getPassword());
		editor.putString(Constants.USER_EMAIL, userInfo.getEmail());
		editor.commit();
	}

	/**
	 * 锟斤拷锟街会话
	 */
	public void saveSession(String uid) {
		SharedPreferences prefer = getUserInfoPref();
		Editor editor = prefer.edit();
		editor.putString(Constants.USER_SID, uid);
		editor.commit();
	}

	/**
	 * 锟斤拷没峄�
	 * 
	 * @return
	 */
	public String getSession() {
		SharedPreferences prefer = getUserInfoPref();
		return prefer.getString(Constants.USER_SID, null);
	}

	/**
	 * 注锟斤拷锟绞憋拷锟斤拷锟斤拷锟矫伙拷锟斤拷息<br>
	 * 锟斤拷锟斤拷锟斤拷锟角憋拷锟斤拷锟矫伙拷锟斤拷偷锟铰斤拷锟绞斤拷员惴斤拷锟斤拷没锟斤拷锟�
	 */
	public void initUserInfoForLogout() {
		SharedPreferences prefer = getUserInfoPref();
		Editor editor = prefer.edit();
		editor.putString(Constants.USER_PASSWORD, null);
		editor.putString(Constants.USER_EMAIL, null);
		editor.putString(Constants.USER_SID, null);
		editor.commit();
	}

	// 锟斤拷锟絫ab锟斤拷锟斤拷锟斤拷冒姹�
	public int getCategroyTabVersion(String tab) {
		SharedPreferences prefer = getMarketConfigPref();
		return prefer.getInt(tab, -1);
	}

	// 锟斤拷锟斤拷tab锟斤拷锟斤拷锟斤拷冒姹�
	public void saveCategoryTabVersion(String tab, int version) {
		SharedPreferences prefer = getMarketConfigPref();
		Editor editor = prefer.edit();
		editor.putInt(tab, version);
		editor.commit();
	}

	// 锟斤拷锟斤拷锟叫憋拷应锟斤拷tab
	public void saveCategoryAppTab(String tab, int index) {
		SharedPreferences prefer = getMarketConfigPref();
		Editor editor = prefer.edit();
		editor.putInt(tab, index);
		editor.commit();
	}

	// 锟斤拷取锟斤拷锟接︼拷锟絫ab
	public int getCategoryAppTab(String tab, int dIndex, boolean update) {
		if (update) {
			saveCategoryAppTab(tab, dIndex);
			return dIndex;

		} else {
			SharedPreferences prefer = getMarketConfigPref();
			return prefer.getInt(tab, dIndex);
		}
	}
	/**
	 * 
	 * @param state
	 */
	public void setInstallState(boolean state)
	{
		SharedPreferences prefer = getUserInfoPref();
		Editor editor = prefer.edit();
		editor.putBoolean("installed", state);
		editor.commit();
	}
	/**
	 * 
	 * @return
	 */
	public boolean getInstallState()
	{
		SharedPreferences prefer = getUserInfoPref();
		return prefer.getBoolean("installed", false);
		
	}
	
	/**
	 * 
	 * @param state
	 */
	public void setHaveInstallShortcut(boolean state)
	{
		SharedPreferences prefer = getMarketConfigPref();
		Editor editor = prefer.edit();
		editor.putBoolean(Constants.EXIST_SHORT_CUT_PREF_KEY, state);
		editor.commit();
	}
	/**
	 * 
	 * @return
	 */
	public boolean getHaveInstallShortcut()
	{
		SharedPreferences prefer = getMarketConfigPref();
		return prefer.getBoolean(Constants.EXIST_SHORT_CUT_PREF_KEY, false);
	}

    public void setIgnoredUpdateVersion(int code) {
        SharedPreferences prefer = getMarketConfigPref();
        Editor editor = prefer.edit();
        editor.putInt(Constants.USER_IGNORED_VERSION_CODE, code);
        editor.commit();
    }

    public int getIgnoredUpdateVersion() {
        SharedPreferences prefer = getMarketConfigPref();
        return prefer.getInt(Constants.USER_IGNORED_VERSION_CODE, 0);
    }
}
