package com.sigmobile.dawebmail.asyncTasks;

/**
 * Created by rish on 6/10/15.
 */
public interface MultiMailActionListener {

    void onPreMultiMailAction();

    void onPostMultiMailAction(boolean success, String mailAction);

}
