package com.sigmobile.dawebmail.utils;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by rish on 6/10/15.
 */
public class TheFont {

    private static Typeface typeface;

    public static Typeface getFont(Context context) {
        if (typeface == null)
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/GeosansLight.ttf");
        return typeface;
    }
}