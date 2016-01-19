package com.sigmobile.dawebmail.asyncTasks;

/**
 * Created by rish on 18/1/16.
 */
public interface SendMailListener {

    void onPreSend();

    void onPostSend(boolean success);
}
