package com.mkchaudh.nnataraj.orangeftp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import com.mkchaudh.nnataraj.orangeftp.data.FTPConnectionCacher;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;

import static com.mkchaudh.nnataraj.orangeftp.MainActivity.RESULT_FAILURE;

public class UploadFileActivity extends AppCompatActivity {

    public static final String FTP_SERVER_NICKNAME = "ftpServerNickname";
    public static final String FULL_LOCAL_FILEPATH = "fullLocalFilepath";
    public static final String FULL_REMOTE_FILEPATH = "fullRemoteFilepath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();

        setResult(RESULT_FAILURE);

        if (extras != null) {

            final String ftpServerNickname = extras.getString(FTP_SERVER_NICKNAME);
            final String fullLocalFilepath = extras.getString(FULL_LOCAL_FILEPATH);
            final String fullRemoteFilepath = extras.getString(FULL_REMOTE_FILEPATH);

            if (ftpServerNickname != null
                    && fullRemoteFilepath != null
                    && fullLocalFilepath != null) {

                final FTPClient ftpClient = FTPConnectionCacher.getFTPConnection(ftpServerNickname);
                try {
                    final BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fullLocalFilepath));

                    ((TextView) findViewById(R.id.filename)).setText(fullLocalFilepath.substring(fullLocalFilepath.lastIndexOf("/") + 1));

                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {

                                FTPConnectionCacher.refreshFTPConnection(ftpServerNickname, ftpClient);

                                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                                boolean isSuccess = ftpClient.storeFile(fullRemoteFilepath, bufferedInputStream);
                                bufferedInputStream.close();

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
