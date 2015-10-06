package rish.crearo.dawebmaillite.asyncTasks;

import java.util.ArrayList;

import rish.crearo.dawebmaillite.database.EmailMessage;

/**
 * Created by rish on 6/10/15.
 */
public interface RefreshInboxListener {

    void onPreRefresh();

    void onPostRefresh(boolean success, ArrayList<EmailMessage> refreshedEmails);

}
