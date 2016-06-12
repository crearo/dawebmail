package com.sigmobile.dawebmail.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.network.SoapAPI;

import java.util.ArrayList;

/**
 * Created by rish on 6/10/15.
 */
public class DeleteMail extends AsyncTask<Void, Void, Void> {

    private Context context;
    private DeleteMailListener deleteMailListener;
    private Boolean result = false;
    private ArrayList<EmailMessage> emailToBeDeleted;
    private User currentUser;

    public DeleteMail(User user, Context context, DeleteMailListener deleteMailListener, ArrayList<EmailMessage> emailsToBeDeleted) {
        this.deleteMailListener = deleteMailListener;
        this.emailToBeDeleted = emailsToBeDeleted;
        this.context = context;
        this.currentUser = user;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        deleteMailListener.onPreDelete();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        SoapAPI soapAPI = new SoapAPI();
        /**
         * Traverse through all emailsToBeDeleted
         * If any of them is unsuccessful, return false
         */
        for (EmailMessage emailMessage : emailToBeDeleted) {
            result = soapAPI.performMailAction(context, currentUser, context.getString(R.string.msg_action_trash), String.valueOf(emailMessage.contentID));
            if (!result)
                return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        deleteMailListener.onPostDelete(result);
    }
}
