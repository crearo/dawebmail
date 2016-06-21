package com.sigmobile.dawebmail.utils;

/**
 * Created by rish on 6/10/15.
 */
public final class Constants {

    public static String FOLDER = "FOLDER";
    public static String INBOX = "INBOX";
    public static String SENT = "SENT";
    public static String TRASH = "TRASH";

    public static String FRAGMENT_TAG_INBOX = "FRAGMENT_TAG_INBOX";
    public static String FRAGMENT_TAG_FOLDER = "FRAGMENT_TAG_FOLDER";
    public static String FRAGMENT_TAG_SMARTBOX = "FRAGMENT_TAG_SMARTBOX";

    public static String WEBMAIL_READ = "READ";
    public static String WEBMAIL_UNREAD = "UNREAD";

    public static String CURRENT_EMAIL_SERIALIZABLE = "CURRENT_EMAIL_SERIALIZABLE";
    public static String CURRENT_EMAIL_TYPE = "CURRENT_EMAIL_TYPE";
    public static String CURRENT_EMAIL_ID = "CURRENT_EMAIL_ID";

    public static String BROADCAST_REFRESH_ADAPTERS = "BROADCAST_REFRESH_ADAPTERS";
    public static String BUNDLE_ON_POST_REFRESH_EMAILS_SIZE = "BUNDLE_ON_POST_REFRESH_EMAILS_SIZE";

    public static String REFRESH_TYPE_LOAD_MORE = "REFRESH_TYPE_LOAD_MORE";
    public static String REFRESH_TYPE_REFRESH = "REFRESH_TYPE_REFRESH";

    private Constants() throws InstantiationException {
        throw new InstantiationException("This utility class is not created for instantiation");
    }

}