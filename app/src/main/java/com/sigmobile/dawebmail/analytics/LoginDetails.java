package com.sigmobile.dawebmail.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

import com.sigmobile.dawebmail.database.UserSettings;
import com.sigmobile.dawebmail.utils.Constants;

public class LoginDetails implements Serializable {

    Context context;

    public String Login_studentID = "STUDENT ID";
    public String Login_Connection = "No Connection";
    public String Login_TimeStamp = "Time";
    public String Login_connectionDetails = "No Connection";
    public String Login_loginType = "Manual";
    public String Login_TimeTaken = "-";
    public String Login_Success = "false";

    public LoginDetails() {
        Login_studentID = "";
        Login_Connection = "";
        Login_TimeStamp = "";
        Login_connectionDetails = "";
        Login_loginType = "";
        Login_TimeTaken = "";
        Login_Success = "";
    }

    public void setValues(Context context, String LoginType, String LoginSuccess, String LoginTime) {
        this.context = context;
        SharedPreferences settings = context.getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
        Login_studentID = UserSettings.getUsername(context);
        Login_Connection = getConnectionType();
        Login_TimeStamp = DateFormat.getDateTimeInstance().format(new Date());
        Login_connectionDetails = getConDetails();
        Login_loginType = LoginType;
        Login_Success = LoginSuccess;
        Login_TimeTaken = LoginTime;
    }

    private String getConDetails() {
        return "None for now";
    }

    private String getConnectionType() {
        String conn = "Not connected";
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                conn = "3G";
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                conn = "Wifi";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

//    public void addLoginDetails(LoginDetails details) {
//        new ServerLoader(context).addLoginDetails(details);
//    }
}
