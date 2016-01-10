package com.sigmobile.dawebmail.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * Created by rish on 11/1/16.
 */
public class FileUtils {

    public static void openDoc(Context context, String filePath) {
        File file = new File(filePath);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);
        intent.setDataAndType(Uri.fromFile(new File(file.toString())), type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
