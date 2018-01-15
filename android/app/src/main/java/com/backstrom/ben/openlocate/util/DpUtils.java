package com.backstrom.ben.openlocate.util;

import android.content.Context;

/**
 * Created by benba on 1/15/2018.
 */

public class DpUtils {

    public static float convertDpToPixels(Context context, float dp){
        float density = context.getResources().getDisplayMetrics().densityDpi;
        float px = dp * (density / 160f);
        return px;
    }

    public static float convertPixelsToDp(Context context, float px){
        float density = context.getResources().getDisplayMetrics().densityDpi;
        float dp = px / (density / 160f);
        return dp;
    }
}
