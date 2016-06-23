package com.sigmobile.dawebmail.utils;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by rish on 6/10/15.
 */
public final class TheFont {

    private static Typeface typeface;

    private TheFont() throws InstantiationException {
        throw new InstantiationException("This utility class is not created for instantiation");
    }

    public static Typeface getFont(Context context) {
        if (typeface == null)
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/GeosansLight.ttf");
        return typeface;
    }
}