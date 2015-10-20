/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.applite.calendarview;

// TODO Remove calendar imports when the required methods have been
// refactored into the public api

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;

import java.util.HashMap;

/**
 * <p>
 * This is a specialized adapter for creating a list of weeks with selectable
 * days. It can be configured to display the week number, start the week on a
 * given day, show a reduced number of days, or display an arbitrary number of
 * weeks at a time. See {@link } for usage.
 * </p>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SimpleWeeksAdapter extends BaseAdapter {

    private static final String TAG = "MonthByWeek";

    /**
     * The number of weeks to display at a time.
     */
    public static final String WEEK_PARAMS_NUM_WEEKS = "num_weeks";
    /**
     * Which month should be in focus currently.
     */
    public static final String WEEK_PARAMS_FOCUS_MONTH = "focus_month";
    /**
     * Whether the week number should be shown. Non-zero to show them.
     */
    public static final String WEEK_PARAMS_SHOW_WEEK = "week_numbers";
    /**
     * Which day the week should start on. {@link Time#SUNDAY} through
     * {@link Time#SATURDAY}.
     */
    public static final String WEEK_PARAMS_WEEK_START = "week_start";
    /**
     * The Julian day to highlight as selected.
     */
    public static final String WEEK_PARAMS_JULIAN_DAY = "selected_day";
    /**
     * How many days of the week to display [1-7].
     */
    public static final String WEEK_PARAMS_DAYS_PER_WEEK = "days_per_week";

    protected static final int WEEK_COUNT = 3497 - 0;
    protected static int DEFAULT_NUM_WEEKS = 6;
    protected static int DEFAULT_MONTH_FOCUS = 0;
    protected static int DEFAULT_DAYS_PER_WEEK = 7;
    protected static int DEFAULT_WEEK_HEIGHT = 32;
    protected static int WEEK_7_OVERHANG_HEIGHT = 7;

    protected static float mScale = 0;
    protected Context mContext;
    // The day to highlight as selected
    protected Time mSelectedDay;
    // The week since 1970 that the selected day is in
    protected int mSelectedWeek;
    // When the week starts; numbered like Time.<WEEKDAY> (e.g. SUNDAY=0).
    protected int mFirstDayOfWeek;
    protected boolean mShowWeekNumber = false;
    protected int mNumWeeks = DEFAULT_NUM_WEEKS;
    protected int mDaysPerWeek = DEFAULT_DAYS_PER_WEEK;
    protected int mFocusMonth = DEFAULT_MONTH_FOCUS;
    private int mPosition;
    protected int mViewHeight = 0;
    //    private List<String> mEventDate;
    private static final boolean DEBUG = false;

    public SimpleWeeksAdapter(Context context, HashMap<String, Integer> params, int position, int mFirstDayOfWeek/*, List<String> events*/) {
        mContext = context;
//        this.mEventDate = events;
        this.mFirstDayOfWeek = mFirstDayOfWeek;
        //自1970年到现在多少周（eg:2015.1.1-2348）
        mPosition = position;
        if (mScale == 0) {
            mScale = context.getResources().getDisplayMetrics().density;
            if (mScale != 1) {
                WEEK_7_OVERHANG_HEIGHT *= mScale;
            }
        }
        init();
        updateParams(params);
    }

    /**
     * Set up the gesture detector and selected time
     */
    protected void init() {
        mSelectedDay = new Time();
        mSelectedDay.setToNow();
    }

    public void setData(HashMap<String, Integer> params, int position, int mFirstDayOfWeek) {
        mPosition = position;
        this.mFirstDayOfWeek = mFirstDayOfWeek;
        updateParams(params);
        notifyDataSetChanged();
    }

    /**
     * Parse the parameters and set any necessary fields. See
     * {@link #WEEK_PARAMS_NUM_WEEKS} for parameter details.
     *
     * @param params A list of parameters for this adapter
     */
    public void updateParams(HashMap<String, Integer> params) {
        if (params == null) {
            if (DEBUG) {
                Log.e(TAG, "WeekParameters are null! Cannot update adapter.");
            }
            return;
        }
        if (params.containsKey(SimpleWeekView.VIEW_PARAMS_HEIGHT)) {
            mViewHeight = params.get(SimpleWeekView.VIEW_PARAMS_HEIGHT);
        }
        if (params.containsKey(WEEK_PARAMS_FOCUS_MONTH)) {
            mFocusMonth = params.get(WEEK_PARAMS_FOCUS_MONTH);
        }
        if (params.containsKey(WEEK_PARAMS_FOCUS_MONTH)) {
            mNumWeeks = params.get(WEEK_PARAMS_NUM_WEEKS);
        }
        if (params.containsKey(WEEK_PARAMS_SHOW_WEEK)) {
            mShowWeekNumber = params.get(WEEK_PARAMS_SHOW_WEEK) != 0;
        }
        if (params.containsKey(WEEK_PARAMS_WEEK_START)) {
            mFirstDayOfWeek = params.get(WEEK_PARAMS_WEEK_START);
        }
        if (params.containsKey(WEEK_PARAMS_JULIAN_DAY)) {
            int julianDay = params.get(WEEK_PARAMS_JULIAN_DAY);
            mSelectedDay.setJulianDay(julianDay);
            mSelectedWeek = Time.getWeeksSinceEpochFromJulianDay(julianDay, mFirstDayOfWeek);
        }
        if (params.containsKey(WEEK_PARAMS_DAYS_PER_WEEK)) {
            mDaysPerWeek = params.get(WEEK_PARAMS_DAYS_PER_WEEK);
        }
        refresh();
    }

    /**
     * Updates the selected day and related parameters.
     *
     * @param selectedTime The time to highlight
     */
    public void setSelectedDay(Time selectedTime) {
        mSelectedDay.set(selectedTime);
        long millis = mSelectedDay.normalize(true);
        mSelectedWeek = Time.getWeeksSinceEpochFromJulianDay(
                Time.getJulianDay(millis, mSelectedDay.gmtoff), mFirstDayOfWeek);
        notifyDataSetChanged();
    }

    /**
     * Returns the currently highlighted day
     *
     * @return
     */
    public Time getSelectedDay() {
        return mSelectedDay;
    }

    /**
     * updates any config options that may have changed and refreshes the view
     */
    protected void refresh() {
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return DEFAULT_NUM_WEEKS;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleWeekView v;
        HashMap<String, Integer> drawingParams = null;
        if (convertView != null) {
            v = (SimpleWeekView) convertView;
            // We store the drawing parameters in the view so it can be recycled
            drawingParams = (HashMap<String, Integer>) v.getTag();
        } else {
            v = new SimpleWeekView(mContext);
            // Set up the new view
            LayoutParams params = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            v.setLayoutParams(params);
//            v.setClickable(true);
        }
        if (drawingParams == null) {
            drawingParams = new HashMap<String, Integer>();
        }
        drawingParams.clear();

        int selectedDay = -1;
        if (mSelectedWeek == position + mPosition) {
            selectedDay = mSelectedDay.weekDay;
            v.setOnClickSelf(true);
        }
        // pass in all the view parameters
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_HEIGHT, mViewHeight);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_SHOW_WK_NUM, mShowWeekNumber ? 1 : 0);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK_START, mFirstDayOfWeek);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_NUM_DAYS, mDaysPerWeek);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK, position + mPosition);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_FOCUS_MONTH, mFocusMonth);
//        v.setEventDate(mEventDate);
        v.setPosition(position);
        v.setWeekParams(drawingParams, mSelectedDay.timezone);
        v.invalidate();
        return v;
    }

    /**
     * Changes which month is in focus and updates the view.
     *
     * @param month The month to show as in focus [0-11]
     */
    public void updateFocusMonth(int month) {
        mFocusMonth = month;
        notifyDataSetChanged();
    }

    /**
     * Maintains the same hour/min/sec but moves the day to the tapped day.
     *
     * @param day The day that was tapped
     */
    protected void onDayTapped(Time day) {
        day.hour = mSelectedDay.hour;
        day.minute = mSelectedDay.minute;
        day.second = mSelectedDay.second;
        setSelectedDay(day);
    }
}
