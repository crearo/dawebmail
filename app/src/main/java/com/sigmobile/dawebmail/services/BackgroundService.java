package com.sigmobile.dawebmail.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.sigmobile.dawebmail.asyncTasks.RefreshInbox;
import com.sigmobile.dawebmail.asyncTasks.RefreshInboxListener;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.utils.ConnectionManager;
import com.sigmobile.dawebmail.utils.Constants;
import com.sigmobile.dawebmail.utils.Printer;

import java.util.ArrayList;

public class BackgroundService extends Service implements RefreshInboxListener {
    String username, pwd;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Printer.println("Created Service");
    }

    @Override
    public void onStart(Intent intent, int startId) {

        Printer.println("Started Service");
        username = User.getUsername(getApplicationContext());
        pwd = User.getPassword(getApplicationContext());

        Printer.println("SERVICE USERNAME " + username);
        refreshInbox_BroadcastFunction();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void refreshInbox_BroadcastFunction() {
        SharedPreferences prefs = getSharedPreferences(Constants.USER_PREFERENCES, MODE_PRIVATE);

        boolean wifiEnabled = prefs.getBoolean(Constants.TOGGLE_WIFI, true);
        boolean dataEnabled = prefs.getBoolean(Constants.TOGGLE_MOBILEDATA, false);

        if (((wifiEnabled && ConnectionManager.isConnectedByWifi(this)) || (dataEnabled && ConnectionManager.isConnectedByMobileData(this)))) {
            new RefreshInbox(getApplicationContext(), this, Constants.INBOX).execute();

        } else if ((ConnectionManager.isConnectedByWifi(this) == false && ConnectionManager.isConnectedByMobileData(this) == false)) {
            Printer.println("No need to check for mail");
        }
    }

    @Override
    public void onPreRefresh() {
    }

    @Override
    public void onPostRefresh(boolean success, ArrayList<EmailMessage> refreshedEmails) {
        if (refreshedEmails.size() == 0)
            Printer.println("No new Webmails");
        else if (refreshedEmails.size() == 1)
            NotificationMaker.showNotification(this, "One New Webmail!", refreshedEmails.get(0).fromName, refreshedEmails.get(0).subject);
        else {
            int numberToShow = (refreshedEmails.size() >= 5) ? 5 : refreshedEmails.size();
            NotificationMaker.sendInboxNotification(numberToShow, this, refreshedEmails);
        }
    }
}