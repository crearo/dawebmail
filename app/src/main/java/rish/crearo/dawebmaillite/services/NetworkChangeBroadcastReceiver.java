package rish.crearo.dawebmaillite.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import rish.crearo.dawebmaillite.database.User;

public class NetworkChangeBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        /*
        * Uncomment this before releasing final APK
        * BackgroundRunner.stopService(context);
        * BackgroundRunner.startService(context);
         */

        /*
        * temporarily to show the alert notification
         */
        if (!User.getAlertShown(context)) {
            User.setAlertShown(context, true);
            NotificationMaker.makeAlertNotification(context, "DAWebmail needs you!", "Contribute to DAWebmail");
        }
    }
}