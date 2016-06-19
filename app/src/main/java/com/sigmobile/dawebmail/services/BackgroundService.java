package com.sigmobile.dawebmail.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.sigmobile.dawebmail.asyncTasks.RefreshInbox;
import com.sigmobile.dawebmail.asyncTasks.RefreshInboxListener;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.CurrentUser;
import com.sigmobile.dawebmail.utils.ConnectionManager;
import com.sigmobile.dawebmail.utils.Constants;
import com.sigmobile.dawebmail.utils.Settings;

import java.util.ArrayList;

public class BackgroundService extends Service implements RefreshInboxListener {

    private Settings settings;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart(Intent intent, int startId) {
        settings = new Settings(getApplicationContext());
        for (User user : User.getAllUsers()) {
            refreshInboxInBackground(user);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void refreshInboxInBackground(User user) {

        boolean dataEnabled = settings.getBoolean(Settings.KEY_MOBILE_DATA);

        if ((ConnectionManager.isConnectedByWifi(this) || (dataEnabled && ConnectionManager.isConnectedByMobileData(this)))) {
            // ToDo : Add data saving if check here.
            // It should not keep refreshing at night if connected to wifi
            new RefreshInbox(user, getApplicationContext(), this, Constants.INBOX, Constants.REFRESH_TYPE_REFRESH).execute();
        }
    }

    @Override
    public void onPreRefresh() {
    }

    @Override
    public void onPostRefresh(boolean success, ArrayList<EmailMessage> refreshedEmails, User user) {
        if (refreshedEmails.size() == 0) {
        } else if (refreshedEmails.size() == 1) {
            NotificationMaker.showNotification(this, user, refreshedEmails.get(0).getFromName(), refreshedEmails.get(0).getSubject());
            CurrentUser.setCurrentUser(user, getApplicationContext());
        } else {
            int numberToShow = (refreshedEmails.size() >= 5) ? 5 : refreshedEmails.size();
            NotificationMaker.sendInboxNotification(numberToShow, user, this, refreshedEmails);
            CurrentUser.setCurrentUser(user, getApplicationContext());
        }
    }
}