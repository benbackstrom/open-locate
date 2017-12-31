package com.backstrom.ben.openlocate.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by benba on 3/13/2017.
 */

public class DateFormatUtil {

    public static String getFormattedDate(long timestamp) {
        try{
            DateFormat sdf = new SimpleDateFormat("h:mm a, M/dd/yyyy");
            Date netDate = (new Date(timestamp));
            return sdf.format(netDate);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
