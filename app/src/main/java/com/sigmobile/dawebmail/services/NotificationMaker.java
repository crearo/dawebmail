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

import com.sigmobile.dawebmail.ContributeActivity;
import com.sigmobile.dawebmail.LoginActivity;
import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.UserSettings;

import java.util.ArrayList;


/**
 * Created by rish on 6/10/15.
 */
public class NotificationMaker {

    public static void showNotification(Context context, String msgnumber, String sendername, String subject) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.icon_final);
        mBuilder.setContentTitle(sendername);
        mBuilder.setContentText(subject);
        mBuilder.setSound(Uri.parse(UserSettings.getNotificationSound(context)));
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
        mBuilder.setAutoCancel(true);

        Intent notificationintent = new Intent(context, LoginActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(LoginActivity.class);

        stackBuilder.addNextIntent(notificationintent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(1002, mBuilder.build());
    }

    public static void sendInboxNotification(int numberToShow, Context context, ArrayList<EmailMessage> newEmails) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, LoginActivity.class), 0);
            Notification.Builder mBuilder = new Notification.Builder(context)
                    .setContentTitle("DAWebmails")
                    .setContentText("Swipe down to view webmails!")
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.icon_final);

            Notification.InboxStyle notification1 = null;

            notification1 = new Notification.InboxStyle(mBuilder);

            for (int i = 0; i < numberToShow; i++) {
                String emailFrom = newEmails.get(newEmails.size() - 1 - i).fromName;
                String emailSubject = newEmails.get(newEmails.size() - 1 - i).subject;
                String htmlText = "<b>" + emailFrom + "</b> " + emailSubject;
                notification1.addLine(Html.fromHtml(htmlText));
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

            mNotificationManager.notify(1003, mBuilder.build());
        }
    }

    public static void cancelNotification(Context context) {
        NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

    public static void makeAlertNotification(Context context, String title, String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.icon_final);
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
        mBuilder.setContentTitle(title);
        mBuilder.setTicker(title);
        mBuilder.setSound(Uri.parse(UserSettings.getNotificationSound(context)));
        mBuilder.setContentText(message);
        mBuilder.setAutoCancel(true);

        Intent notificationintent = new Intent(context, ContributeActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ContributeActivity.class);

        stackBuilder.addNextIntent(notificationintent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(1004, mBuilder.build());
    }
}