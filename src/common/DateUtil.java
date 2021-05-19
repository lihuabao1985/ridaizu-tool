package common;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.common.base.Strings;

/**
 * 日付ユーティリティクラス
 *
 * @author
 *
 */
public class DateUtil {

    /**
     * 長い形式日付フォーマット.
     */
    public static final String LONG_DATE = "yyyy/MM/dd HH:mm:ss";

    public static final String LONG_DATE_SSS = "yyyy/MM/dd HH:mm:ss.SSS";

    public static final String LONG_DATE_SS = "yyyy/MM/dd HH:mm:ss.SS";

    public static final String LONG_DATE_HYPHEN = "yyyy-MM-dd HH:mm:ss";

    public static final String LONG_DATE_SSS_HYPHEN = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 短い形式日付フォーマット.
     */
    public static final String SHORT_DATE = "yyyy/MM/dd";

    public static final String SHORT_DATE_HYPHEN = "yyyy-MM-dd";

    public static final String SHORT_DATE_YYYYMM = "yyyy/MM";

    public static final String SHORT_DATE_YYYYMM_HYPHEN = "yyyy-MM";

    public static final String SHORT_DATE_HANZI = "yyyy年MM月dd日";

    /**
     * YYYYMMDD形式日付フォーマット.
     */
    public static final String NORMAL_DATE = "yyyyMMdd";

    public static final String NORMAL_DATE_SHORT = "yyyyMM";

    /**
     * 指定したフォーマットでストリングをTimestamp型に転換する.
     *
     * @param format
     *            日付フォーマット
     * @param date
     *            日付
     * @return Timestamp
     */
    public static Timestamp stringToTimestamp(String format, String date) {

        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date d = null;
        try {
            d = sdf.parse(date);
            return new Timestamp(d.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 指定したフォーマットでストリングをDate型に転換する.
     *
     * @param format
     * @param date
     * @return
     */
    public static Date stringToDate(String format, String date) {

        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    /**
     * 日付をストリングに転換する
     *
     * @param date
     *            日付
     * @param format
     *            フォーマット
     * @return
     */
    public static String dateToString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 基準日付のn年後の日付文字列を取得する
     *
     * <pre>
     * 例）
     *   addMonthDate("200608", 2)	= 200610
     * </pre>
     * @param startDate YYYYMMフォーマットの基準日付
     * @param n　基準日付にプラスする月数
     * @return startDateのnヵ月後の日付
     * @throws NumberFormatException
     */
    public static String addYearDate(String startDate, int n) {
        if (null == startDate) {
            return "";
        }

        String[] date = new String[] {};
        if (startDate.contains("/")) {

            date = startDate.split("/");
        } else if (startDate.contains("-")) {

            date = startDate.split("-");
        } else {
            return "";
        }

        int year = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]) - 1;
        int day = Integer.parseInt(date[2].split(" ")[0]);
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        cal.add(Calendar.YEAR, n);
        return dateToString(cal.getTime(), SHORT_DATE);
    }

    /**
     * 基準日付のn年後の日付文字列を取得する
     *
     * <pre>
     * 例）
     *   addMonthDate("200608", 2)	= 200610
     * </pre>
     * @param startDate YYYYMMフォーマットの基準日付
     * @param n　基準日付にプラスする月数
     * @return startDateのnヵ月後の日付
     * @throws NumberFormatException
     */
    public static String addMonthDate(String startDate, int n) {
        if (null == startDate) {
            return "";
        }

        String[] date = new String[] {};
        if (startDate.contains("/")) {

            date = startDate.split("/");
        } else if (startDate.contains("-")) {

            date = startDate.split("-");
        } else {
            return "";
        }

        int year = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]) - 1;
        int day = Integer.parseInt(date[2].split(" ")[0]);
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        cal.add(Calendar.MONTH, n);
        return dateToString(cal.getTime(), SHORT_DATE);
    }

    /**
     * 基準日付のn日後の日付文字列を取得する
     *
     * <pre>
     * 例）
     *   addMonthDate("200608", 2)	= 200610
     * </pre>
     * @param startDate YYYYMMフォーマットの基準日付
     * @param n　基準日付にプラスする月数
     * @return startDateのnヵ月後の日付
     * @throws NumberFormatException
     */
    public static String addDayDate(String startDate, int n) {
        if (null == startDate) {
            return "";
        }

        String[] date = new String[] {};
        if (startDate.contains("/")) {

            date = startDate.split("/");
        } else if (startDate.contains("-")) {

            date = startDate.split("-");
        } else {
            return "";
        }

        int year = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]) - 1;
        int day = Integer.parseInt(date[2].split(" ")[0]);
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        cal.add(Calendar.DATE, n);
        return dateToString(cal.getTime(), SHORT_DATE);
    }

    public static Timestamp getCurrentDateTime() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static String timestampToStringyyyyMMddHHmm(Timestamp date) {
        if (null != date) {
            return date.toString().replace("-", "/").substring(0,16);
        }
        return "";
    }

    public static String dateFor235959(String date) {
        if (date == null || "".equals(date))
            return "";

        return date.trim() + " 23:59:59";
    }

    public static List<String> getYearList(String startYear, String endYear) {

        int iStartYear = 0;
        int iEndYear = 0;

        if (Strings.isNullOrEmpty(startYear) && Strings.isNullOrEmpty(endYear)) {

            return null;
        } else if (!Strings.isNullOrEmpty(startYear) && !Strings.isNullOrEmpty(endYear)) {

            iStartYear = Integer.valueOf(startYear);
            iEndYear = Integer.valueOf(endYear);

        } else if (!Strings.isNullOrEmpty(startYear)) {

            iStartYear = Integer.valueOf(startYear);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtil.getCurrentDateTime());
            iEndYear = calendar.get(Calendar.YEAR);

        } else if (!Strings.isNullOrEmpty(endYear)) {
            iEndYear = Integer.valueOf(endYear);
            iStartYear = iEndYear - 10;
        }

        List<String> yearList = new ArrayList<String>();
        for (int i = iStartYear; i <= iEndYear; i++) {
            yearList.add(String.valueOf(i));
        }

        return yearList;
    }

    public static List<String> getMonthList(String startYear, String startMonth, String endYear, String endMonth) {

        String startDate = "";
        String endDate = "";

        if ((Strings.isNullOrEmpty(startYear) && Strings.isNullOrEmpty(startMonth) &&
                Strings.isNullOrEmpty(endYear) && Strings.isNullOrEmpty(endMonth)) ||
                (Strings.isNullOrEmpty(startYear) && Strings.isNullOrEmpty(endYear))) {

            return null;
        }

        if (!Strings.isNullOrEmpty(startYear) && !Strings.isNullOrEmpty(endYear)) {

            startDate += startYear;
            endDate += endYear;

        } else if (!Strings.isNullOrEmpty(startYear)) {

            startDate += startYear;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtil.getCurrentDateTime());
            endDate += String.valueOf(calendar.get(Calendar.YEAR));

        } else if (!Strings.isNullOrEmpty(endYear)) {
            endDate += endYear;
            startDate += String.valueOf(Integer.valueOf(endYear) - 10);
        }

        if (Strings.isNullOrEmpty(startMonth) && Strings.isNullOrEmpty(endMonth)) {

            startDate += "-1";
            endDate += "-12";

        } else if (!Strings.isNullOrEmpty(startMonth) && !Strings.isNullOrEmpty(endMonth)) {

            startDate += "-" + startMonth;
            endDate += "-" + endMonth;

        } else if (!Strings.isNullOrEmpty(startMonth)) {

            startDate += "-" + startMonth;
            endDate += "-12";

        } else if (!Strings.isNullOrEmpty(endMonth)) {
            startDate += "-1";
            endDate += "-" + endMonth;
        }

        int count = DateUtil.getMonthDiff(startDate, endDate);

        List<String> monthList = new ArrayList<String>();
        for (int i = 0; i <= count; i++) {
            String date = DateUtil.addMonthDate(startDate + "-1", i);

            String[] dates = date.split("/");

            monthList.add(String.format("%s-%s", dates[0], dates[1]));
        }

        return monthList;
    }
    public static int getMonthDiff(String startDate, String endDate){
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.setTime(stringToTimestamp(SHORT_DATE_YYYYMM_HYPHEN, startDate));
        endCalendar.setTime(stringToTimestamp(SHORT_DATE_YYYYMM_HYPHEN, endDate));
        int m = (endCalendar.get(Calendar.MONTH)) - (startCalendar.get(Calendar.MONTH));
        int y = (endCalendar.get(Calendar.YEAR)) - (startCalendar.get(Calendar.YEAR));
        return y * 12 + m;
    }

    public static int getNowYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static void main(String[] a) {
//		String str = "2012年01月17日";
//
//		System.out.println(DateUtil.stringToDate(DateUtil.SHORT_DATE_HANZI, str));
//		System.out.println(getNowYear());

        double d = 20.5;
        int i = 3;
        System.out.println(d * i);

    }
}
