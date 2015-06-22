package com.kapps.market.ui.search;

import java.text.DecimalFormat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kapps.market.MApplication;
import com.kapps.market.MarketActivity;
import com.kapps.market.R;
import com.kapps.market.bean.BaseApp;
import com.kapps.market.util.Constants;

/**
 * 2010-6-11 market 锟斤拷锟斤拷锟斤拷
 * 
 * @author admin
 * 
 */
public class AppSearchActivity extends MarketActivity {
	public static final String TAG = "AppSearchActivity";

	protected MApplication marketContext = MApplication.getInstance();
	private EditText   searchedit;
	private ImageButton searchButton;
    AppSearchPage appSearch;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appsearch_activity);

		searchedit = (EditText) findViewById(R.id.keyWordEditor);
		searchButton = (ImageButton) findViewById(R.id.searchImageButton);

		appSearch = new AppSearchPage(this, (EditText) searchedit);
		searchButton.setOnClickListener(appSearch);
		
		FrameLayout contentFrame = (FrameLayout) findViewById(R.id.contentFrame);
		contentFrame.addView(appSearch);
	}

	@Override
	protected void initCustomActionbar() {
		ActionBar actionBar = getSupportActionBar();
		//设置标题  
        actionBar.setTitle(R.string.search);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);    
    }

	public void HandlePurchase()
	{
		BaseApp app=marketContext.mApp;
		DecimalFormat df=new DecimalFormat("#.##");
		String price=df.format(app.getPrice());
	
		Intent intent = new Intent();
		intent.setAction("com.ehoo.paysdk.MAIN");
		Bundle bundle = new Bundle();
	//	bundle.putString("merId", "1782");//锟斤拷锟斤拷锟斤拷袒锟斤拷锟斤拷
		bundle.putString("appKey", "1001");//锟斤拷要锟斤拷锟斤拷支锟斤拷锟斤拷应锟矫筹拷锟斤拷锟斤拷
		bundle.putString("amount","1.00" );//锟斤拷要支锟斤拷锟侥斤拷锟�
		bundle.putString("chargePoint", "01");//支锟斤拷锟斤拷
		intent.putExtras(bundle);
		startActivityForResult(intent, 8888); 
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
	
		if(resultCode == 8888) {
			Bundle bundle = data.getExtras();
			String orderId = bundle.getString("orderId");
			String result = bundle.getString("resultCode");
	
			if(result.equals("0000"))
			{
				Toast.makeText(this, getString(R.string.paysuccess_colon)+orderId, Toast.LENGTH_SHORT).show();
				Message message = Message.obtain();
				message.what = Constants.M_DOWNLOAD_AFTER_PAY_SUCCESS;
				marketContext.handleMarketMessage(message);
			}
			else
			{
				Toast.makeText(this, getString(R.string.payfail), Toast.LENGTH_SHORT).show();
				Message message = Message.obtain();
				message.what = Constants.M_DOWNLOAD_AFTER_PAY_FAIL;
				marketContext.handleMarketMessage(message);
			}
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        appSearch.flushView(1);
    }
}
