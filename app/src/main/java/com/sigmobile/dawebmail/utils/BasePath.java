package com.sigmobile.dawebmail.utils;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by rish on 8/1/16.
 */
public class BasePath {

    public static String getBasePath() {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/DAWebmail");

        if (!dir.exists())
            dir.mkdirs();

        return dir.getAbsolutePath();
    }

    public static ArrayList<String> getAttachmentsPaths(int contentID) {
        ArrayList<String> attachments = new ArrayList<>();
        for (File file : new File(getBasePath()).listFiles()) {
            if (file.getName().split("-")[0].toString().equals("" + contentID)) {
                System.out.println("Found same");
                attachments.add(file.getAbsolutePath());
            }
        }
        return attachments;
    }
}