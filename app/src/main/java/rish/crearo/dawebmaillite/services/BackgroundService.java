package rish.crearo.dawebmaillite.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.Collections;

import rish.crearo.dawebmaillite.asyncTasks.Login;
import rish.crearo.dawebmaillite.asyncTasks.LoginListener;
import rish.crearo.dawebmaillite.asyncTasks.RefreshInbox;
import rish.crearo.dawebmaillite.asyncTasks.RefreshInboxListener;
import rish.crearo.dawebmaillite.database.EmailMessage;
import rish.crearo.dawebmaillite.database.User;
import rish.crearo.dawebmaillite.utils.ConnectionManager;
import rish.crearo.dawebmaillite.utils.Constants;
import rish.crearo.dawebmaillite.utils.Printer;

public class BackgroundService extends Service implements RefreshInboxListener, LoginListener {
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
            if (!User.isLoggedIn())
                new Login(getApplicationContext(), this).execute();
            else
                new RefreshInbox(getApplicationContext(), this).execute();

        } else if ((ConnectionManager.isConnectedByWifi(this) == false && ConnectionManager.isConnectedByMobileData(this) == false)) {
            Printer.println("No need to check for mail");
        }
    }

    @Override
    public void onPreLogin() {
    }

    @Override
    public void onPostLogin(boolean loginSuccess, String timeTaken) {
        if (loginSuccess)
            new RefreshInbox(getApplicationContext(), this).execute();
    }

    @Override
    public void onPreRefresh() {

    }

    @Override
    public void onPostRefresh(boolean success, ArrayList<EmailMessage> refreshedEmails) {
        Collections.reverse(refreshedEmails);

        for (EmailMessage m : refreshedEmails)
            m.save(); // now all e-mails are in the database

        if (refreshedEmails.size() == 0)
            Printer.println("No new Webmails");
        else if (refreshedEmails.size() == 1)
            NotificationMaker.showNotification(this, "One New Webmail!", refreshedEmails.get(0).getFromName(), refreshedEmails.get(0).getSubject());
        else {
            int numberToShow = (refreshedEmails.size() >= 5) ? 5 : refreshedEmails.size();
            NotificationMaker.sendInboxNotification(numberToShow, this, refreshedEmails);
        }
    }
}