package com.sigmobile.dawebmail.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.network.SoapAPI;

/**
 * Created by rish on 18/1/16.
 */
public class SendMail extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "SendMail";

    private SendMailListener sendMailListener;
    private Context context;
    private String mailSubject, mailContent, mailToAddress;
    private boolean important = false;
    private boolean result = false;
    private User currentUser;

    public SendMail(User user, SendMailListener sendMailListener, Context context, String mailSubject, String mailContent, String mailToAddress, boolean important) {
        this.sendMailListener = sendMailListener;
        this.context = context;
        this.mailSubject = mailSubject;
        this.mailContent = mailContent;
        this.mailToAddress = mailToAddress;
        this.important = important;
        this.currentUser = user;
    }

    @Override
    protected void onPreExecute() {
        sendMailListener.onPreSend();
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        result = new SoapAPI(context, currentUser).sendMail(mailToAddress, mailSubject, mailContent, important);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        sendMailListener.onPostSend(result);
        super.onPostExecute(aVoid);
    }
}