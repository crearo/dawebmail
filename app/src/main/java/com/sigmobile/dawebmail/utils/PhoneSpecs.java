package com.sigmobile.dawebmail.utils;

import android.os.Build;
import android.text.TextUtils;

/**
 * Created by rish on 20/6/16.
 */
public class PhoneSpecs {

    /**
     * Returns the consumer friendly device name
     */
    public static String getDeviceName() {
        try {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer)) {
                return capitalize(model);
            }
            return capitalize(manufacturer) + " " + model;
        } catch (Exception e) {
            return "null";
        }
    }

    public static String getAndroidVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    private static String capitalize(String str) {
        try {
            if (TextUtils.isEmpty(str)) {
                return str;
            }
            char[] arr = str.toCharArray();
            boolean capitalizeNext = true;

            StringBuilder phrase = new StringBuilder();
            for (char c : arr) {
                if (capitalizeNext && Character.isLetter(c)) {
                    phrase.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                    continue;
                } else if (Character.isWhitespace(c)) {
                    capitalizeNext = true;
                }
                phrase.append(c);
            }

            return phrase.toString();
        } catch (Exception e) {
            return str;
        }
    }
}