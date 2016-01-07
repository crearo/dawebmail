package com.sigmobile.dawebmail.analytics;

/**
 * Created by rish on 29/7/15.
 */
public class FeedbackDetails {

    public String studentID = "";
    public String feedback = "";
    public String timeStamp = "";

    public FeedbackDetails(String studentID, String feedback, String timeStamp) {
        this.studentID = studentID;
        this.feedback = feedback;
        this.timeStamp = timeStamp;
    }
}
