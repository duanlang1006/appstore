package com.kapps.market;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kapps.market.analysi.AnalysiManager;
import com.kapps.market.log.LogUtil;
import com.kapps.market.task.MarketServiceWraper;
import com.kapps.market.task.mark.ATaskMark;
import com.kapps.market.task.mark.local.InitSoftwareSummaryTaskMark;
import com.kapps.market.ui.manage.AppManageActivity;
import com.kapps.market.ui.search.AppSearchActivity;
import com.kapps.market.util.Constants;
import com.feedback.NotificationType;

/**
 * 2010-6-11 閿熷彨绛规嫹閿熸枻鎷烽敓鏂ゆ嫹Activity閿熶茎闈╂嫹閿熸磥锛岄敓鏂ゆ嫹閿熸枻鎷穖arket閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷
 * 
 * @author admin
 * 
 */
public abstract class MarketActivity extends ActionBarActivity {

	public static final String TAG = "MarketActivity";
	// 棰勯敓楗虹鎷峰閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷峰�閿熸枻鎷烽敓鏂ゆ嫹閿熻娇鐚lse閿熼叺绛规嫹濮嬮敓鏂ゆ嫹
	// 閿熸枻鎷烽敓鏂ゆ嫹閿熷彨绛规嫹鍙敓鏂ゆ嫹濮嬮敓鏂ゆ嫹涓�敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熻鎾呮嫹鎬侀敓鏂ゆ嫹
	private static boolean preInit = false;
	// 閿熸枻鎷烽敓鎺ワ讣鎷烽湁閿熸枻鎷烽敓渚ヤ紮鎷烽敓鏂ゆ嫹
	protected MApplication marketContext;
	protected MarketServiceWraper serviceWraper;
	protected TaskMarkPool taskMarkPool;
	// 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷�
	private MarkableHandler mmHandler;

	public final int MENU_SEARCH = 0;
	public final int MENU_DOWN_MGR = 1;
	public final int MENU_LOGIN = 2;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		initCustomActionbar();
		
		
		marketContext = (MApplication) getApplication();
		serviceWraper = marketContext.getServiceWraper();
		taskMarkPool = marketContext.getTaskMarkPool();

		// 娉ㄩ敓缁撳閿熸枻鎷烽敓鏂ゆ嫹
		mmHandler = new MMHandler();
		marketContext.registerSubHandler(mmHandler);

		// 閿熸枻鎷烽敓鐭紮鎷烽敓鐨嗐倧鎷风樃閿熺粸纭锋嫹閿燂拷
		if (!preInit) {
			preInit = true;
			initMarketPreferData();
			LogUtil.d(TAG, "################## init activity preInit: " + preInit);
		}

		// 閿熸枻鎷烽敓鍓垮嚖鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹
		AnalysiManager.enableNewReplyNotification(this, NotificationType.AlertDialog);

	}

	/**
	 * 鎵ч敓鏂ゆ嫹涓�簺閿熷彨绛规嫹閿熸枻鎷疯閿熸枻鎷烽敓鏂ゆ嫹鑾搁敓鐨嗐倧鎷烽敓鏂ゆ嫹閿燂拷
	 * 
	 * @param quickView
	 *            閿熸枻鎷烽敓鏂ゆ嫹寮洪敓鏂ゆ嫹閿熸枻鎷峰浘閿熸枻鎷蜂箞閿熸枻鎷烽敓鏂ゆ嫹鎵ч敓鏂ゆ嫹閿熸枻鎷烽閿熸枻鎷峰綍閿熸枻鎷�
	 */
	// TODO cm 閿熸枻鎷峰閿熸枻鎷烽敓鍙鎷烽閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓锟�	
	private void initMarketPreferData() {
		// 閿熸枻鎷峰閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鍙唻鎷�
		ATaskMark taskMark = taskMarkPool.getInitDownloadTaskMark();
		serviceWraper.initDownloadTask(marketContext, taskMark);

		// 閿熸枻鎷峰閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷锋枩閿燂拷
		InitSoftwareSummaryTaskMark initSoftwareMark = taskMarkPool.getInitSoftwareSummaryTaskMark();
		serviceWraper.initSoftwareSummaryList(marketContext, initSoftwareMark);

		// 閿熸枻鎷峰閿熸枻鎷穉pk閿熷彨锟介敓鏂ゆ嫹閿熸枻鎷锋病閿熷彨鍖℃嫹閿熸枻鎷�
		// taskMark = taskMarkPool.getInitLocalApkSummaryTaskMark();
		// serviceWraper.initLocalApkSummaryInfoList(marketContext, taskMark,
		// null);

	}

	@Override
	protected void onResume() {
		super.onResume();
		// 閿熸枻鎷烽敓鍓胯揪鎷烽敓鏂ゆ嫹
		AnalysiManager.onResume(this);
        //add by shuizhu, complete correct??TODO
        setHandleAllMessage(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 閿熸枻鎷烽敓鍓胯揪鎷烽敓鏂ゆ嫹
		AnalysiManager.onPause(this);

        //add by shuizhu, complete correct??TODO
        setHandleAllMessage(false);
	}

	@Override
	protected void onDestroy() {
		if (mmHandler != null) {
			marketContext.unregisterSubHandler(mmHandler);
		}

		super.onDestroy();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		/*
		if (!NetworkinfoParser.isNetConnect(marketContext)) {
			menu.findItem(R.id.logoutMenuItem).setVisible(false);
			menu.findItem(R.id.marketUpdateMenuItem).setVisible(false);
			// 閿熸枻鎷烽敓鍓垮嚖鎷烽敓鏂ゆ嫹
			menu.findItem(R.id.feedbackMenuItem).setVisible(false);
		}
		*/

		// 閿熼ズ璇ф嫹閿熸枻鎷风ず閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷疯檹璇撮敓锟		
		//menu.findItem(R.id.softUpdateMenuItem).setVisible(false);

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.app_main_menu, menu);*/
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {           
        case MENU_SEARCH:{//search
			Intent intent = new Intent();
			intent.setClass(this, AppSearchActivity.class);
			startActivity(intent);
        	return true;
        }
        case MENU_DOWN_MGR:{//download mgr.  
        	Intent intent = new Intent();
			intent.setClass(this, AppManageActivity.class);
			startActivity(intent);
        	return true;
        }
        case MENU_LOGIN: {
    		Intent intent = new Intent();
    		if (marketContext.isSessionLocalValid()) {
    			intent.putExtra(Constants.LOGIN_INVOKE_STATE, Constants.VIEW_LOGIN);
    		}
			intent.setClass(this, AccountFrame.class);
			startActivity(intent);
        	return true;
        }
        case android.R.id.home:
            finish();
            return true; 
		case R.id.closeMenuItem:
			finish();
			break;
		case R.id.logoutMenuItem:
			doLogout();
			break;
		case R.id.configMenuItem:
			doConfig();
			break;
		case R.id.marketUpdateMenuItem:
			marketContext.checkMarketUpdate(true);
			break;
		case R.id.softUpdateMenuItem:
			marketContext.checkSoftwareUpdate(true);
			break;
		case R.id.aboutMenuItem:
			doShowAbout();
			break;
		case R.id.feedbackMenuItem:
			AnalysiManager.openUmengFeedbackSDK(this);
			break;
		default:
			break;
		}
		return true;
	}
	

	// shui: old logout method, trigger in menu, but not used now
	private void doLogout() {
		marketContext.initMarketContextForLogout();
		// 閿熸枻鎷峰綍閿熸枻鎷烽檰閿熸枻鎷烽敓鏂ゆ嫹
		Intent intent = new Intent(this, AccountFrame.class);
		intent.putExtra(Constants.LOGIN_INVOKE_STATE, Constants.RE_LOGIN);
		startActivity(intent);
		// 閿熸枻鎷烽敓鏂ゆ嫹
		finish();
	}

	// 閿熸枻鎷烽敓鏂ゆ嫹
	private void doConfig() {
		// 閿熸枻鎷峰綍閿熸枻鎷烽檰閿熸枻鎷烽敓鏂ゆ嫹
		Intent intent = new Intent(this, MConfigFrame.class);
		startActivity(intent);
	}

	// 閿熸枻鎷风ず閿熸枻鎷烽敓鏂ゆ嫹
	private void doShowAbout() {
		showDialog(R.id.aboutMenuItem);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		if (id == R.id.aboutMenuItem) {
			View view = getLayoutInflater().inflate(R.layout.about_view, null);
			TextView textView = (TextView) view.findViewById(R.id.versionLabel);
			textView.setText(getString(R.string.market_version_colon) + marketContext.getContextConfig().getVersion());
			dialog = new AlertDialog.Builder(this).setTitle(R.string.about_title).setView(view).create();
		}
		return dialog;
	}

	/**
	 * this is dangerous. shuizhu.
	 * 
	 */
	private void setHandleAllMessage(boolean all) {
		if (mmHandler != null) {
			mmHandler.setHandleAll(all);
		}
	}

	/**
	 * 閿熸枻鎷烽敓鐭揪鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓瑙掑嚖鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿燂拷
	 * 
	 */
	protected void setHandleMessageMark(int megMark) {
		if (mmHandler != null) {
			mmHandler.setMessageMark(megMark);
		}
	}

	/**
	 * 閿熸枻鎷烽敓鏂ゆ嫹閿熸帴纭锋嫹閿熸枻鎷锋伅
	 * 
	 * @param msg
	 */
	public void subHandleMessage(Message msg) {

	}

	// 閿熸枻鎷烽敓鏂ゆ嫹閿熻姤澶勯敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹甯岄敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹璇橀敓缁炴唻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿岄敓鏂ゆ嫹鑹块敓鏂ゆ嫹閿熻緝顫嫹閿燂拷
	private class MMHandler extends MarkableHandler {

		/**
		 * @param handlerMark
		 */
		public MMHandler() {
			super();

		}

		@Override
		public void handleMessage(Message msg) {
			// 閿熸枻鎷烽敓鏂ゆ嫹涓敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熺禎inish閿熷壙鈽呮嫹閿熸触涓嶈揪鎷烽敓鏂ゆ嫹
			if (isFinishing()) {
				return;
			}
			switch (msg.what) {
			default:
				subHandleMessage(msg);
				break;
			}
		}
	}

	protected abstract void initCustomActionbar() ;
}
