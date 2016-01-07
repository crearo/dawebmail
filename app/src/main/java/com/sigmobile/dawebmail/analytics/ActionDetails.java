package com.sigmobile.dawebmail.analytics;

import android.content.Context;

/**
 * Created by rish on 1/8/15.
 */
public class ActionDetails {

    private Context context;
    public String action_StudentID = "STUDENT ID";
    public String action_Action = "ACTION";
    public String action_Connection = "No Connection";
    public String action_ConnectionDetails = "No Connection";
    public String action_TimeStamp = "Time";
    public String action_TimeTaken = "TimeTaken";
    public String action_Success = "false";

    public ActionDetails(String action_StudentID, String action_Action, String action_Connection, String action_ConnectionDetails, String action_TimeStamp, String action_TimeTaken, String action_Success) {
        this.action_StudentID = action_StudentID;
        this.action_Action = action_Action;
        this.action_Connection = action_Connection;
        this.action_ConnectionDetails = action_ConnectionDetails;
        this.action_TimeStamp = action_TimeStamp;
        this.action_TimeTaken = action_TimeTaken;
        this.action_Success = action_Success;
    }
}