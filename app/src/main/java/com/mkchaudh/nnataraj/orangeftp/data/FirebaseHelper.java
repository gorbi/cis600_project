package com.mkchaudh.nnataraj.orangeftp.data;

import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by nagaprasad on 4/23/17.
 */
public class FirebaseHelper {

    private static HashMap<String, HashMap<String, String>> ftpClients = new HashMap<>();
    private static DatabaseReference userRef = null;
    private static DatabaseReference ftpClientsRef = null;

    public static void updateFTPClients(HashMap<String, HashMap<String, String>> ftpClients) {
        if (ftpClients != null) {
            Set<String> keys = ftpClients.keySet();

            for (String key : keys) {
                FirebaseHelper.ftpClients.put(key, ftpClients.get(key));
            }
        }
    }

    public static void updateFTPClient(String servernickname, HashMap<String, String> ftpClient) {
        ftpClients.put(servernickname, ftpClient);
        if (ftpClientsRef != null)
            ftpClientsRef.getRef().setValue(ftpClients);
    }

    public static void removeFTPClient(String servernickname) {
        ftpClients.remove(servernickname);
        if (ftpClientsRef != null)
            ftpClientsRef.getRef().setValue(ftpClients);
    }

    public static HashMap<String, HashMap<String, String>> getFtpClients() {
        return new HashMap<>(ftpClients);
    }

    public static HashMap<String, String> getFTPClient(String servernickname) {
        return ftpClients.get(servernickname);
    }

    public static void set(String userId) {
        ftpClients.clear();
        userRef = FirebaseDatabase.getInstance().getReference(userId);
        ftpClientsRef = userRef.child("ftpclients");
    }

    public static DatabaseReference getUserRef() {
        if (userRef != null)
            return userRef.getRef();
        else
            return null;
    }

    public static DatabaseReference getFtpClientsRef() {
        if (ftpClientsRef != null)
            return ftpClientsRef.getRef();
        else
            return null;
    }

}
