package com.sigmobile.dawebmail.scraper;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.orm.StringUtil;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.sigmobile.dawebmail.database.EmailMessage;
import com.sigmobile.dawebmail.utils.Constants;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
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
    public static final String REST_URL_LOGIN = "https://webmail.daiict.ac.in/service/home/~/inbox.rss?limit=1";
    public static final String REST_URL_INBOX = "https://webmail.daiict.ac.in/home/~/inbox.json";

    private String username, password;
    private Context context;

    private ArrayList<EmailMessage> allNewEmails = new ArrayList<>();

    public RestAPI(String username, String password, Context context) {
        this.username = username;
        this.password = password;
        this.context = context;
    }

    public boolean logIn() {
        return makeLoginRequest();
    }

    public boolean refresh(String TYPE) {
        if (TYPE.equals(Constants.INBOX)) {
            return makeInboxRefreshRequest();
        }
        return false;
    }

    private boolean makeLoginRequest() {
        try {
            URL url = new URL(REST_URL_LOGIN);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            String userPassword = username + ":" + password;
            String encoding = Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT);
            conn.setRequestProperty("Authorization", "Basic " + encoding);
            conn.setReadTimeout(30 * 1000);
            conn.connect();

            Log.d(LOGTAG, "Response Code: " + conn.getResponseCode());
            if (conn.getResponseCode() != 401) {
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

    private boolean makeInboxRefreshRequest() {

        allNewEmails = new ArrayList<>();

        try {
            URL url = new URL(REST_URL_INBOX);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            String userPassword = username + ":" + password;
            String encoding = Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT);
            conn.setRequestProperty("Authorization", "Basic " + encoding);
            conn.setReadTimeout(30 * 1000);
            conn.connect();

            Log.d(LOGTAG, "Response Code: " + conn.getResponseCode());
            if (conn.getResponseCode() != 401) {
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

                JSONObject responseObject = new JSONObject(total.toString());

                EmailMessage latestContentID = Select.from(EmailMessage.class).orderBy(StringUtil.toSQLName("contentID")).first();

                if (total.toString().contains("\"id\":\"" + latestContentID.contentID + "\"")) {
                    Log.d(LOGTAG, "Phone's latest email is still there on webmail");
                }

                for (int i = 0; i < responseObject.getJSONArray("m").length(); i++) {
                    JSONObject webmailObject = (JSONObject) responseObject.getJSONArray("m").get(i);
                    int contentID = Integer.parseInt(webmailObject.getString("id"));
                    String fromName = "fromName";
                    String fromAddress = "fromAddress";
                    String subject = webmailObject.getString("su");
                    String readUnread = Constants.WEBMAIL_READ;
                    if (webmailObject.has("f"))
                        if (webmailObject.getString("f").contains("u"))
                            readUnread = Constants.WEBMAIL_UNREAD;
                    String dateInMillis = webmailObject.getString("d");
                    String content = webmailObject.getString("fr");

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

                    Log.d(LOGTAG, "NEW EMAIL | " + contentID + " | " + fromName + " | " + fromAddress + " | " + subject + " | " + dateInMillis + " | " + readUnread);

                    if (contentID == latestContentID.contentID) {
                        Log.d(LOGTAG, "Found same email. ID = " + contentID);
                        break;
                    } else {
                        EmailMessage emailMessage = Select.from(EmailMessage.class).where(Condition.prop(StringUtil.toSQLName("contentID")).eq(contentID)).first();
                        if (emailMessage != null) {
                            Log.d(LOGTAG, "Found existing mail, updating");
                            emailMessage.contentID = contentID;
                            emailMessage.fromName = fromName;
                            emailMessage.fromAddress = fromAddress;
                            emailMessage.subject = subject;
                            emailMessage.dateInMillis = dateInMillis;
                            emailMessage.readUnread = readUnread;
                            emailMessage.save();
                        } else {
                            Log.d(LOGTAG, "No existing mail found, Creating");
                            emailMessage = new EmailMessage(contentID, fromName, fromAddress, subject, dateInMillis, readUnread, content);
                            emailMessage.save();
                            allNewEmails.add(emailMessage);
                        }
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

    public ArrayList<EmailMessage> getNewEmails() {
        return allNewEmails;
    }
}