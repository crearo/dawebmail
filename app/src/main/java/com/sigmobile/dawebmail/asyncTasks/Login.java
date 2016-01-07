package com.sigmobile.dawebmail.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.network.RestAPI;

/**
 * Created by rish on 6/10/15.
 */
public class Login extends AsyncTask<Void, Void, Void> {

    LoginListener loginListener;
    String username, pwd;
    long initTime, finalTime = 0;
    Context context;
    boolean loggedIn = false;

    public Login(Context context, LoginListener loginListener) {
        this.loginListener = loginListener;
        this.context = context;
        username = User.getUsername(context);
        pwd = User.getPassword(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        loginListener.onPreLogin();
        initTime = System.currentTimeMillis();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        RestAPI restAPI = new RestAPI(username, pwd, context);
        loggedIn = restAPI.logIn();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (loggedIn)
            User.setLastRefreshed(context, "" + System.currentTimeMillis());
        User.setIsLoggedIn(loggedIn);
        finalTime = System.currentTimeMillis();
        loginListener.onPostLogin(loggedIn, "" + (finalTime - initTime));
    }
}
