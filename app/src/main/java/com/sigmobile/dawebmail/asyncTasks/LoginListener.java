package com.sigmobile.dawebmail.asyncTasks;

/**
 * Created by rish on 6/10/15.
 */
public interface LoginListener {

    void onPreLogin();

    void onPostLogin(boolean loginSuccess, String timeTaken);

}
