package com.mkchaudh.nnataraj.orangeftp.data;

import android.util.Log;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nagaprasad on 4/20/17.
 */
public class FTPConnectionCacher {

    private static Map<String, FTPClient> ftpClients = new HashMap<>();

    public static void updateFTPConnection(String ftpServerNickname, FTPClient ftpClient) {
        ftpClients.put(ftpServerNickname, ftpClient);
        Log.d("FTPConnectionCacher", "Updated FTP client for: " + ftpServerNickname);
    }

    public static FTPClient getFTPConnection(String ftpServerNickname) {
        if (ftpClients.get(ftpServerNickname) == null) {
            ftpClients.put(ftpServerNickname, new FTPClient());
            Log.d("FTPConnectionCacher", "Created new FTP client for: " + ftpServerNickname);
        } else {
            Log.d("FTPConnectionCacher", "Reusing existing FTP client for: " + ftpServerNickname);
        }
        return ftpClients.get(ftpServerNickname);
    }

    public static void reset() {
        ftpClients.clear();
    }

    public static void refreshFTPConnection(String ftpServerNickname, FTPClient ftpClient) throws IOException {
        try {
            ftpClient.changeWorkingDirectory("/");
        } catch (Exception ae) {
            final HashMap<String, String> ftpServerDetails = FirebaseHelper.getFTPClient(ftpServerNickname);


            ftpClient.connect(ftpServerDetails.get("hostname"), Integer.parseInt(ftpServerDetails.get("port")));
            ftpClient.login(ftpServerDetails.get("username"), ftpServerDetails.get("password"));
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory("/");
            FTPConnectionCacher.updateFTPConnection(ftpServerDetails.get("servernickname"), ftpClient);
        }
    }
}
