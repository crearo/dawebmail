package com.sigmobile.dawebmail.network;

import android.app.Application;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sigmobile.dawebmail.DAWebmailApplication;

/**
 * Created by rish on 21/6/16.
 */
public class AnalyticsAPI {

    public final static String CATEGORY_ACTION = "CATEGORY_ACTION";

    public final static String ACTION_COMPOSE = "ACTION_COMPOSE";
    public final static String ACTION_DELETE = "ACTION_DELETE";
    public final static String ACTION_TRASH = "ACTION_TRASH";
    public final static String ACTION_MARK_READ = "ACTION_MARK_READ";
    public final static String ACTION_MARK_UNREAD = "ACTION_MARK_UNREAD";
    public final static String ACTION_CONTRIBUTE = "ACTION_CONTRIBUTE";
    public final static String ACTION_MAIL_TO_DEV = "ACTION_MAIL_TO_DEV";
    public final static String ACTION_LOGOUT = "ACTION_LOGOUT";
    public final static String ACTION_NEW_ACCOUNT = "ACTION_NEW_ACCOUNT";

    public static void sendAnalyticsAction(Application application, String eventCategory, String eventAction) {
        DAWebmailApplication mApplication = (DAWebmailApplication) application;
        Tracker mTracker = mApplication.getDefaultTracker();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(eventCategory)
                .setAction(eventAction)
                .build());
    }
}
