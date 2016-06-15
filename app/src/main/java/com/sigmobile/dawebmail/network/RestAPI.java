package com.sigmobile.dawebmail.network;

import android.content.Context;
import android.util.Base64;

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

    private static final String TAG = "RESTAPI";

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

    public boolean refresh(String folder) {
        return handleRefreshAndLoadMoreRequest(folder, Constants.REFRESH_TYPE_REFRESH);
    }

    public boolean loadMore(String folder, int lengthToLoad) {
        return handleRefreshAndLoadMoreRequest(folder, Constants.REFRESH_TYPE_LOAD_MORE, lengthToLoad);
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

            if (conn.getResponseCode() == 200) {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                in.close();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private ArrayList<EmailMessage> fetchMailsOfFolder(String folder) {
        ArrayList<EmailMessage> parsedMails = new ArrayList<>();
        URL url = null;
        try {
            if (folder.equals(Constants.INBOX))
                url = new URL(context.getString(R.string.rest_url_inbox));
            else if (folder.equals(Constants.SENT))
                url = new URL(context.getString(R.string.rest_url_sent));
            else if (folder.equals(Constants.TRASH))
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

                    EmailMessage emailMessage = new EmailMessage(user.username, contentID, fromName, fromAddress, subject, dateInMillis, readUnread, "", totalAttachments, important);
                    parsedMails.add(emailMessage);
                }
                return parsedMails;
            } else {
                return parsedMails;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return parsedMails;
        }
    }

    private boolean handleRefreshAndLoadMoreRequest(String folder, String refreshType, int lengthToLoad) {
        allNewEmails = new ArrayList<>();

        if (folder.equals(Constants.INBOX)) {
            /**
             * Traverse through all stored emails, and delete those that aren't there in fetchedList
             */
            ArrayList<EmailMessage> fetchedEmails = fetchMailsOfFolder(folder);
            for (EmailMessage storedEmail : EmailMessage.getAllMailsOfUser(user)) {
                boolean storedEmailFound = false;
                for (EmailMessage fetchedEmail : fetchedEmails) {
                    if (fetchedEmail.contentID == storedEmail.contentID) {
                        storedEmailFound = true;
                        break;
                    }
                }
                if (!storedEmailFound) {
                    storedEmail.delete();
                }
            }

            EmailMessage lastWebmail = EmailMessage.getLastWebmailOfUser(user);
            EmailMessage latestWebmail = EmailMessage.getLatestWebmailOfUser(user);

            int indexOfLastEmailInFetchedList = 0;
            int indexOfLatestEmailInFetchedList = 0;

            /**
             * Find index of latest and last webmails in the fetched list
             * All emails above latestEmails are ones to be saved in refresh
             * lengthToLoad emails below lastEmail are ones to be saved in loadmore
             */
            for (int i = 0; i < fetchedEmails.size(); i++) {
                if (lastWebmail != null)
                    if (fetchedEmails.get(i).contentID == lastWebmail.contentID)
                        indexOfLastEmailInFetchedList = i;
                if (latestWebmail != null)
                    if (fetchedEmails.get(i).contentID == latestWebmail.contentID)
                        indexOfLatestEmailInFetchedList = i;
            }

            /**
             * Two cases : Refresh or Load More
             */
            if (refreshType.equals(Constants.REFRESH_TYPE_REFRESH)) {
                for (int m = 0; m < indexOfLatestEmailInFetchedList; m++) {
                    EmailMessage fetchedEmail = fetchedEmails.get(m);
                    EmailMessage emailMessage = EmailMessage.saveNewEmailMessage(user, fetchedEmail.contentID, fetchedEmail.fromName, fetchedEmail.fromAddress, fetchedEmail.subject, fetchedEmail.dateInMillis, fetchedEmail.readUnread, fetchedEmail.totalAttachments, fetchedEmail.important);
                    allNewEmails.add(emailMessage);
                }
            } else if (refreshType.equals(Constants.REFRESH_TYPE_LOAD_MORE)) {
                /* Check if fetchedEmailSize is big enough to load lengthToLoad */
                lengthToLoad = (lengthToLoad + indexOfLastEmailInFetchedList) <= (fetchedEmails.size()) ? (lengthToLoad) : (fetchedEmails.size() - indexOfLastEmailInFetchedList);
                for (int m = indexOfLastEmailInFetchedList; m < indexOfLastEmailInFetchedList + lengthToLoad; m++) {
                    EmailMessage fetchedEmail = fetchedEmails.get(m);
                    EmailMessage emailMessage = EmailMessage.saveNewEmailMessage(user, fetchedEmail.contentID, fetchedEmail.fromName, fetchedEmail.fromAddress, fetchedEmail.subject, fetchedEmail.dateInMillis, fetchedEmail.readUnread, fetchedEmail.totalAttachments, fetchedEmail.important);
                    allNewEmails.add(emailMessage);
                }
            }
        } else {
            allNewEmails.addAll(fetchMailsOfFolder(folder));
        }
        return false;
    }

    private boolean handleRefreshAndLoadMoreRequest(String folder, String refreshType) {
        return handleRefreshAndLoadMoreRequest(folder, refreshType, Integer.MAX_VALUE);
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

                writeStringAsFile(context, total.toString());

                MailParser mailParser = new MailParser();
                mailParser.newMailParser(context, emailMessage.contentID, total.toString());
                emailMessage.content = mailParser.getContentHTML();
                emailMessage.totalAttachments = mailParser.getTotalAttachments();

                return emailMessage;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeStringAsFile(Context context, final String fileContents) {
        try {
            FileWriter out = new FileWriter(new File(BasePath.getBasePath(context), "email.txt"));
            out.write(fileContents);
            out.close();
        } catch (IOException e) {
        }
    }

    public ArrayList<EmailMessage> getNewEmails() {
        return allNewEmails;
    }
}