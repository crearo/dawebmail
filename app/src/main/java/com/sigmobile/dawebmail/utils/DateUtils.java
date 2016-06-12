package com.sigmobile.dawebmail.utils;

import android.content.Context;

import com.sigmobile.dawebmail.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by rish on 8/1/16.
 */
public class DateUtils {
    public static String getDate(Context context, long milliSeconds) {

        int ONE_HOUR = 1000 * 60 * 60;
        int TWO_HOURS = 1000 * 60 * 60 * 2;

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        String time = formatter.format(calendar.getTime());
        long currentTimeInMillis = System.currentTimeMillis();
        if ((currentTimeInMillis - milliSeconds) <= ONE_HOUR) {
            return context.getString(R.string.recent_webmails) + time;
        } else if ((currentTimeInMillis - milliSeconds) >= ONE_HOUR && (currentTimeInMillis - milliSeconds) <= TWO_HOURS) {
            return context.getString(R.string.hour_ago_webmails) + time;
        }

        formatter = new SimpleDateFormat("dd/MM/yy");
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}