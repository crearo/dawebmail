package com.sigmobile.dawebmail.scraper;

import android.util.Log;

import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * Created by rish on 7/1/16.
 */
public class MailParser {

    private static final String LOGTAG = "RESTAPI";
    private String contentHTML = "";

    public MailParser() {

    }

    public void parseMail(String content) {
        Log.wtf(LOGTAG, content);
        try {
            System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "true");
            ByteArrayDataSource ds = new ByteArrayDataSource(content, "multipart/mixed");
            MimeMultipart multipart = new MimeMultipart(ds);
            parseMime(multipart);
        } catch (Exception e) {
            Log.d(LOGTAG, "Error in parsingMail");
            e.printStackTrace();
        }
    }

    public void parseMime(MimeMultipart multipart) {
        System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "true");
        try {
            for (int i = 0; i < multipart.getCount(); i++) {
                if (multipart.getBodyPart(i).getContent() instanceof MimeMultipart) {
                    parseMime((MimeMultipart) (multipart.getBodyPart(i).getContent()));
                } else {
                    String multiPartType = multipart.getBodyPart(i).getContentType();
                    String multiPartContent = "" + ((multipart.getBodyPart(i).getContent()));
                    System.out.println(multiPartType + " | " + multiPartContent);
                    if (multiPartType.contains("text/html")) {
                        contentHTML = multiPartContent;
                    } else if (multiPartType.contains("text/plain")) {
                        contentHTML = multiPartContent;
                    } else if (multiPartType.contains("image/jpeg")) {

                    } else if (multiPartType.contains("image/png")) {

                    } else {
                        Log.d(LOGTAG, "is not defined yet");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getContentHTML() {
        return contentHTML;
    }
}