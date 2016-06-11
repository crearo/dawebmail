package com.sigmobile.dawebmail.database;

import com.orm.SugarRecord;

import java.io.Serializable;

public class EmailMessage extends SugarRecord implements Serializable {

    public String fromName = "";
    public String fromAddress = ""; // new
    public String subject = "";
    public String dateInMillis = "";
    public int contentID = -1;
    public String readUnread = "";
    public String content = "";
    public int totalAttachments = 0;
    public boolean important = false;
    public User user;

    public EmailMessage() {
    }

    public EmailMessage(User user, int contentID, String fromName, String fromAddress, String subject, String dateInMillis, String readUnread, String content, int totalAttachments, boolean important) {
        this.user = user;
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
}