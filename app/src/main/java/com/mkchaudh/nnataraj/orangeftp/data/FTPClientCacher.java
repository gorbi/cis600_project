package com.mkchaudh.nnataraj.orangeftp.data;

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
    }

    public static FTPClient getFTPClient(String ftpServerNickname) {
        if (ftpClients.get(ftpServerNickname) == null)
            ftpClients.put(ftpServerNickname, new FTPClient());
        return ftpClients.get(ftpServerNickname);
    }
}
