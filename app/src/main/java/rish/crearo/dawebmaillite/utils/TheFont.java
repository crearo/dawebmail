package rish.crearo.dawebmaillite.utils;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by rish on 6/10/15.
 */
public class TheFont {

    public static Typeface getFont(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/GeosansLight.ttf");
    }
}
