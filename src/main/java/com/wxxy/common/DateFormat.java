package com.wxxy.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormat {
    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return sdf.format(date);
    }
}
