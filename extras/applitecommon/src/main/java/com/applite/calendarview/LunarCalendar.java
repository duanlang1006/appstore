/** Create by Spreadst */
package com.applite.calendarview;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.applite.common.R;

public class LunarCalendar {
    private static String[] lunarCalendarNumber = null;

    private static String[] lunarCalendarTen = null;

    private static String[] year_of_birth = null;

    private static String[] lunarTerm = null;
    
    private static String[] sectionalTermNames = null;
    
    private static String lunarLeapTag = null, lunarMonthTag = null,
            zhengyueTag = null;

    Context mContext;

    public int lunarYear = 0;

    public int lunarMonth = 0;

    public int lunarDay = 0;

    public int solarYear = 0;

    public int solarMonth = 0;

    public int solarDay = 0;

    public boolean isLeapMonth = false;

    public boolean isFastival = false;

    /* SPRD: bug257032 2013-12-26 special solar term dates @{ */
    private static String[] mSpecialSolarTermDates;
    /* SPRD: bug257032 2013-12-26 @} */

    /* SPRD: bug258885 2013-12-25 update lunar language resources info @{ */
    private static boolean mHasInitialedRes;
    // such as Mid-Autumn Day
    private static String[] mTraditionalFestivalStr;
    // such as Valentine's Day
    private static String[] mFestivalStr;
    // such as Jia, Yi, Bing, Ding
    private static String[] mYearStemStr;
    // such as Zi, Chou, Yin, Mao
    private static String[] mYearBranchStr;

    static {
        mHasInitialedRes = false;
    }

    public LunarCalendar(Context context) {
        mContext = context;
        if (!mHasInitialedRes) {
            reloadLanguageResources(context);
            mHasInitialedRes = true;
        }
    }

    public static void reloadLanguageResources(Context ctx) {
        if (lunarCalendarNumber == null) {
            lunarCalendarNumber = new String[12];
        }
        lunarCalendarNumber[0] = getString(ctx, R.string.chineseNumber1);
        lunarCalendarNumber[1] = getString(ctx, R.string.chineseNumber2);
        lunarCalendarNumber[2] = getString(ctx, R.string.chineseNumber3);
        lunarCalendarNumber[3] = getString(ctx, R.string.chineseNumber4);
        lunarCalendarNumber[4] = getString(ctx, R.string.chineseNumber5);
        lunarCalendarNumber[5] = getString(ctx, R.string.chineseNumber6);
        lunarCalendarNumber[6] = getString(ctx, R.string.chineseNumber7);
        lunarCalendarNumber[7] = getString(ctx, R.string.chineseNumber8);
        lunarCalendarNumber[8] = getString(ctx, R.string.chineseNumber9);
        lunarCalendarNumber[9] = getString(ctx, R.string.chineseNumber10);
        lunarCalendarNumber[10] = getString(ctx, R.string.chineseNumber11);
        lunarCalendarNumber[11] = getString(ctx, R.string.chineseNumber12);

        if (lunarCalendarTen == null) {
            lunarCalendarTen = new String[5];
        }
        lunarCalendarTen[0] = getString(ctx, R.string.chineseTen0);
        lunarCalendarTen[1] = getString(ctx, R.string.chineseTen1);
        lunarCalendarTen[2] = getString(ctx, R.string.chineseTen2);
        lunarCalendarTen[3] = getString(ctx, R.string.chineseTen3);
        lunarCalendarTen[4] = getString(ctx, R.string.chineseTen4);

        if (year_of_birth == null) {
            year_of_birth = new String[12];
        }
        year_of_birth[0] = getString(ctx, R.string.animals0);
        year_of_birth[1] = getString(ctx, R.string.animals1);
        year_of_birth[2] = getString(ctx, R.string.animals2);
        year_of_birth[3] = getString(ctx, R.string.animals3);
        year_of_birth[4] = getString(ctx, R.string.animals4);
        year_of_birth[5] = getString(ctx, R.string.animals5);
        year_of_birth[6] = getString(ctx, R.string.animals6);
        year_of_birth[7] = getString(ctx, R.string.animals7);
        year_of_birth[8] = getString(ctx, R.string.animals8);
        year_of_birth[9] = getString(ctx, R.string.animals9);
        year_of_birth[10] = getString(ctx, R.string.animals10);
        year_of_birth[11] = getString(ctx, R.string.animals11);

        lunarLeapTag = getString(ctx, R.string.leap_month);
        lunarMonthTag = getString(ctx, R.string.month);
        zhengyueTag = getString(ctx, R.string.zheng);

        if (lunarTerm == null) {
            lunarTerm = new String[24];
        }
        lunarTerm[0] = getString(ctx, R.string.terms0);
        lunarTerm[1] = getString(ctx, R.string.terms1);
        lunarTerm[2] = getString(ctx, R.string.terms2);
        lunarTerm[3] = getString(ctx, R.string.terms3);
        lunarTerm[4] = getString(ctx, R.string.terms4);
        lunarTerm[5] = getString(ctx, R.string.terms5);
        lunarTerm[6] = getString(ctx, R.string.terms6);
        lunarTerm[7] = getString(ctx, R.string.terms7);
        lunarTerm[8] = getString(ctx, R.string.terms8);
        lunarTerm[9] = getString(ctx, R.string.terms9);
        lunarTerm[10] = getString(ctx, R.string.terms10);
        lunarTerm[11] = getString(ctx, R.string.terms11);
        lunarTerm[12] = getString(ctx, R.string.terms12);
        lunarTerm[13] = getString(ctx, R.string.terms13);
        lunarTerm[14] = getString(ctx, R.string.terms14);
        lunarTerm[15] = getString(ctx, R.string.terms15);
        lunarTerm[16] = getString(ctx, R.string.terms16);
        lunarTerm[17] = getString(ctx, R.string.terms17);
        lunarTerm[18] = getString(ctx, R.string.terms18);
        lunarTerm[19] = getString(ctx, R.string.terms19);
        lunarTerm[20] = getString(ctx, R.string.terms20);
        lunarTerm[21] = getString(ctx, R.string.terms21);
        lunarTerm[22] = getString(ctx, R.string.terms22);
        lunarTerm[23] = getString(ctx, R.string.terms23);

        if (mTraditionalFestivalStr == null) {
            mTraditionalFestivalStr = new String[10];
        }
        mTraditionalFestivalStr[0] = getString(ctx, R.string.chunjie);
        mTraditionalFestivalStr[1] = getString(ctx, R.string.yuanxiao);
        mTraditionalFestivalStr[2] = getString(ctx, R.string.duanwu);
        mTraditionalFestivalStr[3] = getString(ctx, R.string.qixi);
        mTraditionalFestivalStr[4] = getString(ctx, R.string.zhongqiu);
        mTraditionalFestivalStr[5] = getString(ctx, R.string.chongyang);
        mTraditionalFestivalStr[6] = getString(ctx, R.string.laba);
        mTraditionalFestivalStr[7] = getString(ctx, R.string.beixiaonian);
        mTraditionalFestivalStr[8] = getString(ctx, R.string.nanxiaonian);
        mTraditionalFestivalStr[9] = getString(ctx, R.string.chuxi);

        if (mFestivalStr == null) {
            mFestivalStr = new String[24];
        }
        mFestivalStr[0] = getString(ctx, R.string.new_Year_day);
        mFestivalStr[1] = getString(ctx, R.string.meteorology_day);
        mFestivalStr[2] = getString(ctx, R.string.valentin_day);
        mFestivalStr[3] = getString(ctx, R.string.women_day);
        mFestivalStr[4] = getString(ctx, R.string.ear_day);
        mFestivalStr[5] = getString(ctx, R.string.arbor_day);
        mFestivalStr[6] = getString(ctx, R.string.labol_day);
        mFestivalStr[7] = getString(ctx, R.string.youth_day);
        mFestivalStr[8] = getString(ctx, R.string.children_day);
        mFestivalStr[9] = getString(ctx, R.string.Communist_day);
        mFestivalStr[10] = getString(ctx, R.string.army_day);
        mFestivalStr[11] = getString(ctx, R.string.teacher_day);
        mFestivalStr[12] = getString(ctx, R.string.national_day);
        mFestivalStr[13] = getString(ctx, R.string.christmas_day);
        mFestivalStr[14] = getString(ctx, R.string.fool_day);
        mFestivalStr[15] = getString(ctx, R.string.earth_day);
        mFestivalStr[16] = getString(ctx, R.string.nurses_day);
        mFestivalStr[17] = getString(ctx, R.string.no_tobacco_day);
        mFestivalStr[18] = getString(ctx, R.string.environment_day);
        mFestivalStr[19] = getString(ctx, R.string.halloween_day);
        mFestivalStr[20] = getString(ctx, R.string.singles_day);
        mFestivalStr[21] = getString(ctx, R.string.aids_day);
        mFestivalStr[22] = getString(ctx, R.string.nanjing_massacre_day);
        mFestivalStr[23] = getString(ctx, R.string.war_day);
        if (mYearStemStr == null) {
            mYearStemStr = new String[10];
        }
        mYearStemStr[0] = getString(ctx, R.string.jia);
        mYearStemStr[1] = getString(ctx, R.string.yi);
        mYearStemStr[2] = getString(ctx, R.string.bing);
        mYearStemStr[3] = getString(ctx, R.string.ding);
        mYearStemStr[4] = getString(ctx, R.string.wutian);
        mYearStemStr[5] = getString(ctx, R.string.ji);
        mYearStemStr[6] = getString(ctx, R.string.geng);
        mYearStemStr[7] = getString(ctx, R.string.xin);
        mYearStemStr[8] = getString(ctx, R.string.ren);
        mYearStemStr[9] = getString(ctx, R.string.gui);

        if (mYearBranchStr == null) {
            mYearBranchStr = new String[12];
        }
        mYearBranchStr[0] = getString(ctx, R.string.zi);
        mYearBranchStr[1] = getString(ctx, R.string.chou);
        mYearBranchStr[2] = getString(ctx, R.string.yin);
        mYearBranchStr[3] = getString(ctx, R.string.mao);
        mYearBranchStr[4] = getString(ctx, R.string.chen);
        mYearBranchStr[5] = getString(ctx, R.string.si);
        mYearBranchStr[6] = getString(ctx, R.string.wudi);
        mYearBranchStr[7] = getString(ctx, R.string.wei);
        mYearBranchStr[8] = getString(ctx, R.string.shen);
        mYearBranchStr[9] = getString(ctx, R.string.you);
        mYearBranchStr[10] = getString(ctx, R.string.xu);
        mYearBranchStr[11] = getString(ctx, R.string.hai);

        /* SPRD: bug257032 2013-12-26 special solar term dates @{ */
        if (mSpecialSolarTermDates == null) {
            mSpecialSolarTermDates = ctx.getResources()
                .getStringArray(R.array.special_solar_term_dates);
        }
        /* SPRD: bug257032 2013-12-26 */
        
        //my add sectionalTermNames
        if (sectionalTermNames == null) {
        	sectionalTermNames = new String[12];
        }
        sectionalTermNames[0] = getString(ctx, R.string.terms0);
        sectionalTermNames[1] = getString(ctx, R.string.terms2);
        sectionalTermNames[2] = getString(ctx, R.string.terms4);
        sectionalTermNames[3] = getString(ctx, R.string.terms6);
        sectionalTermNames[4] = getString(ctx, R.string.terms8);
        sectionalTermNames[5] = getString(ctx, R.string.terms10);
        sectionalTermNames[6] = getString(ctx, R.string.terms12);
        sectionalTermNames[7] = getString(ctx, R.string.terms14);
        sectionalTermNames[8] = getString(ctx, R.string.terms16);
        sectionalTermNames[9] = getString(ctx, R.string.terms18);
        sectionalTermNames[10] = getString(ctx, R.string.terms20);
        sectionalTermNames[11] = getString(ctx, R.string.terms22);
    }

    public static void clearLanguageResourcesRefs() {
        lunarCalendarNumber = null;
        lunarCalendarTen = null;
        year_of_birth = null;
        lunarTerm = null;
        mTraditionalFestivalStr = null;
        mFestivalStr = null;
        mYearStemStr = null;
        mYearBranchStr = null;

        mHasInitialedRes = false;

        // SPRD: bug257032 2013-12-26 special solar term dates
        mSpecialSolarTermDates = null;
    }

    private static String getString(Context ctx, int resId) {
        return ctx.getString(resId);
    }

    public String getTraditionalFestival() {
        return getTraditionalFestival(lunarYear, lunarMonth, lunarDay);
    }

    public String getTraditionalFestival(int lunarYear, int lunarMonth,
            int lunarDay) {
        /* SPRD: bug254439 delete duplicatin traditional festival string @{ */
        // if is leap month, return empty string
        if (isLeapMonth) {
            return "";
        }
        /* @} */
        String festivalStr = "";
        if (lunarMonth == 1 && lunarDay == 1)
            festivalStr = mTraditionalFestivalStr[0];
        if (lunarMonth == 1 && lunarDay == 15)
            festivalStr = mTraditionalFestivalStr[1];
        if (lunarMonth == 5 && lunarDay == 5)
            festivalStr = mTraditionalFestivalStr[2];
        if (lunarMonth == 7 && lunarDay == 7)
            festivalStr = mTraditionalFestivalStr[3];
        if (lunarMonth == 8 && lunarDay == 15)
            festivalStr = mTraditionalFestivalStr[4];
        if (lunarMonth == 9 && lunarDay == 9)
            festivalStr = mTraditionalFestivalStr[5];
        if (lunarMonth == 12 && lunarDay == 8)
            festivalStr = mTraditionalFestivalStr[6];
        if (lunarMonth == 12 && lunarDay == 23)
            festivalStr = mTraditionalFestivalStr[7];
        if (lunarMonth == 12 && lunarDay == 24)
            festivalStr = mTraditionalFestivalStr[8];

        if (lunarMonth == 12) {
            if (lunarDay == LunarCalendarConvertUtil.getLunarMonthDays(
                    lunarYear, lunarMonth))
                festivalStr = mTraditionalFestivalStr[9];
        }
        return festivalStr;
    }

    public String getFestival() {
        return getFestival(solarMonth, solarDay);
    }

    private String getFestival(int lunarMonth, int lunarDay) {
        String festivalStr = "";
        if (lunarMonth == 0 && lunarDay == 1)
            festivalStr = mFestivalStr[0];
        if (lunarMonth == 1 && lunarDay == 10)
            festivalStr = mFestivalStr[1];
        if (lunarMonth == 1 && lunarDay == 14)
            festivalStr = mFestivalStr[2];
        if (lunarMonth == 2 && lunarDay == 8)
            festivalStr = mFestivalStr[3];
        if (lunarMonth == 2 && lunarDay == 3)
            festivalStr = mFestivalStr[4];
        if (lunarMonth == 2 && lunarDay == 12)
            festivalStr = mFestivalStr[5];
        if (lunarMonth == 4 && lunarDay == 1)
            festivalStr = mFestivalStr[6];
        if (lunarMonth == 4 && lunarDay == 4)
            festivalStr = mFestivalStr[7];
        if (lunarMonth == 5 && lunarDay == 1)
            festivalStr = mFestivalStr[8];
        /** add 20130702 spreadst of 181042 no communist day start */
        if (lunarMonth == 6 && lunarDay == 1)
            festivalStr = mFestivalStr[9];
        /** add 20130702 spreadst of 181042 no communist day end */
        if (lunarMonth == 7 && lunarDay == 1)
            festivalStr = mFestivalStr[10];
        if (lunarMonth == 8 && lunarDay == 10)
            festivalStr = mFestivalStr[11];
        if (lunarMonth == 9 && lunarDay == 1)
            festivalStr = mFestivalStr[12];
        if (lunarMonth == 11 && lunarDay == 25)
            festivalStr = mFestivalStr[13];
        
        
        if (lunarMonth == 3 && lunarDay == 1)
            festivalStr = mFestivalStr[14];
        if (lunarMonth == 3 && lunarDay == 22)
            festivalStr = mFestivalStr[15];
        if (lunarMonth == 4 && lunarDay == 12)
            festivalStr = mFestivalStr[16];
        if (lunarMonth == 4 && lunarDay == 31)
            festivalStr = mFestivalStr[17];
        if (lunarMonth == 5&& lunarDay == 5)
            festivalStr = mFestivalStr[18];
        if (lunarMonth == 9 && lunarDay == 31)
            festivalStr = mFestivalStr[19];
        if (lunarMonth == 10 && lunarDay == 11)
            festivalStr = mFestivalStr[20];
        if (lunarMonth ==11 && lunarDay ==1)
            festivalStr = mFestivalStr[21];
        if (lunarMonth == 11 && lunarDay == 13)
            festivalStr = mFestivalStr[22];
        if (lunarMonth == 8 && lunarDay == 3)
            festivalStr = mFestivalStr[23];
        return festivalStr;
    }

    /* SPRD: bug254474 correct the algorithm of getting solar terms @{ */
    /* SPRD: bug257032 2013-12-26 special solar term dates @{ */
    private String getSolarTerm(int year, int month, int date) {
        String termStr = "";
        SpecialSolarTermInfo info = getSpecialSolarTermInfo(year, month, date);
        if (info != null && info.mIndex != -1) {
            if (info.mIndex != 0) {
                termStr = info.mTermStr;
            } // else info.mIndex == 0, then this should return empty string
        } else {
            if (date == LunarCalendarConvertUtil.getSolarTermDayOfMonth(year,
                    month * 2)) {
                termStr = lunarTerm[month * 2];
            } else if (date == LunarCalendarConvertUtil.getSolarTermDayOfMonth(
                    year, month * 2 + 1)) {
                termStr = lunarTerm[month * 2 + 1];
            }
        }
        return termStr;
    }
    /* SPRD: bug257032 2013-12-26 @} */
    /* @} */

    private String getChinaMonthString() {
        return getChinaMonthString(lunarMonth, isLeapMonth);
    }

    private String getChinaMonthString(int lunarMonth, boolean isLeapMonth) {
        String chinaMonth = (isLeapMonth ? lunarLeapTag : "")
                + ((lunarMonth == 1) ? zhengyueTag
                        : lunarCalendarNumber[lunarMonth - 1]) + lunarMonthTag;
        return chinaMonth;
    }

    private String getChinaDayString(boolean notDisplayLunarMonthForFirstDay) {
        return getChinaDayString(lunarMonth, lunarDay, isLeapMonth,
                notDisplayLunarMonthForFirstDay);
    }

    public String getChinaDayString(int lunarMonth, int lunarDay,
            boolean isLeapMonth, boolean notDisplayLunarMonthForFirstDay) {
        if (lunarDay > 30)
            return "";
        if (lunarDay == 1 && notDisplayLunarMonthForFirstDay)
            return getChinaMonthString(lunarMonth, isLeapMonth);
        if (lunarDay == 10)
            return lunarCalendarTen[0] + lunarCalendarTen[1];
        if (lunarDay == 20)
            return lunarCalendarTen[4] + lunarCalendarTen[1];

        return lunarCalendarTen[lunarDay / 10]
                + lunarCalendarNumber[(lunarDay + 9) % 10];
    }

    private String getChinaYearString() {
        return getChinaYearString(lunarYear);
    }

    private String getChinaYearString(int lunarYear) {
        return String.valueOf(lunarYear);
    }

    private String getLunarYearString(int num) {
        return (mYearStemStr[num % 10] + mYearBranchStr[num % 12]);
    }
    /* SPRD: bug258885 2013-12-25 @} */

    public String getLunarYear(int year) {
        int num = year - 1900 + 36;
        return getLunarYearString(num);
    }

    public String animalsYear(int year) {
        return year_of_birth[(year - 4) % 12];
    }

    public String[] getLunarCalendarInfo(boolean notDisplayLunarMonthForFirstDay) {
        if (lunarYear == 0 || lunarMonth == 0 || lunarDay == 0)
            return null;// new String[]{null,null,null,null,null};
        String lunarYearStr = getChinaYearString();
        String lunarMonthStr = getChinaMonthString();
        String lunarDayStr = getChinaDayString(notDisplayLunarMonthForFirstDay);

        String traditionFestivalStr = getTraditionalFestival();
        String festivalStr = getFestival();
        // SPRD: bug254474 correct the algorithm of getting solar terms
        String solarTermStr = getSolarTerm(solarYear, solarMonth, solarDay);

        return new String[] { lunarYearStr, lunarMonthStr, lunarDayStr,
                traditionFestivalStr, festivalStr, solarTermStr };
    }

    public String getLunarDayInfo() {
        if (lunarYear == 0 || lunarMonth == 0 || lunarDay == 0) {
            return "";
        }
        // if this day is traditional festival, show as it
        String traditionFestivalStr = getTraditionalFestival();
        String festivalStr = getFestival();
        // SPRD: bug254474 correct the algorithm of getting solar terms
        String solarTermStr = getSolarTerm(solarYear, solarMonth, solarDay);
        /*add 20130703 Spreadst of 176738 the color error start*/
        if (!traditionFestivalStr.trim().equals("")
                || !festivalStr.trim().equals("")
                || !solarTermStr.trim().equals("")) {
            isFastival = true;
        } else {
            isFastival = false;
        }
        /* add 20130703 Spreadst of 176738 the color error end */
        if (traditionFestivalStr != null && festivalStr != null
                && !traditionFestivalStr.trim().equals("")
                && !festivalStr.trim().equals("")) {
            return traditionFestivalStr + "/" + festivalStr;
        }

        if (traditionFestivalStr != null && solarTermStr != null
                && !traditionFestivalStr.trim().equals("")
                && !solarTermStr.trim().equals("")) {
            return traditionFestivalStr + "/" + solarTermStr;
        }

        if (festivalStr != null && solarTermStr != null
                && !festivalStr.trim().equals("")
                && !solarTermStr.trim().equals("")) {
            return festivalStr + "/" + solarTermStr;
        }

        if (traditionFestivalStr != null
                && !traditionFestivalStr.trim().equals("")) {
            return traditionFestivalStr;
        }

        // if this day is festival, show as it
        if (festivalStr != null && !festivalStr.trim().equals("")) {
            return festivalStr;
        }

        // if this day is solar term, show as it
        if (solarTermStr != null && !solarTermStr.trim().equals("")) {
            return solarTermStr;
        }

        // if this day is first day of lunar month, show lunar month number
        String lunarMonthStr = getChinaMonthString();
        if (lunarDay == 1) {
            return lunarMonthStr;
        }

        // otherwise, show lunar day number
        String lunarDayStr = getChinaDayString(false);
        return lunarDayStr;

    }

    /* SPRD: bug257032 2013-12-26 special solar term dates @{ */
    final static class SpecialSolarTermInfo {
        String mSpecialStr;
        String mTermStr;
        int mIndex;

        SpecialSolarTermInfo(String specialStr, String termStr,
                int index) {
            mSpecialStr = specialStr;
            mTermStr = termStr;
            mIndex = index;
        }
    }

    private static SpecialSolarTermInfo getSpecialSolarTermInfo(int year,
            int month, int day) {
        SpecialSolarTermInfo info = null;
        if (mSpecialSolarTermDates != null) {
            // out date format will be xxxxxxxx, eg. 20131221, length equals 8
            StringBuilder dateStrBuilder = new StringBuilder(8);
            dateStrBuilder.setLength(0);
            dateStrBuilder.append(year);
            // month is from 0-11
            if (month < 9) {
                dateStrBuilder.append(0);
            }
            dateStrBuilder.append(month + 1);
            // day is from 1-31
            if (day < 10) {
                dateStrBuilder.append(0);
            }
            dateStrBuilder.append(day);
            //Log.d("chen", "current date str: " + dateStrBuilder.toString());
            int index;
            String term = "";
            for (String dateStr : mSpecialSolarTermDates) {
                index = dateStr.indexOf(dateStrBuilder.toString());
                if (index != -1) {
                    term = lunarTerm[Integer.valueOf(dateStr
                        .substring(dateStr.lastIndexOf('|') + 1))];
                    info = new SpecialSolarTermInfo(dateStr, term,
                            index);
                    break;
                }
            }
        }
        return info;
    }
    /* SPRD: bug257032 2013-12-26 @} */
    //myself lunarTerm
    //立春
    /**
     * 一、计算公式：[Y*D+C]-L
     * 公式解读：年数的后2位乘0.2422加3.87取整数减闰年数。21世纪C值=3.87，22世纪C值=4.15。
     * 举例说明：2058年立春日期的计算步骤[58×.0.2422+3.87]-[(58-1)/4]=17-14=3，则2月3日立春
     * @param year
     * @return
     */
	public int beginOfSpring(int year) {
		int endYear = year % 100;
		return (int) (endYear * 0.2422 + 3.87) - (endYear - 1) / 4;
	}
	/**
	 * 定气法计算二十四节气,二十四节气是按地球公转来计算的，并非是阴历计算的节气的定法有两种。古代历法采用的称为"恒气"，即按时间把一年等分为24份，
	 * 每一节气平均得15天有余，所以又称"平气"。现代农历采用的称为"定气"，即按地球在轨道上的位置为标准，一周360°，两节气之间相隔15°。由于冬至时地
	 * 球位于近日点附近，运动速度较快，因而太阳在黄道上移动15°的时间不到15天。夏至前后的情况正好相反，太阳在黄道上移动较慢，一个节气达16天之多。采用
	 * 定气时可以保证春、秋两分必然在昼夜平分的那两天。
	 * @param date1
	 * @return
	 */
	public String ChineseTwentyFourDay(Calendar calendarDate,boolean isShowInAlmanac) {

//		String[] SolarTerm = new String[] { "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
//				"清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑",
//				"白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至" };
		int[] sTermInfo = new int[] { 0, 21208, 42467, 63836, 85337, 107014,
				128867, 150921, 173149, 195551, 218072, 240693, 263343, 285989,
				308563, 331033, 353350, 375494, 397447, 419210, 440795, 462224,
				483532, 504758 };
		Calendar cal = Calendar.getInstance();
		cal.set(1900, 0, 6, 2, 5, 0);// 月份从0开始
		final Date baseDateAndTime = cal.getTime();
		double num;
		int y;
		int index=0;
		String monthGanZhi = null;
		String tempStr = "";
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		Date date=null;
		try {
			date = formater.parse(Utils.CalendarTostr(calendarDate) + " 23:59:59");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 获取年份
		y = calendarDate.get(Calendar.YEAR);
		for (int i = 1; i <= 24; i++) {
			num = 525948.76 * (y - 1900) + sTermInfo[i - 1];
			Calendar calendarMin = Calendar.getInstance();
			calendarMin.setTime(baseDateAndTime);
			calendarMin.add(Calendar.MINUTE, (int) Math.ceil(num) + 5);
			// 对时间进行格式化字符串比较
			if (calendarMin.getTimeInMillis()>date.getTime()) {
				if ((i-1)%2!=0) {
					continue;
				}
				tempStr = lunarTerm[i - 1];
				break;
			}
		}
		for (int i = 0; i < sectionalTermNames.length; i++) {
			if (sectionalTermNames[i].equals(tempStr)) {
				index=i;
				break;
			}
		}
		int GanYear=getHeavenlyStems(calendarDate);
		switch (GanYear) {
		case 0:
		case 5:
			int gan=index;
			if (index<2) {
				gan+=2;
			}
			if (isShowInAlmanac) {
				monthGanZhi=mYearStemStr[gan%9]+"\n"+mYearBranchStr[(index)%11];
			}else {
				monthGanZhi=mYearStemStr[gan%9]+mYearBranchStr[(index)%11];
			}
			break;
		case 1:
		case 6:
			int gan1=index;
			if (index<2) {
				gan1+=2;
			}
			if (isShowInAlmanac) {
				monthGanZhi=mYearStemStr[(gan1+2)%10]+"\n"+mYearBranchStr[(index)%12];
			}else {
				monthGanZhi=mYearStemStr[(gan1+2)%10]+mYearBranchStr[(index)%12];
			}
			break;
		case 2:
		case 7:
			int gan2=index;
			if (index<2) {
				gan2+=2;
			}
			if (isShowInAlmanac) {
				monthGanZhi=mYearStemStr[(gan2+4)%10]+"\n"+mYearBranchStr[(index)%13];
			}else {
				monthGanZhi=mYearStemStr[(gan2+4)%10]+mYearBranchStr[(index)%13];
			}
			break;
		case 3:
		case 8:
			int gan3=index;
			if (index<2) {
				gan3+=2;
			}
			if (isShowInAlmanac) {
				monthGanZhi=mYearStemStr[(gan3+6)%10]+"\n"+mYearBranchStr[(index)%14];
			}else {
				monthGanZhi=mYearStemStr[(gan3+6)%10]+mYearBranchStr[(index)%14];
			}
			break;
		case 4:
		case 9:
			int gan4=index;
			if (index<2) {
				gan4+=2;
			}
			if (isShowInAlmanac) {
				monthGanZhi=mYearStemStr[(gan4-2)%10]+"\n"+mYearBranchStr[(index)%12];
			}else {
				monthGanZhi=mYearStemStr[(gan4-2)%10]+mYearBranchStr[(index)%12];
			}
			break;
		default:
			break;
		}
		if (isShowInAlmanac) {
			return monthGanZhi+"\n月";
		}else {
			return monthGanZhi+"月";
		}
	}
	private int getHeavenlyStems(Calendar calendar) {
		int year = calendar.get(Calendar.YEAR);
		int beginOfSpring = beginOfSpring(year);
		// 日期<立春日，算上一年的
		int num;
		if (calendar.get(Calendar.MONTH) < 1) {
			 num= (year - 1 - 1900 + 36)%10;
		} else if (calendar.get(Calendar.MONTH) == 1) {
			// 日期>=立春日
			if (calendar.get(Calendar.DAY_OF_MONTH) >= beginOfSpring) {
				num = (year - 1900 + 36)%10;
			} else {
				num= (year - 1 - 1900 + 36)%10;
			}
		} else {
			num = (year - 1900 + 36)%10;
		}
		return num;
	}
	public String getHeavenlyStemsDay(Calendar calendar,boolean isShowInAlmanac) {
		// G = 4C + [C / 4] + 5y + [y / 4] + [3 * (M + 1) / 5] + d - 3
		// Z = 8C + [C / 4] + 5y + [y / 4] + [3 * (M + 1) / 5] + d + 7 + i
		int C=calendar.get(Calendar.YEAR)/100;
		int M=0;
		int y=0;
		int d=calendar.get(Calendar.DAY_OF_MONTH);
		int i=0;
		if (calendar.get(Calendar.MONTH)==0) {
			M=13;
			y=calendar.get(Calendar.YEAR)%100-1;
			i=0;
		}else if (calendar.get(Calendar.MONTH)==1) {
			M=14;
			y=calendar.get(Calendar.YEAR)%100-1;
			i=6;
		}else {
			M=calendar.get(Calendar.MONTH)+1;
			y=calendar.get(Calendar.YEAR)%100;
			i=(calendar.get(Calendar.MONTH)+1)/2==0?6:0;
		}
		int G=(4*C+C/4+5*y+y/4+3*(M+1)/5+d-3)%10;
		int Z=(8*C+C/4+5*y+y/4+3*(M+1)/5+d+7+i)%12;
		G=G-1;
		Z=Z-1;
		if (G<0) {
			G=9;
		}
		if (Z<0) {
			Z=11;
		}
		if (isShowInAlmanac) {
			return mYearStemStr[G]+"\n"+mYearBranchStr[Z]+"\n日";
		}else {
			return mYearStemStr[G]+mYearBranchStr[Z]+"日";
		}
		
	}
	public int getLunarMonth() {
		return lunarMonth;
	}
	public int getLunarYear() {
		return lunarYear;
	}
	public int getLunarDay() {
		return lunarDay;
	}
}
