package com.backstrom.ben.openlocate.util;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by benba on 4/18/2017.
 */

public class BitmapUtils {

    private static final String TAG = BitmapUtils.class.getSimpleName();

    public static String bitmapToString(Bitmap bmp){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
        byte[] bytes = outputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.URL_SAFE);

    }
}
