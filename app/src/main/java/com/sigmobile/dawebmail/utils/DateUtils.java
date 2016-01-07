package com.sigmobile.dawebmail.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by rish on 8/1/16.
 */
public class DateUtils {
    public static String getDate(long milliSeconds) {

        int JUST_NOW = 1000 * 60 * 5;
        int ONE_HOUR = 1000 * 60 * 60;
        int ONE_DAY = 1000 * 60 * 60 * 24;

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        String time = formatter.format(calendar.getTime());
        if (System.currentTimeMillis() - milliSeconds <= JUST_NOW) {
            return "Just Now at " + time;
        } else if (System.currentTimeMillis() - milliSeconds <= ONE_HOUR) {
            return "One Hour Ago at " + time;
        }

        formatter = new SimpleDateFormat("dd/MM/yy");
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
