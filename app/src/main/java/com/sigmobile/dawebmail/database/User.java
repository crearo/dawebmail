package com.sigmobile.dawebmail.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sigmobile.dawebmail.utils.Constants;
import com.sigmobile.dawebmail.utils.Printer;

/**
 * Created by rish on 6/10/15.
 */
public class User {

    private static boolean loggedIn = false;
    private static String lastRefreshed = "NEVER";
    private boolean alertShown = false;

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static void setIsLoggedIn(boolean b) {
        loggedIn = b;
    }

    public static void setUsername(String username, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.USERNAME, username);
        editor.apply();
        editor.commit();
    }

    public static String getUsername(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String name = preferences.getString(Constants.USERNAME, null);
        Printer.println("USERNAME  = " + name);
        return name;
    }


    public static void setPassword(String pwd, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PASSWORD, pwd);
        editor.apply();
        editor.commit();
    }

    public static String getPassword(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(Constants.PASSWORD, null);
    }

    public static String getLastRefreshed(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(Constants.LAST_REFRESHED, null);
    }

    public static void setLastRefreshed(Context context, String lastRefreshed) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.LAST_REFRESHED, lastRefreshed);
        editor.apply();
        editor.commit();
    }

    public static boolean getAlertShown(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(Constants.ALERT_SHOWN, false);
    }

    public static void setAlertShown(Context context, boolean alertShown) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.ALERT_SHOWN, alertShown);
        editor.apply();
        editor.commit();
    }
}