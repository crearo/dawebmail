package com.sigmobile.dawebmail.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sigmobile.dawebmail.R;

/**
 * Created by rish on 19/6/16.
 */
public class Settings {

    private Context context;
    private SharedPreferences prefs;

    public final static String KEY_MOBILE_DATA = "KEY_MOBILE_DATA";
    public final static String KEY_NOTIFICATION_SOUND = "KEY_NOTIFICATION_SOUND";
    public final static String KEY_LAST_REFRESHED = "KEY_LAST_REFRESHED";
    public final static String KEY_ALERT_SHOWN = "KEY_ALERT_SHOWN_2";
    public final static String KEY_UPDATE_SHOWN = "KEY_UPDATE_SHOWN_2";
    public final static String KEY_DATABASE_CREATED = "KEY_DATABASE_CREATED";

    public Settings(Context context) {
        this.context = context;
    }

    private SharedPreferences sharedPreferences() {
        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return prefs;
    }

    public void save(String name, boolean value) {
        sharedPreferences().edit().putBoolean(name, value).commit();
    }

    public void save(String name, String value) {
        sharedPreferences().edit().putString(name, value).commit();
    }

    public boolean getBoolean(String name) {
        if (name.equals(KEY_MOBILE_DATA))
            return sharedPreferences().getBoolean(KEY_MOBILE_DATA, true);
        else if (name.equals(KEY_ALERT_SHOWN))
            return sharedPreferences().getBoolean(KEY_ALERT_SHOWN, false);
        else if (name.equals(KEY_UPDATE_SHOWN))
            return sharedPreferences().getBoolean(KEY_UPDATE_SHOWN, false);
        else if (name.equals(KEY_DATABASE_CREATED))
            return sharedPreferences().getBoolean(KEY_DATABASE_CREATED, false);
        else
            return false;
    }

    public String getString(String name) {
        if (name.equals(KEY_NOTIFICATION_SOUND))
            return sharedPreferences().getString(KEY_NOTIFICATION_SOUND, ("android.resource://" + context.getPackageName() + "/" + R.raw.zoop));
        else if (name.equals(KEY_LAST_REFRESHED))
            return sharedPreferences().getString(KEY_LAST_REFRESHED, "");
        else
            return "";
    }
}