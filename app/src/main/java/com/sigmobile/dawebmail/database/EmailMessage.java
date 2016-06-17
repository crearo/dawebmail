package com.sigmobile.dawebmail.database;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.Serializable;
import java.util.List;

public class EmailMessage extends SugarRecord<EmailMessage> implements Serializable {

    private static final String TAG = "EmailMessage";

    private String fromName = "";
    private String fromAddress = "";
    private String subject = "";
    private String dateInMillis = "";
    private int contentID = -1;
    private String readUnread = "";
    private String content = "";
    private int totalAttachments = 0;
    private boolean important = false;
    private String userName;

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

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDateInMillis() {
        return dateInMillis;
    }

    public void setDateInMillis(String dateInMillis) {
        this.dateInMillis = dateInMillis;
    }

    public int getContentID() {
        return contentID;
    }

    public void setContentID(int contentID) {
        this.contentID = contentID;
    }

    public String getReadUnread() {
        return readUnread;
    }

    public void setReadUnread(String readUnread) {
        this.readUnread = readUnread;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTotalAttachments() {
        return totalAttachments;
    }

    public void setTotalAttachments(int totalAttachments) {
        this.totalAttachments = totalAttachments;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public static List<EmailMessage> getAllMailsOfUser(User user) {
        List<EmailMessage> emailMessages = Select.from(EmailMessage.class).where(Condition.prop(StringUtil.toSQLName("userName")).eq(user.getUsername())).orderBy(StringUtil.toSQLName("contentID")).list();
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

    public static EmailMessage getLastWebmailOfUser(User user) {
        List<EmailMessage> emailMessages = getAllMailsOfUser(user);
        if (emailMessages.size() > 0)
            return emailMessages.get(0);
        return null;
    }

    public static EmailMessage getEmailMessageFromContentID(int contentID) {
        return Select.from(EmailMessage.class).where(Condition.prop(StringUtil.toSQLName("contentID")).eq(contentID)).first();
    }

    public static EmailMessage saveNewEmailMessage(User user, int contentID, String fromName, String fromAddress, String subject, String dateInMillis, String readUnread, int totalAttachments, boolean important) {
        /* Check if webmail of that content ID exists - if it does, don't save */
        if (getEmailMessageFromContentID(contentID) == null) {
            EmailMessage emailMessage = new EmailMessage(user.getUsername(), contentID, fromName, fromAddress, subject, dateInMillis, readUnread, "", totalAttachments, important);
            emailMessage.save();
            return emailMessage;
        }
        return null;
    }

    public static void deleteEmailMessage(EmailMessage emailMessage) {
        emailMessage.delete();
    }

    public static void updateExistingEmailMessage(User user, EmailMessage emailMessage, int contentID, String fromName, String fromAddress, String subject, String dateInMillis, String readUnread, int totalAttachments, boolean important) {
        emailMessage.setUserName(user.getUsername());
        emailMessage.setFromName(fromName);
        emailMessage.setFromAddress(fromAddress);
        emailMessage.setSubject(subject);
        emailMessage.setDateInMillis(dateInMillis);
        emailMessage.setReadUnread(readUnread);
        emailMessage.setTotalAttachments(totalAttachments);
        emailMessage.setImportant(important);
        emailMessage.save();
    }

    public static void updateReadStatus(EmailMessage emailMessage, String readStatus) {
        emailMessage.setReadUnread(readStatus);
        emailMessage.save();
    }
}