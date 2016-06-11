package com.sigmobile.dawebmail.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.sigmobile.dawebmail.utils.Printer;

import java.util.Calendar;

/**
 * Created by rish on 6/10/15.
 */
public class BackgroundRunner {

    //    int TIME_REFRESH = 15 * 60 * 1000;
    private static int TIME_REFRESH = 20 * 1000;

    public static void startService(Context context) {

        Intent intent = new Intent(context, BackgroundService.class);

        context.startService(intent);
        Calendar cal = Calendar.getInstance();
        PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // refresh every 15 minutes
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), TIME_REFRESH, pintent);
        Printer.println("Setting AlarmManager");
    }

    public static void stopService(Context context) {
        context.stopService(new Intent(context, BackgroundService.class));
    }
}