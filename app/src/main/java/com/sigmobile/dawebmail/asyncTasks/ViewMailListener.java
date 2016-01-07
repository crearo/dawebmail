package com.sigmobile.dawebmail.asyncTasks;

/**
 * Created by rish on 6/10/15.
 */
public interface ViewMailListener {

    void onPreView();

    void onPostView(boolean success);

}
