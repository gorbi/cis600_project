package com.mkchaudh.nnataraj.orangeftp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.mkchaudh.nnataraj.orangeftp.data.FirebaseHelper;
import org.apache.commons.net.ftp.FTPClient;

import java.util.HashMap;

public class UpdateFTPClientActivity extends AppCompatActivity {

    public static final String FTP_SERVER_NICKNAME = "ftpServerNickname";

    private void displayToast(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message,Toast.LENGTH_LONG).show();
            }
        };
        runOnUiThread(runnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_ftpclient);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();

        final EditText servernickname = (EditText) findViewById(R.id.servernickname);
        final EditText hostname = (EditText) findViewById(R.id.hostname);
        final EditText port = (EditText) findViewById(R.id.port);
        final EditText username = (EditText) findViewById(R.id.username);
        final EditText password = (EditText) findViewById(R.id.password);
        final CheckBox verify = (CheckBox) findViewById(R.id.verify);


        if (extras != null) {
            final String ftpServerNickname = extras.getString(FTP_SERVER_NICKNAME);
            if (ftpServerNickname != null) {
                HashMap<String, String> ftpclient = FirebaseHelper.getFTPClient(ftpServerNickname);
                servernickname.setText(ftpclient.get("servernickname").toString());
                hostname.setText(ftpclient.get("hostname").toString());
                port.setText(ftpclient.get("port").toString());
                username.setText(ftpclient.get("username").toString());
                password.setText(ftpclient.get("password").toString());
            }
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding...");
        progressDialog.setCancelable(false);


        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                if (verify.isChecked()) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                progressDialog.setCancelable(true);
                                FTPClient ftpClient = new FTPClient();
                                ftpClient.connect(hostname.getText().toString(), Integer.parseInt(port.getText().toString()));
                                ftpClient.login(username.getText().toString(), password.getText().toString());
                                ftpClient.enterLocalPassiveMode();
                                ftpClient.changeWorkingDirectory("/");
                                ftpClient.listFiles();
                                progressDialog.setCancelable(false);
                                HashMap<String, String> ftpclient = new HashMap<>();
                                ftpclient.put("servernickname", servernickname.getText().toString());
                                ftpclient.put("hostname", hostname.getText().toString());
                                ftpclient.put("port", port.getText().toString());
                                ftpclient.put("username", username.getText().toString());
                                ftpclient.put("password", password.getText().toString());
                                FirebaseHelper.updateFTPClient(servernickname.getText().toString(), ftpclient);
                                progressDialog.dismiss();
                                displayToast("Server added successfully");
                                finish();
                            } catch (Exception ae) {
                                progressDialog.dismiss();
                                displayToast("Connectivity check failed");
                            }
                        }
                    };

                    new Thread(runnable).start();
                } else {
                    HashMap<String, String> ftpclient = new HashMap<>();
                    ftpclient.put("servernickname", servernickname.getText().toString());
                    ftpclient.put("hostname", hostname.getText().toString());
                    ftpclient.put("port", port.getText().toString());
                    ftpclient.put("username", username.getText().toString());
                    ftpclient.put("password", password.getText().toString());
                    FirebaseHelper.updateFTPClient(servernickname.getText().toString(), ftpclient);
                    progressDialog.dismiss();
                    displayToast("Server added successfully");
                    finish();
                }
            }
        });
    }

}
