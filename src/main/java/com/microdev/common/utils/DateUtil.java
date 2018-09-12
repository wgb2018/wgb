package com.microdev.common.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * @author liutf
 */
public class DateUtil {
    public static OffsetDateTime nowBeiJingDateTime() {
        return OffsetDateTime.now(Clock.system(ZoneId.of("Asia/Shanghai")));
    }

    /**
     * 按照指定的格式格式化时间字符串 如果没有传入字符串，那么按照指定的格式格式化当前时间
     *
     * @param pattern 格式模样,如：yyyy-MM-dd HH:mm:ss
     * @param dateStr 要格式的时间字符串
     * @return
     */
    public static Date toDate(String pattern, String dateStr) {
        DateFormat sdf = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 日期格式化为字符串
     *
     * @param date    要格式化的日期
     * @param pattern 日期的格式，如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String toStr(String pattern, Date date) {
        try {
            return new SimpleDateFormat(pattern).format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static int caculateAge(Timestamp time) {

        if (time == null) return 0;
        Calendar oldDay = Calendar.getInstance();
        Calendar newDay = Calendar.getInstance();
        oldDay.setTime(time);
        newDay.setTime(new Date());
        int oldYear = oldDay.get(Calendar.YEAR);
        int newYear = newDay.get(Calendar.YEAR);
        oldDay.set(Calendar.YEAR, newYear);
        if (oldDay.get(Calendar.DAY_OF_YEAR) > newDay.get(Calendar.DAY_OF_YEAR)) {
            return newYear - oldYear - 1 < 0 ? 0 : newYear - oldYear - 1;
        } else {
            return newYear - oldYear;
        }
    }
    /**
     * 根据日期计算年龄
     *
     */

    public static Integer CaculateAge(OffsetDateTime dateTime){
        if(dateTime==null){
            return 0;
        }
        OffsetDateTime nowDate=OffsetDateTime.now();
        int oldYear = dateTime.getYear();
        int nowYear = nowDate.getYear();
        if (dateTime.getDayOfYear() > nowDate.getDayOfYear()) {
            return nowYear - oldYear <= 0 ? 0 : nowYear - oldYear - 1;
        } else {
            return nowYear - oldYear;
        }

    }

    /**
     * 将字符类型的字符串转换成OffsetDateTime
     * @param str
     * @return
     */
    public static OffsetDateTime strToOffSetDateTime(String str, String pattern) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(pattern);

        Date d = format.parse(str);
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(d.getTime()), ZoneId.systemDefault());
    }


}
