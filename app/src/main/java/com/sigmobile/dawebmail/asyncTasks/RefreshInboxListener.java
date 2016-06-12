package com.sigmobile.dawebmail.asyncTasks;

import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;

import java.util.ArrayList;

/**
 * Created by rish on 6/10/15.
 */
public interface RefreshInboxListener {

    void onPreRefresh();

    void onPostRefresh(boolean success, ArrayList<EmailMessage> refreshedEmails, User user);

}
