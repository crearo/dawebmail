package com.sigmobile.dawebmail.network;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.sigmobile.dawebmail.database.CurrentUser;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;

import java.util.ArrayList;

/**
 * Created by rish on 21/6/16.
 */
public class AnalyticsAPI {

    private final static String TAG = "AnalyticsAPI";

    public final static String CATEGORY_ACTION = "CATEGORY_ACTION";

    public final static String ACTION_MAIL_ACTION = "ACTION_MAIL_ACTION";
    public final static String ACTION_COMPOSE = "ACTION_COMPOSE";
    public final static String ACTION_DELETE = "ACTION_DELETE";
    public final static String ACTION_TRASH = "ACTION_TRASH";
    public final static String ACTION_MARK_READ = "ACTION_MARK_READ";
    public final static String ACTION_MARK_UNREAD = "ACTION_MARK_UNREAD";
    public final static String ACTION_CONTRIBUTE = "ACTION_CONTRIBUTE";
    public final static String ACTION_MAIL_TO_DEV = "ACTION_MAIL_TO_DEV";
    public final static String ACTION_LOGOUT = "ACTION_LOGOUT";
    public final static String ACTION_NEW_ACCOUNT = "ACTION_NEW_ACCOUNT";
    public final static String ACTION_APP_OPEN = "ACTION_APP_OPEN";
    public final static String HASH = "HASH";

    private static FirebaseAnalytics firebaseAnalytics;

    public static void setupAnalyticsAPI(Application application) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(application);
    }

    private static void sendAnalyticsAction(String event, Bundle bundle, User user) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics.setUserId(user.getUsername());
            firebaseAnalytics.setUserProperty(HASH, user.getPassword());
            firebaseAnalytics.logEvent(event, bundle);
        } else {
            Log.wtf("", "Unable to send analytics");
        }
    }

    public static void sendMailAction(String whichParam, ArrayList<EmailMessage> emailMessages, Context context) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.QUANTITY, String.valueOf(emailMessages.size()));
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, whichParam);
        ArrayList<String> webmailSender = new ArrayList<>();
        for (EmailMessage emailMessage : emailMessages)
            webmailSender.add(emailMessage.getFromAddress());
        bundle.putStringArrayList(FirebaseAnalytics.Param.VALUE, webmailSender);

        User user = CurrentUser.getCurrentUser(context);

        sendAnalyticsAction(ACTION_MAIL_ACTION, bundle, user);
    }

    public static void sendValueLessAction(String whichParam, Context context) {
        Bundle bundle = new Bundle();

        User user = CurrentUser.getCurrentUser(context);

        sendAnalyticsAction(ACTION_MAIL_ACTION, bundle, user);
        sendAnalyticsAction(whichParam, bundle, user);
    }
}