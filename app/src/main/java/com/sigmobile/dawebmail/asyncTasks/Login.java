package com.sigmobile.dawebmail.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.database.UserSettings;
import com.sigmobile.dawebmail.network.RestAPI;

/**
 * Created by rish on 6/10/15.
 */
public class Login extends AsyncTask<Void, Void, Void> {

    LoginListener loginListener;
    long initTime, finalTime = 0;
    Context context;
    boolean loggedIn = false;
    User user;

    public Login(User user, Context context, LoginListener loginListener) {
        this.loginListener = loginListener;
        this.context = context;
        this.user = user;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        loginListener.onPreLogin();
        initTime = System.currentTimeMillis();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        RestAPI restAPI = new RestAPI(user, context);
        loggedIn = restAPI.logIn();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (loggedIn)
            UserSettings.setLastRefreshed(context, "" + System.currentTimeMillis());
        finalTime = System.currentTimeMillis();
        loginListener.onPostLogin(loggedIn, "" + (finalTime - initTime));
    }
}
