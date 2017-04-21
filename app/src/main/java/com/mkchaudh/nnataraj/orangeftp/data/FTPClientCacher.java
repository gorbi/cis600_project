package com.mkchaudh.nnataraj.orangeftp.data;

import android.util.Log;
import org.apache.commons.net.ftp.FTPClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nagaprasad on 4/20/17.
 */
public class FTPClientCacher {
    private static Map<String, FTPClient> ftpClients = new HashMap<>();

    public static void updateFTPClient(String ftpServerNickname, FTPClient ftpClient) {
        ftpClients.put(ftpServerNickname, ftpClient);
        Log.d("FTPClientCacher", "Updated FTP client for: " + ftpServerNickname);
    }

    public static FTPClient getFTPClient(String ftpServerNickname) {
        if (ftpClients.get(ftpServerNickname) == null) {
            ftpClients.put(ftpServerNickname, new FTPClient());
            Log.d("FTPClientCacher", "Created new FTP client for: " + ftpServerNickname);
        } else {
            Log.d("FTPClientCacher", "Reusing existing FTP client for: " + ftpServerNickname);
        }
        return ftpClients.get(ftpServerNickname);
    }
}
