package com.sigmobile.dawebmail.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by rish on 6/10/15.
 */
public class CurrentUser {

    /**
     * Only a single user can be active at once.
     * Kept here as a separate class on purpose - do not merge with User or Settings.
     */

    private static String CURRENT_USERNAME = "CURRENT_USER";

    public static void setCurrentUser(User currentUser, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        if (currentUser == null) {
            editor.putString(CURRENT_USERNAME, null);
        } else {
            editor.putString(CURRENT_USERNAME, currentUser.getUsername());
        }
        editor.apply();
        editor.commit();
    }

    public static User getCurrentUser(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = preferences.getString(CURRENT_USERNAME, null);
        if (username == null)
            return null;
        return User.getUserFromUserName(username);
    }
}