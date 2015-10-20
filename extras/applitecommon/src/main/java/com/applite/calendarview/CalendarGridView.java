package com.applite.calendarview;

import android.app.Activity;
import android.content.Context;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class CalendarGridView extends GridView implements View.OnTouchListener {

    private DisplayMetrics dm;
    private Activity mActivity;
    private Calendar mCalendar;
    private Time mTime = new Time();
    private Time mTempTime = new Time();
    private Time mSelectedDay = new Time();
    private int mFirstDayOfWeek;
    private static final int mNumWeeks = 6;
    /**
     * true周日为第一天    false周一为第一天
     */
    private static boolean FIRST_DAY = true;
    private GestureDetector mGestureDetector;
    private int mPosition;
    private HashMap<String, Integer> mDrawingParams = new HashMap<String, Integer>();
    private ICallback mCallback;
    private int mMonthInterval = 0;

    public CalendarGridView(Context context) {
        super(context);
        initCalendar(context);
    }

    public CalendarGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCalendar(context);
    }

    public CalendarGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initCalendar(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        SimpleWeeksAdapter mSimpleWeeksAdapter = new SimpleWeeksAdapter(mActivity,
                mDrawingParams, mPosition, mFirstDayOfWeek);
        this.setAdapter(mSimpleWeeksAdapter);
    }

    protected class CalendarGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            return true; // 禁止GridView滑动
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            int po = ((AbsListView) v).pointToPosition((int) event.getX(), (int) event.getY());
            if (po != GridView.INVALID_POSITION) {
                for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
                    ((SimpleWeekView) ((ViewGroup) v).getChildAt(i)).setOnClickSelf(false);
                }
                SimpleWeekView view = (SimpleWeekView) ((ViewGroup) v).getChildAt(po);
                Time day = view.getDayFromLocation(event.getX());
                if (day != null) {
                    if (Utils.mFocusDay) {//当前月的日期
                        view.setOnClickSelf(true);
                        mSelectedDay.set(day);
//                        day.hour = mSelectedDay.hour;
//                        day.minute = mSelectedDay.minute;
//                        day.second = mSelectedDay.second;
                        setDate();
                        ((SimpleWeeksAdapter) getAdapter()).setData(mDrawingParams, mPosition, mFirstDayOfWeek);
                        mCallback.OnClickDate(day);
                    } else {
                        mSelectedDay.set(day);
                        setMonthInterval(day);
                        setSelectedDay();
                        setDate();
                        ((SimpleWeeksAdapter) getAdapter()).setData(mDrawingParams, mPosition, mFirstDayOfWeek);
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 设置回调listener
     *
     * @param iCallback
     */
    public void setCallback(ICallback iCallback) {
        this.mCallback = iCallback;
    }

    private void initCalendar(Context context) {
        setOnTouchListener(this);
        mActivity = (Activity) context;
        dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mGestureDetector = new GestureDetector(mActivity, new CalendarGestureListener());

        String tz = Time.getCurrentTimezone();
        mTempTime.timezone = tz;
        mTempTime.setToNow();
        mSelectedDay.switchTimezone(mTempTime.timezone);
        mSelectedDay.setToNow();

        setSelectedDay();
        setFirstDayOfWeek(FIRST_DAY);
        setDate();
    }

    private void setSelectedDay() {
        int day = mSelectedDay.monthDay;
        mCalendar = Utils.getSelectCalendar(Utils.PAGER_CURRENTITEM + mMonthInterval);
        if (mCalendar.get(Calendar.YEAR) >= 2038) {
            mTime.set(1, 11, 2037);
            mCalendar.setTimeInMillis(mTime.normalize(true));
        } else if (mCalendar.get(Calendar.YEAR) < 1970) {
            mTime.set(1, 0, 1970);
            mCalendar.setTimeInMillis(mTime.normalize(true));
        } else {
            mTime.set(mCalendar.getTimeInMillis());
            mTime.normalize(true);
        }
        mSelectedDay.set(mCalendar.getTimeInMillis());
        if (day > mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            day = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        mSelectedDay.monthDay = day;
        mSelectedDay.normalize(true);
    }

    private void setDate() {
        setDdrawingParams();
        setPosition();
    }

    /**
     * 设置点击时间和选中时间的月份间隔
     *
     * @param time
     * @return
     */
    private void setMonthInterval(Time time) {
        Calendar cal = Utils.getSelectCalendar(Utils.PAGER_CURRENTITEM + mMonthInterval);
        if (cal.get(Calendar.YEAR) < time.year) {
            mMonthInterval = mMonthInterval + 1;
            mCallback.ToNextMonth();
        } else if (cal.get(Calendar.YEAR) > time.year) {
            mMonthInterval = mMonthInterval - 1;
            mCallback.ToLastMonth();
        } else {
            if (cal.get(Calendar.MONTH) < time.month) {
                mMonthInterval = mMonthInterval + 1;
                mCallback.ToNextMonth();
            } else if (cal.get(Calendar.MONTH) > time.month) {
                mMonthInterval = mMonthInterval - 1;
                mCallback.ToLastMonth();
            }
        }
    }

    /**
     * 设置周首日
     */
    public void setFirstDayOfWeek(boolean bool) {
        FIRST_DAY = bool;
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        if (FIRST_DAY) {
            cal.setFirstDayOfWeek(Calendar.SUNDAY);
            mFirstDayOfWeek = cal.getFirstDayOfWeek() - 1;
        } else {
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            mFirstDayOfWeek = cal.getFirstDayOfWeek() - 1;
        }
    }

    /**
     * 得到周首日
     */
    public int getFirstDayOfWeek() {
        return mFirstDayOfWeek;
    }

    private void setPosition() {
        mPosition = Time.getWeeksSinceEpochFromJulianDay(Time.getJulianDay(mTime.toMillis(true), mTime.gmtoff), mFirstDayOfWeek);
    }

    public int getPosition() {
        return mPosition;
    }

    private void setDdrawingParams() {
        mDrawingParams.clear();
        //每一个SimpleWeekView的高度
        mDrawingParams.put(SimpleWeekView.VIEW_PARAMS_HEIGHT, (dm.widthPixels + 6) / mNumWeeks);
        //有几个SimpleWeekView
        mDrawingParams.put(SimpleWeeksAdapter.WEEK_PARAMS_NUM_WEEKS, mNumWeeks);
        //周首日
        mDrawingParams.put(SimpleWeeksAdapter.WEEK_PARAMS_WEEK_START, mFirstDayOfWeek);
        //月视图显示的是哪个月
        mDrawingParams.put(SimpleWeekView.VIEW_PARAMS_FOCUS_MONTH, mTime.month);
        //选定日期
        mDrawingParams.put(SimpleWeeksAdapter.WEEK_PARAMS_JULIAN_DAY, Time.getJulianDay(mSelectedDay.toMillis(true), mSelectedDay.gmtoff));
    }

    public HashMap<String, Integer> getDdrawingParams() {
        return mDrawingParams;
    }

}
