package com.sigmobile.dawebmail.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.scraper.RestAPI;
import com.sigmobile.dawebmail.utils.Constants;

import java.util.ArrayList;

/**
 * Created by rish on 6/10/15.
 */
public class RefreshInbox extends AsyncTask<Void, Void, Void> {

    RefreshInboxListener listener;
    Context context;
    long timeStarted = 0;
    long timeFinished = 0;
    boolean result = false;
    String username, pwd;
    ArrayList<EmailMessage> refreshedEmails;

    public RefreshInbox(Context context, RefreshInboxListener refreshInboxListener) {
        this.context = context;
        this.listener = refreshInboxListener;
        username = User.getUsername(context);
        pwd = User.getPassword(context);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        timeStarted = System.currentTimeMillis();
        listener.onPreRefresh();
    }

    @Override
    protected Void doInBackground(Void... voids) {
//        ScrapingMachine scrapper = new ScrapingMachine(username, pwd, context);
//        result = scrapper.scrapeAllMessagesfromInbox();
//        refreshedEmails = scrapper.getNewEmails();

        RestAPI restAPI = new RestAPI(username, pwd, context);
        restAPI.refresh(Constants.INBOX);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (result) {
            User.setLastRefreshed(context, "" + System.currentTimeMillis());
        }
        timeFinished = System.currentTimeMillis();
        listener.onPostRefresh(result, refreshedEmails);
    }
}
