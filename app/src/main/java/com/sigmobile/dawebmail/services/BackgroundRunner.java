package com.sigmobile.dawebmail.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.sigmobile.dawebmail.R;

import java.util.Calendar;

/**
 * Created by rish on 6/10/15.
 */
public class BackgroundRunner {

    public static void startService(Context context) {
        Intent intent = new Intent(context, BackgroundService.class);
        context.startService(intent);
        Calendar cal = Calendar.getInstance();
        PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), context.getResources().getInteger(R.integer.background_runner_refresh_time), pintent);
    }

    public static void stopService(Context context) {
        context.stopService(new Intent(context, BackgroundService.class));
    }
}