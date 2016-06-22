package com.sigmobile.dawebmail;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.orm.SugarApp;
import com.sigmobile.dawebmail.network.AnalyticsAPI;

/**
 * Created by rish on 21/6/16.
 */

public class DAWebmailApplication extends SugarApp {
    @Override
    public void onCreate() {
        super.onCreate();
        AnalyticsAPI.setupAnalyticsAPI(getApplicationContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}