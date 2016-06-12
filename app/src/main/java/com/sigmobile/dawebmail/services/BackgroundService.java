package com.sigmobile.dawebmail.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.sigmobile.dawebmail.asyncTasks.RefreshInbox;
import com.sigmobile.dawebmail.asyncTasks.RefreshInboxListener;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.UserSettings;
import com.sigmobile.dawebmail.utils.ConnectionManager;
import com.sigmobile.dawebmail.utils.Constants;

import java.util.ArrayList;

public class BackgroundService extends Service implements RefreshInboxListener {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart(Intent intent, int startId) {
        for (User user : User.getAllUsers()) {
            refreshInboxInBackground(user);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void refreshInboxInBackground(User user) {
        SharedPreferences prefs = getSharedPreferences(Constants.USER_PREFERENCES, MODE_PRIVATE);

        boolean wifiEnabled = prefs.getBoolean(Constants.TOGGLE_WIFI, true);
        boolean dataEnabled = prefs.getBoolean(Constants.TOGGLE_MOBILEDATA, false);

        if (((wifiEnabled && ConnectionManager.isConnectedByWifi(this)) || (dataEnabled && ConnectionManager.isConnectedByMobileData(this)))) {
            new RefreshInbox(user, getApplicationContext(), this, Constants.INBOX).execute();
        } else if ((ConnectionManager.isConnectedByWifi(this) == false && ConnectionManager.isConnectedByMobileData(this) == false)) {
        }
    }

    @Override
    public void onPreRefresh() {
    }

    @Override
    public void onPostRefresh(boolean success, ArrayList<EmailMessage> refreshedEmails, User user) {
        if (refreshedEmails.size() == 0) {
        } else if (refreshedEmails.size() == 1) {
            NotificationMaker.showNotification(this, user, refreshedEmails.get(0).fromName, refreshedEmails.get(0).subject);
            UserSettings.setCurrentUser(user, getApplicationContext());
        } else {
            int numberToShow = (refreshedEmails.size() >= 5) ? 5 : refreshedEmails.size();
            NotificationMaker.sendInboxNotification(numberToShow, user, this, refreshedEmails);
            UserSettings.setCurrentUser(user, getApplicationContext());
        }
    }
}