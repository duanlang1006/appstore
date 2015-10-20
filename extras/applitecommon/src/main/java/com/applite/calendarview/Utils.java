package com.applite.calendarview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.text.format.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static int PAGER_CURRENTITEM = 815;//months between 1/1970 and 12/2037(如果今天是2037.12年　如果PAGER_CURRENTITEM<815那么就会滑不到1970)
    public static int WEEK_PAGER_CURRENTITEM = 3549;// weeks between 1/1/1970 and 1/1/2038
    public static int ALMANAC_PAGER_CURRENTITEM = 50000;
    public static boolean mFocusDay;

    //	public static float CALENDAR_HERGHT_RATIO=0.56f;
    public static String LeftPad_Tow_Zero(int str) {
        java.text.DecimalFormat format = new java.text.DecimalFormat("00");
        return format.format(str);
    }

    /**
     * @param mPageNumber
     * @return
     */
    public static Calendar getSelectCalendar(int mPageNumber) {
        Calendar calendar = Calendar.getInstance();
        if (mPageNumber > PAGER_CURRENTITEM) {
            for (int i = 0; i < mPageNumber - PAGER_CURRENTITEM; i++) {
                calendar = setNextViewItem(calendar);
            }
        } else if (mPageNumber < PAGER_CURRENTITEM) {
            for (int i = 0; i < PAGER_CURRENTITEM - mPageNumber; i++) {
                calendar = setPrevViewItem(calendar);
            }
        } else {
            calendar.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        }
        return calendar;
    }

    // 上一个月
    private static Calendar setPrevViewItem(Calendar calendar) {
        int iMonthViewCurrentMonth = calendar.get(Calendar.MONTH);
        int iMonthViewCurrentYear = calendar.get(Calendar.YEAR);
        iMonthViewCurrentMonth--;// 当前选择月--

        // 如果当前月为负数的话显示上一年
        if (iMonthViewCurrentMonth == -1) {
            iMonthViewCurrentMonth = 11;
            iMonthViewCurrentYear--;
        }
        calendar.set(Calendar.DAY_OF_MONTH, 1); // 设置日为当月1日
        calendar.set(Calendar.MONTH, iMonthViewCurrentMonth); // 设置月
        calendar.set(Calendar.YEAR, iMonthViewCurrentYear); // 设置年
        return calendar;
    }

    private static Calendar setNextViewItem(Calendar calendar) {
        int iMonthViewCurrentMonth = calendar.get(Calendar.MONTH);
        int iMonthViewCurrentYear = calendar.get(Calendar.YEAR);
        iMonthViewCurrentMonth++;
        if (iMonthViewCurrentMonth == 12) {
            iMonthViewCurrentMonth = 0;
            iMonthViewCurrentYear++;
        }
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, iMonthViewCurrentMonth);
        calendar.set(Calendar.YEAR, iMonthViewCurrentYear);
        return calendar;
    }

    public static String CalendarTostr(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(calendar.getTime());
        return dateStr;
    }

    public static Calendar strToCalendar(String time) {
        Calendar calendar = Calendar.getInstance();
        if (!TextUtils.isEmpty(time)) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = dateFormat.parse(time);
                calendar = Calendar.getInstance();
                calendar.setTime(date);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return calendar;
    }

}
