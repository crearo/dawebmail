package com.sigmobile.dawebmail.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import com.sigmobile.dawebmail.utils.Printer;

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

        // refresh every 15 minutes
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 15 * 60 * 1000, pintent);
        Printer.println("Setting AlarmManager");
    }

    public static void stopService(Context context) {
        context.stopService(new Intent(context, BackgroundService.class));
    }
}