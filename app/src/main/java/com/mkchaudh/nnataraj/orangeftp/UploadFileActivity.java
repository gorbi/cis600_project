package com.mkchaudh.nnataraj.orangeftp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import com.mkchaudh.nnataraj.orangeftp.data.FTPConnectionCacher;
import com.mkchaudh.nnataraj.orangeftp.data.FirebaseHelper;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.util.HashMap;

public class UploadFileActivity extends AppCompatActivity {

    public static final String FTP_SERVER_NICKNAME = "ftpServerNickName";
    public static final String FULL_LOCAL_FILEPATH = "fullLocalFilepath";
    public static final String FULL_REMOTE_FILEPATH = "fullRemoteFilepath";
    public static final int RESULT_FAILURE = -143;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();

        setResult(RESULT_FAILURE);

        if (extras != null) {

            final String ftpServerNickName = extras.getString(FTP_SERVER_NICKNAME);
            final String fullLocalFilepath = extras.getString(FULL_LOCAL_FILEPATH);
            final String fullRemoteFilepath = extras.getString(FULL_REMOTE_FILEPATH);

            if (ftpServerNickName != null
                    && fullRemoteFilepath != null
                    && fullLocalFilepath != null) {

                final FTPClient ftpClient = FTPConnectionCacher.getFTPConnection(ftpServerNickName);
                try {
                    final FileInputStream fileInputStream = new FileInputStream(fullLocalFilepath);

                    ((TextView) findViewById(R.id.filename)).setText(fullLocalFilepath.substring(fullLocalFilepath.lastIndexOf("/") + 1));

                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {

                                try {
                                    ftpClient.changeWorkingDirectory("/");
                                } catch (Exception ae) {
                                    final HashMap<String, String> ftpServerDetails = FirebaseHelper.getFTPClient(ftpServerNickName);


                                    ftpClient.connect(ftpServerDetails.get("hostname"), Integer.parseInt(ftpServerDetails.get("port")));
                                    ftpClient.login(ftpServerDetails.get("username"), ftpServerDetails.get("password"));
                                    ftpClient.enterLocalPassiveMode();
                                    ftpClient.changeWorkingDirectory("/");
                                    FTPConnectionCacher.updateFTPConnection(ftpServerDetails.get("servernickname"), ftpClient);
                                }

                                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                                boolean isSuccess = ftpClient.storeFile(fullRemoteFilepath, fileInputStream);
                                fileInputStream.close();

                                if (isSuccess)
                                    setResult(Activity.RESULT_OK);
                                finish();
                            } catch (IOException ae) {
                                StringWriter stackTrace = new StringWriter();
                                ae.printStackTrace(new PrintWriter(stackTrace));
                                Log.e("UploadFileActivity", stackTrace.toString());
                                finish();
                            }
                        }
                    };
                    new Thread(runnable).start();
                } catch (IOException ae) {
                    StringWriter stackTrace = new StringWriter();
                    ae.printStackTrace(new PrintWriter(stackTrace));
                    Log.e("UploadFileActivity", stackTrace.toString());
                    finish();
                }
            }
        }
    }

}
