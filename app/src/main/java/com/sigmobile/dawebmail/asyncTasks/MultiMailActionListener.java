package com.sigmobile.dawebmail.asyncTasks;

import com.sigmobile.dawebmail.database.EmailMessage;

import java.util.ArrayList;

/**
 * Created by rish on 6/10/15.
 */
public interface MultiMailActionListener {

    void onPreMultiMailAction();

    void onPostMultiMailAction(boolean success, String mailAction, ArrayList<EmailMessage> emailsForMultiAction);

}
