package com.sigmobile.dawebmail.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.utils.Constants;

/**
 * Created by rish on 6/10/15.
 */
public class UserSettings {

    public static void setCurrentUser(User currentUser, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        if (currentUser == null) {
            editor.putString(Constants.CURRENT_USERNAME, null);
        } else {
            editor.putString(Constants.CURRENT_USERNAME, currentUser.username);
        }
        editor.apply();
        editor.commit();
    }

    public static User getCurrentUser(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = preferences.getString(Constants.CURRENT_USERNAME, null);
        if (username == null)
            return null;
        return User.getUserFromUserName(username);
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

    public static String getNotificationSound(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(Constants.NOTIFICATION_SOUND, ("android.resource://" + context.getPackageName() + "/" + R.raw.zoop));
    }

    public static void setNotificationSound(Context context, String uri) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.NOTIFICATION_SOUND, uri);
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