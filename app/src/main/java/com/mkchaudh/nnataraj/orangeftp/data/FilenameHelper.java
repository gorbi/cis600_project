package com.mkchaudh.nnataraj.orangeftp.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nagaprasad on 4/27/17.
 */
public class FilenameHelper {
    private static Map<String, String> filename = new HashMap<>();

    public static String put(String uid, String filename) {
        return FilenameHelper.filename.put(uid, filename);
    }

    public static String get(String uid) {
        return FilenameHelper.filename.get(uid);
    }

    public static void reset() {
        filename.clear();
    }
}
