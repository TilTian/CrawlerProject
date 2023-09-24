package org.example.Utils;

public class StringUtils {

    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }
    public static String XiaomiCommentIdParaConvert(String commentId) {
        return commentId.replace(":", "%3A");
    }
}
