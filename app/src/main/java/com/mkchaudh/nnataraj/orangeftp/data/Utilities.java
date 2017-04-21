package com.mkchaudh.nnataraj.orangeftp.data;

/**
 * Created by nagaprasad on 4/21/17.
 */
public class Utilities {
    public static String getReadableSize(long bytes) {
        if (bytes > 999)
            if (bytes > 999999)
                if (bytes > 999999999)
                    return (Math.round(bytes * 100.00 / 1024 / 1024 / 1024) / 100.00) + " GB";
                else
                    return (Math.round(bytes * 100.00 / 1024 / 1024) / 100.00) + " MB";
            else
                return (Math.round(bytes * 100.00 / 1024) / 100.00) + " KB";
        else
            return bytes + " bytes";
    }
}
