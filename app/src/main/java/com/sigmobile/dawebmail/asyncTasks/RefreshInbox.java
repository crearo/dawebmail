package com.sigmobile.dawebmail.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.UserSettings;
import com.sigmobile.dawebmail.network.RestAPI;

import java.util.ArrayList;

/**
 * Created by rish on 6/10/15.
 */
public class RefreshInbox extends AsyncTask<Void, Void, Void> {

    private RefreshInboxListener listener;
    private Context context;
    private ArrayList<EmailMessage> refreshedEmails;
    private String REFRESH_TYPE;
    private User user;

    public RefreshInbox(User user, Context context, RefreshInboxListener refreshInboxListener, String REFRESH_TYPE) {
        this.context = context;
        this.listener = refreshInboxListener;
        this.REFRESH_TYPE = REFRESH_TYPE;
        this.user = user;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onPreRefresh();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        // TODO : Remove emails that dont exist here.

        RestAPI restAPI = new RestAPI(user, context);
        restAPI.refresh(REFRESH_TYPE);
        refreshedEmails = restAPI.getNewEmails();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        /*
        *This should be changed! Right now every webmail that is refreshed is considered as a new refresh.
        * Even ones that fail. I need a way to check if we were able to return successfully, and then save the last refreshed.
         */
        boolean complete = false;
        if (refreshedEmails != null) {
            UserSettings.setLastRefreshed(context, "" + System.currentTimeMillis());
            complete = true;
        }
        listener.onPostRefresh(complete, refreshedEmails, user);
    }
}