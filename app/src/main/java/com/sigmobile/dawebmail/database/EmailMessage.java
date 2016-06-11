package com.sigmobile.dawebmail.database;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.Serializable;
import java.util.List;

public class EmailMessage extends SugarRecord<EmailMessage> implements Serializable {

    public String fromName = "";
    public String fromAddress = ""; // new
    public String subject = "";
    public String dateInMillis = "";
    public int contentID = -1;
    public String readUnread = "";
    public String content = "";
    public int totalAttachments = 0;
    public boolean important = false;
    public String userName;

    private static final String TAG = "EmailMessage";

    public EmailMessage() {
    }

    public EmailMessage(String userName, int contentID, String fromName, String fromAddress, String subject, String dateInMillis, String readUnread, String content, int totalAttachments, boolean important) {
        this.userName = userName;
        this.contentID = contentID;
        this.fromName = fromName;
        this.fromAddress = fromAddress;
        this.subject = subject;
        this.dateInMillis = dateInMillis;
        this.readUnread = readUnread;
        this.content = content;
        this.totalAttachments = totalAttachments;
        this.important = important;
    }


    public static List<EmailMessage> getAllMailsOfUser(User user) {
        List<EmailMessage> emailMessages = Select.from(EmailMessage.class).where(Condition.prop(StringUtil.toSQLName("userName")).eq(user.username)).orderBy(StringUtil.toSQLName("contentID")).list();
        return emailMessages;
    }

    public static void deleteAllMailsOfUser(User user) {
        for (EmailMessage emailMessage : getAllMailsOfUser(user)) {
            emailMessage.delete();
        }
    }

    public static EmailMessage getLatestWebmailOfUser(User user) {
        List<EmailMessage> emailMessages = getAllMailsOfUser(user);
        if (emailMessages.size() > 0)
            return emailMessages.get(emailMessages.size() - 1);
        return null;
    }

    public static EmailMessage getEmailMessageFromContentID(int contentID) {
        return Select.from(EmailMessage.class).where(Condition.prop(StringUtil.toSQLName("contentID")).eq(contentID)).first();
    }

    public static EmailMessage createNewEmailMessage(User user, int contentID, String fromName, String fromAddress, String subject, String dateInMillis, String readUnread, int totalAttachments, boolean important) {
        EmailMessage emailMessage = new EmailMessage(user.username, contentID, fromName, fromAddress, subject, dateInMillis, readUnread, "", totalAttachments, important);
        emailMessage.save();
        return emailMessage;
    }

    public static void updateExistingEmailMessage(User user, EmailMessage emailMessage, int contentID, String fromName, String fromAddress, String subject, String dateInMillis, String readUnread, int totalAttachments, boolean important) {
        emailMessage.userName = user.username;
        emailMessage.contentID = contentID;
        emailMessage.fromName = fromName;
        emailMessage.fromAddress = fromAddress;
        emailMessage.subject = subject;
        emailMessage.dateInMillis = dateInMillis;
        emailMessage.readUnread = readUnread;
        emailMessage.totalAttachments = totalAttachments;
        emailMessage.important = important;
        emailMessage.save();
    }
}