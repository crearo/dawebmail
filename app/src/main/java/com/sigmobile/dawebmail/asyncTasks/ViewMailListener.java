package com.sigmobile.dawebmail.asyncTasks;

import com.sigmobile.dawebmail.database.EmailMessage;

/**
 * Created by rish on 6/10/15.
 */
public interface ViewMailListener {

    void onPreView();

    void onPostView(EmailMessage emailMessage);

}
