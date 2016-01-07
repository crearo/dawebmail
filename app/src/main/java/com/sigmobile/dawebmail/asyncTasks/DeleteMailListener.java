package com.sigmobile.dawebmail.asyncTasks;

/**
 * Created by rish on 6/10/15.
 */
public interface DeleteMailListener {

    void onPreDelete();

    void onPostDelete(boolean success);

}
