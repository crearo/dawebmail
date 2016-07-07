package com.sigmobile.dawebmail.network;

import android.content.Context;
import android.util.Log;

import com.sigmobile.dawebmail.utils.BasePath;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by rish on 7/1/16.
 */
public class MailParser {

    private static final String TAG = "MailParser";
    private String contentHTML = "";
    private int totalAttachments = 0;

    public MailParser() {

    }

    public void newMailParser(Context context, int contentID, String emailContentBytes) {
        try {
            Session s = Session.getDefaultInstance(new Properties());
            InputStream is = new ByteArrayInputStream(emailContentBytes.getBytes(StandardCharsets.UTF_8));
            MimeMessage message = new MimeMessage(s, is);

//            javax.mail.Address[] fromAddress = message.getFrom();
//            String from = fromAddress[0].toString();
//            String subject = message.getSubject();
//            String sentDate = message.getSentDate().toString();

            String contentType = message.getContentType();

            // store attachment file name, separated by comma
            String attachFiles = "";

            if (contentType.contains("multipart")) {
                // content may contain attachments
                Multipart multiPart = (Multipart) message.getContent();
                int numberOfParts = multiPart.getCount();
                for (int partCount = 0; partCount < numberOfParts; partCount++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        // this part is attachment
                        String fileName = part.getFileName();
                        attachFiles += fileName + ", ";
                        part.saveFile(BasePath.getBasePath(context) + "/" + contentID + "-" + fileName);
                        totalAttachments++;
                    } else {
                        // this part may be the message content
                        if (part.getContent() instanceof MimeMultipart) {
//                        System.out.println(((MimeMultipart) (part.getContent())).getBodyPart(1).getContentType());
                            parseMime((MimeMultipart) part.getContent());
                            Log.d(TAG, "IS instanceof Mimemultipart");
                        } else {
                            contentHTML = part.getContent().toString();
                            Log.d(TAG, "ISNT instanceof Mimemultipart");
                        }
                    }
                }
                if (attachFiles.length() > 1) {
                    attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
                }
            } else if (contentType.contains("text/plain")) {
                Object content = message.getContent();
                if (content != null) {
                    contentHTML = content.toString();
                }
            } else if (contentType.contains("text/html")) {
                Object content = message.getContent();
                if (content != null) {
                    contentHTML = content.toString();
                }
            }

            if (contentHTML.contains("background-color: #ffffff;"))
                contentHTML = contentHTML.replace("background-color: #ffffff;", "");

        } catch (Exception e) {
            Log.d(TAG, "Error in parsing email");
            e.printStackTrace();
        }
    }

    //    public void parseMail(String content) {
//        try {
//            System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "true");
//            ByteArrayDataSource ds = new ByteArrayDataSource(content, "multipart/mixed");
//
//            Session s = Session.getDefaultInstance(new Properties());
//            InputStream is = new ByteArrayInputStream(content.getBytes());
//            MimeMessage message = new MimeMessage(s, is);
//
//            MimeMultipart multipart = new MimeMultipart(ds);
//
//            if (message.getContentType().contains("multipart")) {
//                Log.d(TAG, "IS MULTIPART");
//                parseMime(multipart);
//            } else {
//                Log.d(TAG, "IS NOT MULTIPART" + message.getContent());
//                contentHTML = "" + message.getContent();
//            }
//
//        } catch (Exception e) {
//            Log.d(TAG, "Error in parsingMail");
//            e.printStackTrace();
//        }
//    }
//
    public void parseMime(MimeMultipart multipart) {
        try {
            for (int i = 0; i < multipart.getCount(); i++) {
                if (multipart.getBodyPart(i).getContent() instanceof MimeMultipart) {
                    parseMime((MimeMultipart) (multipart.getBodyPart(i).getContent()));
                } else {
                    String multiPartType = multipart.getBodyPart(i).getContentType();
                    String multiPartContent = "" + ((multipart.getBodyPart(i).getContent()));
                    if (multiPartType.contains("text/html")) {
                        contentHTML = multiPartContent;
                    } else if (multiPartType.contains("text/plain")) {
                        contentHTML = multiPartContent;
                    } else {
                        Log.d(TAG, multiPartContent + "\nis not defined yet");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getContentHTML() {
        Log.d(TAG, "Returning html content " + contentHTML);
        return contentHTML;
    }

    public int getTotalAttachments() {
        return totalAttachments;
    }
}