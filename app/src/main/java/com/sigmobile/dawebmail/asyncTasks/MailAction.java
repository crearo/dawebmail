package com.sigmobile.dawebmail.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.network.SoapAPI;

/**
 * Created by rish on 12/6/16.
 */
public class MailAction extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "MailAction";

    MailActionListener mailActionListener;
    Context context;
    boolean result = false;
    User currentUser;
    String mailAction;
    String contentID;

    public MailAction(Context context, User currentUser, String mailAction, String contentID, MailActionListener mailActionListener) {
        this.mailActionListener = mailActionListener;
        this.context = context;
        this.currentUser = currentUser;
        this.mailAction = mailAction;
        this.contentID = contentID;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mailActionListener.onPreMailAction();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        result = new SoapAPI().performMailAction(currentUser, mailAction, contentID);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mailActionListener.onPostMailAction(result);
    }
}