package com.applite.dm.main;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.applite.common.AppliteUtils;
import com.applite.common.Constant;
import com.applite.common.LogUtils;
import com.applite.common.VibratorUtil;
import com.applite.dm.adapter.DownloadAdapter;
import com.applite.dm.adapter.DownloadSimilarAdapter;
import com.applite.dm.R;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.applite.similarview.SimilarAdapter;
import com.applite.similarview.SimilarBean;
import com.applite.similarview.SimilarView;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.mit.impl.ImplAgent;
import com.mit.impl.ImplInfo;
import com.mit.impl.ImplLog;
import com.osgi.extra.OSGIBaseFragment;
import com.osgi.extra.OSGIServiceHost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DownloadListFragment extends OSGIBaseFragment implements DownloadPagerFragment.IDownloadOperator,
        ListView.OnItemClickListener, AdapterView.OnItemLongClickListener,
        View.OnClickListener, SimilarAdapter.SimilarAPKDetailListener {
    final static String TAG = "applite_dm";
    private ListView mListview;
    private DownloadAdapter mAdapter;
    private boolean[] status = null;//这里存放checkBox的选中状态
    private int mStatusFlags;
    private int mTitleId;
    private ImplAgent mImplAgent;
    private List<ImplInfo> mImplList;

    private BitmapUtils mBitmapHelper;
    private SimilarView mSimilarView;
    private List<SimilarBean> mSimilarDataList;
    private SimilarAdapter mSimilarAdapter;

    private HttpUtils mHttpUtils;
    private boolean checkBoxAnima = true;
    private int temp = 0;

    private String COUNT_DOWNLOADING = "count downloading";
    private String COUNT_DOWNLOADED = "count downloaded";
    private String FLAG = "flag";
    private String POSITION = "position";

//    /**
//     * Notification构造器
//     */
//    private NotificationCompat.Builder mBuilder;
//    /**
//     * Notification的ID
//     */
////    int notifyId = 100;
//    int notifyId0 = 100;
//    int notifyId1 = 101;
//    /**
//     * Notification管理
//     */
//    public NotificationManager mNotificationManager;

    private DownloadListener mDownloadListener = new DownloadListener() {
        @Override
        public boolean getFlag1() {
            return (boolean) AppliteSPUtils.get(mActivity, FLAG, false);
        }

        @Override
        public boolean getStatus(int position) {
            if (position < 0 || position >= status.length) {
                return false;
            }
            return status[position];
        }

        @Override
        public int getTitleId() {
            return mTitleId;
        }

        @Override
        public boolean getFlag2() {
            return checkBoxAnima;
        }

        @Override
        public void setFlag2(boolean b) {
            checkBoxAnima = b;
        }
    };


    public static final Comparator<ImplInfo> IMPL_TIMESTAMP_COMPARATOR = new Comparator<ImplInfo>() {
        public final int compare(ImplInfo a, ImplInfo b) {
            int result = 0;
            if (a.getLastMod() < b.getLastMod()) {
                result = 1;
            } else if (a.getLastMod() > b.getLastMod()) {
                result = -1;
            }
            return result;
        }
    };


    public static Bundle newBundle(int resid, int flag) {
        Bundle b = new Bundle();
        b.putInt("titleId", resid);
        b.putInt("statusFilter", flag);
        return b;
    }

    public DownloadListFragment() {
        super();
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mListListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(FLAG)) {
                if ((boolean) AppliteSPUtils.get(mActivity, FLAG, false)) {
                    checkBoxAnima = false;
                }
            } else if (key.equals(POSITION)) {
                if (R.string.dm_downloading == (int) AppliteSPUtils.get(mActivity, POSITION, -1)) {
                    if ((boolean) AppliteSPUtils.get(mActivity, FLAG, false) && View.VISIBLE == mSimilarView.getVisibility()) {
                        mSimilarView.setVisibility(View.GONE);
                        mSimilarView.setPadding(0, -mSimilarView.getHeight(), 0, 0);
                    } else if (!(boolean) AppliteSPUtils.get(mActivity, FLAG, false) && View.GONE == mSimilarView.getVisibility()) {
                        mSimilarView.setVisibility(View.VISIBLE);
                        mSimilarView.setPadding(0, 1, 0, 0);
                    }
                }
            }
//            else if (key.equals(COUNT_DOWNLOADING)) {
//                    showIntentActivityNotify();//这里是显示 点击返回的提示
//            } else if (key.equals(COUNT_DOWNLOADED)) {
//                    showIntentApkNotify();//这里是显示 点击安装的提示
//            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle params = getArguments();
        if (null != params) {
            mStatusFlags = params.getInt("statusFilter");
            mTitleId = params.getInt("titleId");
        }
        mImplAgent = ImplAgent.getInstance(activity.getApplicationContext());
        mImplList = mImplAgent.getDownloadInfoList(mStatusFlags);
        mBitmapHelper = new BitmapUtils(mActivity.getApplicationContext());
        ImplLog.d(DownloadListFragment.TAG, "onAttach," + this + "," + mImplList.size());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SharePreference初始化
        AppliteSPUtils.registerChangeListener(mActivity, mListListener);
        count(0);//当前页选中项目数
        AppliteSPUtils.put(mActivity, FLAG, false);
//        initNotify();//通知栏提示初始化
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImplLog.d(TAG, "onCreateView," + this);
        LayoutInflater mInflater = inflater;
        View view = mInflater.inflate(R.layout.fragment_download_list, container, false);
        mListview = (ListView) view.findViewById(android.R.id.list);
        TextView emptyText = (TextView) view.findViewById(R.id.empty);
        mListview.setEmptyView(emptyText);
        if (mTitleId == R.string.dm_downloading) {
            initSimilarView(view);
            mListview.addFooterView(mSimilarView);
//            emptyText.setText(mActivity.getResources().getString(R.string.back_to_homepage));
//            emptyText.setEnabled(true);
//            Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
//            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
//            emptyText.setCompoundDrawables(null, drawable, null, null);
//            emptyText.setOnClickListener(this);
        }
        mListview.setOnItemClickListener(this);
        status = new boolean[mImplList.size()];
        Arrays.fill(status, false);//全部填充为false(chechbox不选中)
        mListview.setAdapter(mAdapter);

        //这里是长按删除
        mListview.setOnItemLongClickListener(this);
        mListview.setOnScrollListener(new PauseOnScrollListener(mBitmapHelper, false, true));
        if (null != mImplList && mImplList.size() > 0) {
            mAdapter = new DownloadAdapter(mActivity, R.layout.download_list_item,
                    mImplList, mBitmapHelper, mDownloadListener);
            mAdapter.sort(IMPL_TIMESTAMP_COMPARATOR);
            mListview.setAdapter(mAdapter);
        }

        return view;
    }

//    /**
//     * 初始化通知栏
//     */
//    private void initNotify() {
//        LogUtils.d("wanghc", "我执行了initNotify");
//        mBuilder = new NotificationCompat.Builder(mActivity);
//        mBuilder.setContentTitle("测试标题")
//                .setContentText("测试内容")
//                .setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL))
////				.setNumber(number)//显示数量
//                .setTicker("测试通知来啦")//通知首次出现在通知栏，带上升动画效果的
//                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
//                .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
////				.setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
//                .setOngoing(false)//ture，设置他为一个正在进行的通知。
//                        // 他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
////                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，
//                        // 使用defaults属性，可以组合：
//                        //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
//                .setSmallIcon(R.drawable.ic_launcher);
//        mNotificationManager = (NotificationManager) mActivity.getSystemService(mActivity.NOTIFICATION_SERVICE);
//    }
//
//    /**
//     * @获取默认的pendingIntent,为了防止2.3及以下版本报错
//     * @flags属性: 在顶部常驻:Notification.FLAG_ONGOING_EVENT
//     * 点击去除： Notification.FLAG_AUTO_CANCEL
//     */
//    public PendingIntent getDefalutIntent(int flags) {
//        LogUtils.d("wanghc", "我执行了getDefalutIntent");
//        PendingIntent pendingIntent = PendingIntent.getActivity(mActivity, 1, new Intent(), flags);
//        return pendingIntent;
//    }
//
////    /** 显示通知栏 */
////    public void showNotify(){
////        mBuilder.setContentTitle("测试标题")
////                .setContentText("测试内容")
//////				.setNumber(number)//显示数量
////                .setTicker("测试通知来啦");//通知首次出现在通知栏，带上升动画效果的
////        mNotificationManager.notify(notifyId, mBuilder.build());
//////		mNotification.notify(getResources().getString(R.string.app_name), notiId, mBuilder.build());
////    }
//
//    /**
//     * 显示通知栏点击跳转到指定Activity
//     */
//    public void showIntentActivityNotify() {
//        LogUtils.d("wanghc", "我执行了showIntentActivityNotify");
//        // Notification.FLAG_ONGOING_EVENT --设置常驻 Flag;Notification.FLAG_AUTO_CANCEL 通知栏上点击此通知后自动清除此通知
////		notification.flags = Notification.FLAG_AUTO_CANCEL; //在通知栏上点击此通知后自动清除此通知
//        mBuilder.setAutoCancel(true)//点击后让通知将消失
//                .setContentTitle("您有" + mImplList.size() + "个应用正在下载")
//                .setContentText("点击查看");
////                .setTicker("点我");
//        //点击的意图ACTION是跳转到Intent
////        Intent resultIntent = new Intent(this, MainActivity.class);
//        Intent resultIntent = new Intent();
//        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mActivity, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mBuilder.setContentIntent(pendingIntent);
//        mNotificationManager.notify(notifyId0, mBuilder.build());
//    }
//
//    /**
//     * 显示通知栏点击打开Apk
//     */
//    public void showIntentApkNotify() {
//        LogUtils.d("wanghc", "我执行了showIntentApkNotify");
//        // Notification.FLAG_ONGOING_EVENT --设置常驻 Flag;Notification.FLAG_AUTO_CANCEL 通知栏上点击此通知后自动清除此通知
////		notification.flags = Notification.FLAG_AUTO_CANCEL; //在通知栏上点击此通知后自动清除此通知
//        mBuilder.setAutoCancel(true)//点击后让通知将消失
//                .setContentTitle("您有" + mImplList.size() + "个应用下载完成")
//                .setContentText("点击安装");
////                .setTicker("下载完成！");
//        //我们这里需要做的是打开一个安装包
//        Intent apkIntent = new Intent();
//        apkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        apkIntent.setAction(android.content.Intent.ACTION_VIEW);
//        //注意：这里的这个APK是放在assets文件夹下，获取路径不能直接读取的，要通过COYP出去在读或者直接读取自己本地的PATH，这边只是做一个跳转APK，实际打不开的
//        String apk_path = "file:///android_asset/cs.apk";
////		Uri uri = Uri.parse(apk_path);
//        Uri uri = Uri.fromFile(new File(apk_path));
//        apkIntent.setDataAndType(uri, "application/vnd.android.package-archive");
//        // context.startActivity(intent);
//        PendingIntent contextIntent = PendingIntent.getActivity(mActivity, 0, apkIntent, 0);
//        mBuilder.setContentIntent(contextIntent);
//        mNotificationManager.notify(notifyId1, mBuilder.build());
//    }


    //当前页选中项目数
    private void count(int number) {
        if (mTitleId == R.string.dm_downloading) {
            AppliteSPUtils.put(mActivity, COUNT_DOWNLOADING, number);
        } else if (mTitleId == R.string.dm_downloaded) {
            AppliteSPUtils.put(mActivity, COUNT_DOWNLOADED, number);
        }
    }

    private int count() {
        if (mTitleId == R.string.dm_downloading) {
            return (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADING, 0);
        } else if (mTitleId == R.string.dm_downloaded) {
            return (int) AppliteSPUtils.get(mActivity, COUNT_DOWNLOADED, 0);
        } else {
            return 0;
        }
    }

    private void initSimilarView(View view) {
        mSimilarView = (SimilarView) view.inflate(mActivity, R.layout.similar_view, null);
        TextView t = (TextView) mSimilarView.findViewById(R.id.similar_title);
//        t.setText("大家还下载了");
        t.setVisibility(View.GONE);
        mSimilarDataList = new ArrayList<>();
        mHttpUtils = new HttpUtils();
        RequestParams params = new RequestParams();
        params.addBodyParameter("appkey", AppliteUtils.getMitMetaDataValue(mActivity, Constant.META_DATA_MIT));
        params.addBodyParameter("packagename", mActivity.getPackageName());
        params.addBodyParameter("type", "update_management");
        params.addBodyParameter("protocol_version", Constant.PROTOCOL_VERSION);
        params.addBodyParameter("update_info", AppliteUtils.getAllApkData(mActivity));
        mHttpUtils.send(HttpRequest.HttpMethod.POST, Constant.URL, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                LogUtils.i(TAG, "更新请求成功，resulit：" + responseInfo.result);
                try {
                    JSONObject object = new JSONObject(responseInfo.result);
                    String similar_info = object.getString("similar_info");
                    if (!mSimilarDataList.isEmpty())
                        mSimilarDataList.clear();
                    JSONArray similar_json = new JSONArray(similar_info);
                    SimilarBean similarBean = null;
                    if (similar_json.length() != 0 && similar_json != null) {
                        for (int i = 0; i < 4; i++) {
                            similarBean = new SimilarBean();
                            JSONObject obj = new JSONObject(similar_json.get(i).toString());
                            similarBean.setName(obj.getString("name"));
                            similarBean.setPackageName(obj.getString("packageName"));
                            similarBean.setIconUrl(obj.getString("iconUrl"));
                            similarBean.setrDownloadUrl(obj.getString("rDownloadUrl"));
                            similarBean.setVersionCode(obj.getInt("versionCode"));
                            mSimilarDataList.add(similarBean);
                        }
                        if (null == mSimilarAdapter) {
                            mSimilarAdapter = new DownloadSimilarAdapter(mActivity);
                            mSimilarAdapter.setData(mSimilarDataList, DownloadListFragment.this);
                            mSimilarView.setAdapter(mSimilarAdapter);
                        } else {
                            mSimilarAdapter.setData(mSimilarDataList, DownloadListFragment.this);
                            mSimilarAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(mActivity, "kong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                LogUtils.i(TAG, mActivity.getPackageName() + "");
            }

        });
        mSimilarView.setVisibility(View.VISIBLE);
        mSimilarView.setPadding(0, 1, 0, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    if ((boolean) AppliteSPUtils.get(mActivity, FLAG, false)) {
                        reSet();
                        AppliteSPUtils.put(mActivity, FLAG, false);
                        return true;
                    }
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ImplLog.d(DownloadListFragment.TAG, "onDetach," + this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ImplLog.d(DownloadListFragment.TAG, "onDestroyView," + this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListview.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void onClick(View v) {

//        getFragmentManager().popBackStack();
//        if () {
//        ((OSGIServiceHost) mActivity).jumpto(Constant.OSGI_SERVICE_MAIN_FRAGMENT, null, null, false);
//        }
//        Toast.makeText(mActivity, "我点了返回", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DownloadAdapter.ViewHolder vh = (DownloadAdapter.ViewHolder) view.getTag();
        if (!(boolean) AppliteSPUtils.get(mActivity, FLAG, false)) {
            if (null != vh) {
                ((OSGIServiceHost) mActivity).jumptoDetail(
                        vh.implInfo.getPackageName(),
                        vh.implInfo.getTitle(),
                        vh.implInfo.getIconUrl(),
                        vh.implInfo.getVersionCode(),
                        null,
                        true);
            }
        } else {
            status[position] = !status[position];
            temp = count();
            count((status[position] == false) ? temp - 1 : temp + 1);//当前页选中项目数
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (!(boolean) AppliteSPUtils.get(mActivity, FLAG, false)) {
            AppliteSPUtils.put(mActivity, FLAG, true);
            if (checkBoxAnima) {
                VibratorUtil.Vibrate(mActivity, 200);   //震动200ms
            }
            if (R.string.dm_downloading == mTitleId) {
                mSimilarView.setVisibility(View.GONE);
                mSimilarView.setPadding(0, -mSimilarView.getHeight(), 0, 0);

            }
            mAdapter.notifyDataSetChanged();
        }
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    private void reSet() {
        if (null != mSimilarView && View.VISIBLE != mSimilarView.getVisibility()) {
            mSimilarView.setVisibility(View.VISIBLE);
            mSimilarView.setPadding(0, 1, 0, 0);
        }
        Arrays.fill(status, false);//status数组复位
        checkBoxAnima = true;
        count(0);//当前页选中项目数
        AppliteSPUtils.put(mActivity, POSITION, 0);
        if (null != mAdapter) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void deleteItem() {
        List<Long> tempList = new ArrayList<>();
        for (int i = status.length - 1; i >= 0; i--) {
            if (status[i]) {
                tempList.add(mImplList.get(i).getId());
            }
        }
        mImplAgent.remove(tempList);
        count(0);
    }


    @Override
    public void refreshDetail(SimilarBean similarBean) {
        ((OSGIServiceHost) mActivity).jumptoDetail(similarBean.getPackageName(), similarBean.getName(), similarBean.getIconUrl(), similarBean.getVersionCode(), null, true);
    }

    @Override
    public void onClickDelete() {
        deleteItem();
        reSet();
    }

    @Override
    public void onClickSeleteAll() {
        Arrays.fill(status, true);
        count(mImplList.size());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClickDeselectAll() {
        Arrays.fill(status, false);
        count(0);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void resetFlag() {
        reSet();
        AppliteSPUtils.put(mActivity, FLAG, false);
    }

    //获取本页下载项的个数
    @Override
    public int getLength() {
        return status.length;
    }

    //ListFragment和适配器传递数据
    public interface DownloadListener {
        boolean getFlag1();

        boolean getStatus(int position);

        int getTitleId();

        boolean getFlag2();

        void setFlag2(boolean b);
    }


}