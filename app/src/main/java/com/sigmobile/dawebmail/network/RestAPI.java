package com.sigmobile.dawebmail.network;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.sigmobile.dawebmail.R;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.database.User;
import com.sigmobile.dawebmail.utils.BasePath;
import com.sigmobile.dawebmail.utils.Constants;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by rish on 7/1/16.
 */
public class RestAPI {

    private static final String LOGTAG = "RESTAPI";

    private static final int TIME_OUT = 10 * 1000;
    private User user;
    private Context context;
    private ArrayList<EmailMessage> allNewEmails = new ArrayList<>();

    public RestAPI(User user, Context context) {
        this.user = user;
        this.context = context;
    }

    public boolean logIn() {
        return makeLoginRequest();
    }

    public boolean refresh(String TYPE) {
        if (TYPE.equals(Constants.INBOX)) {
            return handleRefreshRequest(Constants.INBOX);
        } else if (TYPE.equals(Constants.SENT)) {
            return handleRefreshRequest(Constants.SENT);
        } else if (TYPE.equals(Constants.TRASH)) {
            return handleRefreshRequest(Constants.TRASH);
        }
        return false;
    }

    public EmailMessage fetchEmailContent(EmailMessage emailMessage) {
        return makeFetchRequest(emailMessage);
    }

    private boolean makeLoginRequest() {
        try {
            URL url = new URL(context.getString(R.string.rest_url_login));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            String userPassword = user.username + ":" + user.password;
            String encoding = Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT);
            conn.setRequestProperty("Authorization", "Basic " + encoding);
            conn.setReadTimeout(TIME_OUT);
            conn.connect();

            Log.d(LOGTAG, "Response Code: " + conn.getResponseCode());
            if (conn.getResponseCode() == 200) {
                Log.d(LOGTAG, "Authenticated User Successfully");
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                Log.d(LOGTAG, "" + total.toString());
                in.close();
                return true;
            } else {
                Log.d(LOGTAG, "Unable to Authenticate User");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean handleRefreshRequest(String REFRESH_TYPE) {
        allNewEmails = new ArrayList<>();
        URL url = null;
        try {
            if (REFRESH_TYPE.equals(Constants.INBOX))
                url = new URL(context.getString(R.string.rest_url_inbox));
            else if (REFRESH_TYPE.equals(Constants.SENT))
                url = new URL(context.getString(R.string.rest_url_sent));
            else if (REFRESH_TYPE.equals(Constants.TRASH))
                url = new URL(context.getString(R.string.rest_url_trash));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            String userPassword = user.username + ":" + user.password;
            String encoding = Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT);
            conn.setRequestProperty("Authorization", "Basic " + encoding);
            conn.setReadTimeout(TIME_OUT);
            conn.connect();

            if (conn.getResponseCode() == 200) {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                in.close();

                JSONObject responseObject = new JSONObject(total.toString());

                for (int i = 0; i < responseObject.getJSONArray("m").length(); i++) {
                    JSONObject webmailObject = (JSONObject) responseObject.getJSONArray("m").get(i);
                    int contentID = Integer.parseInt(webmailObject.getString("id"));
                    int totalAttachments = 0;
                    String fromName = "fromName";
                    String fromAddress = "fromAddress";
                    String subject = webmailObject.getString("su");
                    String readUnread = Constants.WEBMAIL_READ;
                    boolean important = false;
                    if (webmailObject.has("f")) {
                        if (webmailObject.getString("f").contains("u"))
                            readUnread = Constants.WEBMAIL_UNREAD;
                        if (webmailObject.getString("f").contains("a"))
                            totalAttachments = 1;
                        if (webmailObject.getString("f").contains("!"))
                            important = true;
                        else
                            important = false;
                    }
                    String dateInMillis = webmailObject.getString("d");

                    for (int j = 0; j < webmailObject.getJSONArray("e").length(); j++) {
                        JSONObject fromToObject = (JSONObject) webmailObject.getJSONArray("e").get(j);
                        if (fromToObject.getString("t").equals("f")) {
                            fromAddress = fromToObject.getString("a");
                            if (fromToObject.has("p"))
                                fromName = fromToObject.getString("p");
                            else
                                fromName = fromToObject.getString("d");
                        }
                    }

                    if (REFRESH_TYPE.equals(Constants.INBOX)) {
                        EmailMessage latestWebmail = EmailMessage.getLatestWebmailOfUser(user);
                        if (latestWebmail != null && contentID == latestWebmail.contentID) {
                            break;
                        } else {
                            EmailMessage emailMessage = EmailMessage.getEmailMessageFromContentID(contentID);
                            if (emailMessage != null) {
                                EmailMessage.updateExistingEmailMessage(user, emailMessage, contentID, fromName, fromAddress, subject, dateInMillis, readUnread, totalAttachments, important);
                            } else {
                                emailMessage = EmailMessage.createNewEmailMessage(user, contentID, fromName, fromAddress, subject, dateInMillis, readUnread, totalAttachments, important);
                                allNewEmails.add(emailMessage);
                            }
                        }
                    } else {
                        EmailMessage emailMessage = new EmailMessage(user.username, contentID, fromName, fromAddress, subject, dateInMillis, readUnread, "", totalAttachments, important);
                        allNewEmails.add(emailMessage);
                    }
                }
                return true;
            } else {
                Log.d(LOGTAG, "Unable to Authenticate User");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private EmailMessage makeFetchRequest(EmailMessage emailMessage) {
        try {
            URL url = new URL(context.getString(R.string.rest_url_view_webmail) + emailMessage.contentID);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            String userPassword = user.username + ":" + user.password;
            String encoding = Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT);
            conn.setRequestProperty("Authorization", "Basic " + encoding);
            conn.setReadTimeout(TIME_OUT);
            conn.connect();

            if (conn.getResponseCode() == 200) {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line + "\n");
                }
                in.close();

                writeStringAsFile(total.toString());

                MailParser mailParser = new MailParser();
                mailParser.newMailParser(emailMessage.contentID, total.toString());
                emailMessage.content = mailParser.getContentHTML();
                emailMessage.totalAttachments = mailParser.getTotalAttachments();

                return emailMessage;
            } else {
                Log.d(LOGTAG, "Unable to Authenticate User");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeStringAsFile(final String fileContents) {
        try {
            FileWriter out = new FileWriter(new File(BasePath.getBasePath(), "email.txt"));
            out.write(fileContents);
            out.close();
        } catch (IOException e) {
        }
    }

    public ArrayList<EmailMessage> getNewEmails() {
        return allNewEmails;
    }
}