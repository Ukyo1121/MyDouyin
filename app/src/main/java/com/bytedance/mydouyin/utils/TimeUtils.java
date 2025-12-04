package com.bytedance.mydouyin.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    /**
     * 将时间戳转换为显示字符串
     * @param timeMillis 消息的时间戳 (毫秒)
     * @return 格式化后的时间字符串
     */
    public static String getFriendlyTimeSpanByNow(long timeMillis) {
        long now = System.currentTimeMillis();
        long span = now - timeMillis;

        // 1分钟内：刚刚
        if (span < 60 * 1000) {
            return "刚刚";
        }

        // 1小时内：xx分钟前
        if (span < 60 * 60 * 1000) {
            return (span / 1000 / 60) + "分钟前";
        }

        // 获取当天 00:00 的时间戳，用于判断是不是今天
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long todayZero = calendar.getTimeInMillis();

        // 今天：HH:mm
        if (timeMillis >= todayZero) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(new Date(timeMillis));
        }

        // 昨天：昨天 HH:mm
        long yesterdayZero = todayZero - 24 * 60 * 60 * 1000;
        if (timeMillis >= yesterdayZero) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return "昨天 " + sdf.format(new Date(timeMillis));
        }

        // 7天内：x天前
        long sevenDaysAgo = todayZero - 7 * 24 * 60 * 60 * 1000;
        if (timeMillis >= sevenDaysAgo) {
            long days = (todayZero - timeMillis) / (24 * 60 * 60 * 1000) + 1;
            return days + "天前";
        }

        // 其他：MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }
}