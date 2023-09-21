package org.example.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static String COMMON_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static String timeStamp2Date(Long seconds, String format) {
        if (seconds == null || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = COMMON_DATE_FORMAT;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(seconds));
    }
}
