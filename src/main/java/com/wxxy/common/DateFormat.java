package com.wxxy.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormat {
    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return sdf.format(date);
    }

    public static String convertToCronExpression(String dateTimeString) {
        try {
            // 解析时间字符串
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = dateFormat.parse(dateTimeString);

            // 获取时间的各个字段值
            int second = date.getSeconds();
            int minute = date.getMinutes();
            int hour = date.getHours();
            int dayOfMonth = date.getDate();
            int month = date.getMonth() + 1; // 月份是从 0 开始的，需要加 1
            int year = date.getYear() + 1900; // 年份需要加上 1900

            // 构造 Cron 表达式
            String cronExpression = String.format("%d %d %d %d %d ? %d", second, minute, hour, dayOfMonth, month, year);
            return cronExpression;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
