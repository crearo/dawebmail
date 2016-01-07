package com.sigmobile.dawebmail.analytics;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.DisplayMetrics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

public class PhoneDetails implements Serializable {

    Context context;
    Activity activity;

    public String Phone_Brand = "";
    public String Phone_Model = "";
    public String Phone_AndroidVersion = "";
    public String Phone_AppList = "";
    public String Phone_ScreenSize = "";

    public PhoneDetails() {
        Phone_Brand = "";
        Phone_Model = "";
        Phone_AndroidVersion = "";
        Phone_AppList = "";
        Phone_ScreenSize = "";
    }

    public void setVlaues(Activity activity, Context context) {
        this.context = context;
        this.activity = activity;
        Phone_Brand = Build.BRAND;
        Phone_Model = Build.MODEL;
        Phone_AndroidVersion = Build.VERSION.RELEASE;
        Phone_AppList = getApplist(context);
        Phone_ScreenSize = getScreenSize(activity);
    }

    private String getApplist(Context context) {
        try {
            final PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            JSONObject jsonApps = new JSONObject();
            String stringapps = "";
            int count = 1;
            for (ApplicationInfo packageInfo : packages) {
                if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    stringapps += "" + packageInfo.packageName + ",";
                    jsonApps.put("" + count++, "" + packageInfo.packageName);
                }
            }
            return stringapps;
        } catch (JSONException e) {
            e.printStackTrace();
            return "Unable to fetch applist";
        }
    }

    private String getScreenSize(Activity activity) {
        try {
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            int dens = dm.densityDpi;
            double wi = (double) width / (double) dens;
            double hi = (double) height / (double) dens;

            double diagonal = Math.sqrt(Math.pow(wi, 2) + Math.pow(hi, 2));
            diagonal = Math.round(diagonal * 100.0) / 100.0;

            return "{ 'diagonal': '" + diagonal + "', width : '" + wi + "', 'height' : '" + hi + "' }";
        } catch (Exception e) {
            return "Unableto get dimensions";
        }
    }

//    public void addPhoneDetails(PhoneDetails details) {
//        new ServerLoader(context).addPhoneDetails(details);
//    }
}
