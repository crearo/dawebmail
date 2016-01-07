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

    public EmailMessage() {
    }

    public EmailMessage(int contentID, String fromName, String fromAddress, String subject, String dateInMillis, String readUnread, String content) {
        this.contentID = contentID;
        this.fromName = fromName;
        this.fromAddress = fromAddress;
        this.subject = subject;
        this.dateInMillis = dateInMillis;
        this.readUnread = readUnread;
        this.content = content;
    }
}