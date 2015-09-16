package com.applite.homepage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applite.common.LogUtils;
import com.applite.sharedpreferences.AppliteSPUtils;
import com.mit.mitupdatesdk.MitMobclickAgent;
import com.osgi.extra.OSGIBaseFragment;

public class LuckyFragment extends OSGIBaseFragment implements LuckyPanView.CallBackInterface {
    private final String TAG = "LuckyFragment";

    private LuckyPanView mLuckyPanView;
    private ImageView mStartBtn;
    private Button mRules;
    private TextView mPoints;
    private Dialog alertDialog;
    private int mLuckyPonints;

    private String mRulesStr;

    private Toast toast;

    private int Conunts;

    public LuckyFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Conunts = (int) AppliteSPUtils.get(mActivity, AppliteSPUtils.CURRENT_TIMES, 0);
        String da = (String) AppliteSPUtils.get(mActivity, AppliteSPUtils.CURRENT_DATE, "0");

        String currdata = getDate();

        if (!currdata.equals(da)) {
            Conunts = 0;
            AppliteSPUtils.put(mActivity, AppliteSPUtils.CURRENT_TIMES, Conunts);
            AppliteSPUtils.put(mActivity, AppliteSPUtils.CURRENT_DATE, currdata);
        }

        mLuckyPonints = (int) AppliteSPUtils.get(mActivity, AppliteSPUtils.LUCKY_POINTS, 1000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_lucky_main, container, false);

        mLuckyPanView = (LuckyPanView) view.findViewById(R.id.id_luckypan);
        mLuckyPanView.changeCallBack(this);

        mStartBtn = (ImageView) view.findViewById(R.id.id_start_btn);

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mLuckyPanView.isStart()) {
                    mLuckyPonints = (int) AppliteSPUtils.get(mActivity, AppliteSPUtils.LUCKY_POINTS, 1000);
                    LogUtils.i(TAG, "Conunts = " + Conunts);
                    if (mLuckyPonints < 20) {
                        if (toast == null) {
                            toast = Toast.makeText(mActivity, getString(R.string.IntegralProblem), Toast.LENGTH_SHORT);
                        }
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else if (Conunts > 20) {
                        //判断当天抽奖次数，如果已满3次，则弹出提示当天不允许再抽奖
                        if (toast == null) {
                            toast = Toast.makeText(mActivity, getString(R.string.beyond_limit), Toast.LENGTH_SHORT);
                        }
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else {
                        Conunts++;

                        //读取当前积分，减去当前抽奖花费的积分
                        mLuckyPonints = MitMobclickAgent.calDrawPoints(mLuckyPonints, "choujiang");
                        mPoints.setText("积分：" + String.valueOf(mLuckyPonints));
                        AppliteSPUtils.put(mActivity, AppliteSPUtils.LUCKY_POINTS, mLuckyPonints);

                        //重新绘制界面
                        mStartBtn.setImageResource(R.drawable.stop);
                        mLuckyPanView.luckyStart(0);
//                        mLuckyPanView.luckyStart(); //随机中奖概率
                        mLuckyPanView.setflag(true);
                    }
                } else {
                    if (!mLuckyPanView.isShouldEnd()) {
                        mStartBtn.setImageResource(R.drawable.start);
                        mLuckyPanView.luckyEnd();
                    }
                }
            }
        });

        getRulesStr();

        alertDialog = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.rules_title)
                .setMessage(mRulesStr)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogUtils.i(TAG, "click rules dialog ");
                    }
                })
                .create();

        mRules = (Button) view.findViewById(R.id.rules);
        mRules.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                alertDialog.show();
            }
        });

        mPoints = (TextView) view.findViewById(R.id.points);
        mPoints.setText("积分：" + String.valueOf(mLuckyPonints));

        initActionBar();

        return view;
    }

    private String getDate() {
        Time t = new Time();
        t.setToNow();
        int lastmonth = t.month + 1;
        int date = t.year + lastmonth + t.monthDay;
        //String str =  t.year + "年" + lastmonth + "月" + t.monthDay + "日" + t.hour + ":"+ t.minute + ":"+ t.second;
        String str = t.year + "_" + lastmonth + "_" + t.monthDay;
        LogUtils.i(TAG, "str = " + str);
        return str;
    }

    private void getRulesStr() {
        String rules_no1 = getString(R.string.rules_no1);
        String rules_no2 = getString(R.string.rules_no2);
        String rules_no3 = getString(R.string.rules_no3);
        String rules_no4 = getString(R.string.rules_no4);
        String rules_no5 = getString(R.string.rules_no5);
        String rules_no6 = getString(R.string.rules_no6);
        String rules_no7 = getString(R.string.rules_no7);
        String rules_else = getString(R.string.rules_else);
        String rules_end = getString(R.string.rules_end);

        mRulesStr = rules_no1 + "\n" + rules_no2 + "\n" + rules_no3 + "\n" + rules_no4 + "\n" + rules_no5 + "\n" + rules_no6 + "\n" + rules_no7 + "\n\n\n" + rules_else + "\n\n" + rules_end;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_search);
        if (null != item) {
            item.setVisible(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        AppliteSPUtils.put(mActivity, AppliteSPUtils.CURRENT_TIMES, Conunts);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initActionBar() {
        try {
            ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mActivity.getResources().getString(R.string.lucky_draw));
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.removeAllTabs();
            actionBar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void changePointsString() {
        mLuckyPonints = (int) AppliteSPUtils.get(mActivity, AppliteSPUtils.LUCKY_POINTS, 0);
        if (mPoints != null) {
            mPoints.setText("积分：" + String.valueOf(mLuckyPonints));
        }
    }

}
