package com.sigmobile.dawebmail.database;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.Serializable;
import java.util.List;

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


    public static List<EmailMessage> getAllMailsOfUser(User user) {
        return Select.from(EmailMessage.class).where(Condition.prop(StringUtil.toSQLName("user")).eq(user)).orderBy(StringUtil.toSQLName("contentID")).list();
    }

}