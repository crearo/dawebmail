package com.sigmobile.dawebmail.asyncTasks;

/**
 * Created by rish on 12/6/16.
 */
public interface MailActionListener {

    void onPreMailAction();

    void onPostMailAction(boolean success);

}
