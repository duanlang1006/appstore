package com.android.applite.plugin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.android.applite.model.IAppInfo;
import com.applite.util.AppLiteSpUtils;
import com.applite.util.AppliteConfig;
import com.umeng.analytics.MobclickAgent;

public class AppLiteEnter extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this,AppLitePlugin.class);
        intent.putExtra(AppliteConfig.KEY_PROJECT, IAppInfo.CatgoryYlzx);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
        
        MobclickAgent.onEvent(this, "ylzx");//友盟计数事件
        AppLiteSpUtils.setDataYLZX(this, AppLiteSpUtils.getDataYLZX(this) + 1);
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	MobclickAgent.onResume(this);
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	MobclickAgent.onPause(this);
    }
}
