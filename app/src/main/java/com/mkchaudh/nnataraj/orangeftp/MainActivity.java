package com.mkchaudh.nnataraj.orangeftp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.mkchaudh.nnataraj.orangeftp.data.FirebaseHelper;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements FolderItemFragment.OnListFragmentInteractionListener {

    private static final String ARG_CONTENT = "content";
    private Fragment mContent = null;
    public static final int RESULT_FAILURE = -143;
    private static final int REQUEST_DOWNLOAD_FILE = 1;
    private String mCurrentFilePath = null;

    private void changeFragment(Fragment mContent, boolean toBackStack) {

        this.mContent = mContent;

        if (toBackStack) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, mContent)
                    .addToBackStack("store")
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, mContent)
                    .commit();
        }

        //getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, ARG_CONTENT);
        } else {

            startActivity(new Intent(this, LoadDataActivity.class));

            HashMap<String, String> ftpclient = new HashMap<>();
            ftpclient.put("servernickname", "myserverorangeftp");
            ftpclient.put("hostname", "192.168.1.1");
            ftpclient.put("port", "21");
            ftpclient.put("username", "orangeftp");
            ftpclient.put("password", "cis600android");
            FirebaseHelper.updateFTPClient("myserverorangeftp", ftpclient);

            changeFragment(FolderItemFragment
                    .newInstance(FirebaseHelper.getFTPClient("myserverorangeftp"), "/"),false);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mContent != null)
            getSupportFragmentManager().putFragment(outState, ARG_CONTENT, mContent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private File createFile(String filename) throws IOException {

        String suffix;
        try {
            suffix = "." + filename.split("\\.")[1];
        } catch (ArrayIndexOutOfBoundsException ae) {
            suffix = "";
        }

        File storageDir = getCacheDir();
        File file = File.createTempFile(
                UUID.randomUUID().toString(),  /* prefix */
                suffix,         /* suffix */
                storageDir      /* directory */
        );

        mCurrentFilePath = file.getAbsolutePath();
        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DOWNLOAD_FILE && resultCode == RESULT_OK) {
            Log.d("MainActivity", "Successfully downloaded the file to "+mCurrentFilePath);
            changeFragment(ViewImageFragment.newInstance(mCurrentFilePath),true);
        }
    }

    @Override
    public void onListFragmentInteraction(FTPFile item, String currentDirectory) {
        if (item.isDirectory()) {
            //Edge condition will add double forward slashes
            if (currentDirectory.length() == 1) currentDirectory = "";

            currentDirectory += "/" + item.getName();

            changeFragment(FolderItemFragment
                    .newInstance(FirebaseHelper.getFTPClient("myserverorangeftp"), currentDirectory), true);
        }

        if (item.getName().endsWith("jpg") || item.getName().endsWith("png")) {

            File localItem = null;

            try {
                localItem = createFile(item.getName());
            } catch (IOException ae) {
                // Error occurred while creating the File
                StringWriter stackTrace = new StringWriter();
                ae.printStackTrace(new PrintWriter(stackTrace));
                Log.e("MainActivity", stackTrace.toString());
            }

            if (localItem != null) {

                Intent intent = new Intent(this, DownloadFileActivity.class);
                intent.putExtra(UploadFileActivity.FTP_SERVER_NICKNAME, "myserverorangeftp");
                intent.putExtra(UploadFileActivity.FULL_LOCAL_FILEPATH, mCurrentFilePath);
                intent.putExtra(UploadFileActivity.FULL_REMOTE_FILEPATH, currentDirectory + "/" + item.getName());

                startActivityForResult(intent, REQUEST_DOWNLOAD_FILE);
            }

        }
    }
}
