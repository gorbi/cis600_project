package com.mkchaudh.nnataraj.orangeftp;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.mkchaudh.nnataraj.orangeftp.data.FTPConnectionCacher;
import com.mkchaudh.nnataraj.orangeftp.data.FirebaseHelper;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;

public class StoreLocationService extends Service {

    public static final String FTP_SERVER_NICKNAME = "ftpServerNickname";
    public static final String FTP_CURRENT_LOCATION = "currentDirectory";

    public StoreLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        final LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        String ftpServerNickname = intent.getExtras().getString(FTP_SERVER_NICKNAME);
                        String currentDirectory = intent.getExtras().getString(FTP_CURRENT_LOCATION);

                        FTPClient ftpClient = FTPConnectionCacher.getFTPConnection(ftpServerNickname);

                        HashMap<String, String> mFtpServerDetails = FirebaseHelper.getFTPClient(ftpServerNickname);
                        try {
                            try {
                                ftpClient.changeWorkingDirectory(currentDirectory);
                            } catch (Exception ae) {
                                ftpClient.connect(mFtpServerDetails.get("hostname"), Integer.parseInt(mFtpServerDetails.get("port")));
                                ftpClient.login(mFtpServerDetails.get("username"), mFtpServerDetails.get("password"));
                                ftpClient.enterLocalPassiveMode();
                                ftpClient.changeWorkingDirectory(currentDirectory);
                                FTPConnectionCacher.updateFTPConnection(mFtpServerDetails.get("servernickname"), ftpClient);
                            }

                            String data = "Latitude: " + location.getLatitude()
                                    + "\nLongitude: " + location.getLongitude();

                            InputStream stream = new ByteArrayInputStream(data.getBytes(Charset.defaultCharset()));

                            ftpClient.storeFile(currentDirectory+"/"+"location.txt", stream);

                            stream.close();
                        } catch (IOException ae) {
                            if (ftpClient.isConnected()) {
                                try {
                                    ftpClient.disconnect();
                                } catch (IOException f) {
                                    // do nothing
                                }
                            }
                            FTPConnectionCacher.updateFTPConnection(mFtpServerDetails.get("servernickname"), new FTPClient());
                            StringWriter stackTrace = new StringWriter();
                            ae.printStackTrace(new PrintWriter(stackTrace));
                            Log.e("StoreLocationService", stackTrace.toString());
                        }
                        stopSelf();
                    }
                };

                new Thread(runnable).start();

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5*60000,
                50, mLocationListener);

        return Service.START_REDELIVER_INTENT;
    }
}
