package com.sigmobile.dawebmail.utils;

import android.content.Context;
import android.os.Environment;

import com.sigmobile.dawebmail.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by rish on 8/1/16.
 */
public class BasePath {

    public static String getBasePath(Context context) {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/" + context.getString(R.string.app_name));

        if (!dir.exists())
            dir.mkdirs();

        return dir.getAbsolutePath();
    }

    public static ArrayList<String> getAttachmentsPaths(Context context, int contentID) {
        ArrayList<String> attachments = new ArrayList<>();
        for (File file : new File(getBasePath(context)).listFiles()) {
            if (file.getName().split("-")[0].toString().equals("" + contentID)) {
                attachments.add(file.getAbsolutePath());
            }
        }
        return attachments;
    }
}