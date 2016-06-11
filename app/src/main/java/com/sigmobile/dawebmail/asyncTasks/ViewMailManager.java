package com.sigmobile.dawebmail.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.network.RestAPI;

/**
 * Created by rish on 6/10/15.
 */
public class ViewMailManager extends AsyncTask<Void, Void, Void> {

    Context context;
    ViewMailListener viewMailListener;
    boolean result = false;
    EmailMessage emailMessage;
    User user;

    public ViewMailManager(User user, Context context, ViewMailListener viewMailListener, EmailMessage emailMessage) {
        this.viewMailListener = viewMailListener;
        this.context = context;
        this.emailMessage = emailMessage;
        this.user = user;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        viewMailListener.onPreView();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        RestAPI restAPI = new RestAPI(user, context);
        emailMessage = restAPI.fetchEmailContent(emailMessage);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        viewMailListener.onPostView(emailMessage);
    }
}