package com.sigmobile.dawebmail.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.sigmobile.dawebmail.database.UserSettings;
import com.sigmobile.dawebmail.utils.Constants;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

public class LocationDetails implements Serializable {

    public Context context;

    public String Location_studentID;
    public String Location_TimeStamp;
    public String Location_WifiName;
    public String Location_IPAddress;
    public String Location_Subnet;

    public LocationDetails() {
        context = null;
        Location_Subnet = "";
        Location_IPAddress = "";
        Location_WifiName = "";
        Location_TimeStamp = "";
        Location_studentID = "";
    }

    public void setValue(Context context) {
        this.context = context;
        SharedPreferences settings = context.getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
        Location_studentID = UserSettings.getCurrentUser(context).username;
        Location_TimeStamp = DateFormat.getDateTimeInstance().format(new Date());
        Location_WifiName = getWifiName(context);
        Location_IPAddress = getIPAddress(context);
        Location_Subnet = "EMPTY";
    }

    private String getWifiName(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        String wifiname = wifiInfo.getSSID();
        return wifiname;
    }

    private String getIPAddress(Context context) {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        return ip;
    }

//    public void addLocationDetails(LocationDetails details) {
//        new ServerLoader(context).addLocationDetails(details);
//    }
}