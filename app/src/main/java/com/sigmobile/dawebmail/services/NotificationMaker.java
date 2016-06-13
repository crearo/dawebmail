package com.sigmobile.dawebmail.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.text.Html;

import com.sigmobile.dawebmail.LoginActivity;
import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.UserSettings;

import java.util.ArrayList;

/**
 * Created by rish on 6/10/15.
 */
public class NotificationMaker {

    public static void showNotification(Context context, User user, String fromName, String subject) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.envelope_m);
        mBuilder.setTicker(context.getString(R.string.notification_ticker_new_webmail));
        String username = user.username;
        if (User.getUsersCount() > 1) {
            if (username.indexOf("@") != -1)
                mBuilder.setContentTitle(fromName + " to " + username.substring(0, username.indexOf("@")));
            else
                mBuilder.setContentTitle(fromName + " to " + username);
        } else {
            mBuilder.setContentTitle(fromName + " to " + username);
        }
        mBuilder.setContentText(subject);
        mBuilder.setSound(Uri.parse(UserSettings.getNotificationSound(context)));
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
        mBuilder.setAutoCancel(true);

        Intent notificationIntent = new Intent(context, LoginActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(LoginActivity.class);

        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify((int) (System.currentTimeMillis()), mBuilder.build());

        UserSettings.setCurrentUser(user, context);
    }

    public static void sendInboxNotification(int numberToShow, User user, Context context, ArrayList<EmailMessage> newEmails) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, LoginActivity.class), 0);
            Notification.Builder mBuilder = new Notification.Builder(context)
                    .setContentTitle(context.getString(R.string.notification_ticker_new_webmails))
                    .setTicker(context.getString(R.string.notification_ticker_new_webmails))
                    .setContentText(context.getString(R.string.notification_swipe_to_view))
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.envelope_m);

            if (User.getUsersCount() > 1) {
                String username = user.username;
                if (username.indexOf("@") != -1)
                    mBuilder.setContentTitle(context.getString(R.string.notification_ticker_new_webmails) + " for " + username.substring(0, username.indexOf("@")));
                else
                    mBuilder.setContentTitle(context.getString(R.string.notification_ticker_new_webmails) + " for " + username);
            }

            Notification.InboxStyle notification = null;
            notification = new Notification.InboxStyle(mBuilder);

            for (int i = 0; i < numberToShow; i++) {
                String emailFrom = newEmails.get(newEmails.size() - 1 - i).fromName;
                String emailSubject = newEmails.get(newEmails.size() - 1 - i).subject;
                String htmlText = "<b>" + emailFrom + "</b> " + emailSubject;
                notification.addLine(Html.fromHtml(htmlText));
            }

            Intent resultIntent = new Intent(context, LoginActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(LoginActivity.class);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setSound(Uri.parse(UserSettings.getNotificationSound(context)));
            mBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());

            UserSettings.setCurrentUser(user, context);
        }
    }

    public static void cancelNotification(Context context) {
        NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }
}