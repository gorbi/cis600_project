package com.mkchaudh.nnataraj.orangeftp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.mkchaudh.nnataraj.orangeftp.data.FirebaseHelper;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements FolderItemFragment.OnListFragmentInteractionListener {

    private static final String ARG_CONTENT = "content";
    private Fragment mContent = null;

    private static final String ARG_CURRENT_DIRECTORY = "currentDirectory";
    private String mCurrentDirectory = null;

    private static final String ARG_CURRENT_FILE_PATH = "currentFilePath";
    private String mCurrentFilePath = null;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_UPLOAD_PHOTO = 2;
    private static final int REQUEST_DOWNLOAD_FILE = 3;
    private static final int REQUEST_LOAD_DATA_FROM_CLOUD = 4;

    public static final int RESULT_FAILURE = -143;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, ARG_CONTENT);
            mCurrentDirectory = savedInstanceState.getString(ARG_CURRENT_DIRECTORY);
            mCurrentFilePath = savedInstanceState.getString(ARG_CURRENT_FILE_PATH);
        } else {

            startActivityForResult(new Intent(this, LoadDataActivity.class), REQUEST_LOAD_DATA_FROM_CLOUD);

            /*HashMap<String, String> ftpclient = new HashMap<>();
            ftpclient.put("servernickname", "myserverorangeftp");
            ftpclient.put("hostname", "192.168.1.1");
            ftpclient.put("port", "21");
            ftpclient.put("username", "orangeftp");
            ftpclient.put("password", "cis600android");
            FirebaseHelper.updateFTPClient("myserverorangeftp", ftpclient);

            mContent = FolderItemFragment.newInstance(FirebaseHelper.getFTPClient("myserverorangeftp"), "/");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, mContent)
                    .commitAllowingStateLoss();*/
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        try {
            getSupportFragmentManager().putFragment(outState, ARG_CONTENT, mContent);
        } catch (Exception ae) {

        }

        if (mCurrentDirectory != null)
            outState.putString(ARG_CURRENT_DIRECTORY, mCurrentDirectory);

        if (mCurrentFilePath != null)
            outState.putString(ARG_CURRENT_FILE_PATH, mCurrentFilePath);
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

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(
                UUID.randomUUID().toString(),  /* prefix */
                suffix,         /* suffix */
                storageDir      /* directory */
        );

        mCurrentFilePath = file.getAbsolutePath();
        return file;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use to upload
        mCurrentFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DOWNLOAD_FILE && resultCode == RESULT_OK) {
            Log.d("MainActivity", "Successfully downloaded the file to " + mCurrentFilePath);
            if (mCurrentFilePath.endsWith(".jpg") || mCurrentFilePath.endsWith(".png")) {
                mContent = ViewImageFragment.newInstance(mCurrentFilePath);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, mContent)
                        .addToBackStack("store")
                        .commitAllowingStateLoss();
            } else {
                Intent intent = new Intent(this, ViewVideoActivity.class);
                intent.putExtra(ViewVideoActivity.VIDEO_PATH, mCurrentFilePath);
                startActivity(intent);
            }
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK && mCurrentFilePath != null) {
            Intent intent = new Intent(this, UploadFileActivity.class);
            intent.putExtra(UploadFileActivity.FTP_SERVER_NICKNAME, "myserverorangeftp");
            intent.putExtra(UploadFileActivity.FULL_LOCAL_FILEPATH, mCurrentFilePath);
            intent.putExtra(UploadFileActivity.FULL_REMOTE_FILEPATH, mCurrentDirectory + "/" + mCurrentFilePath.substring(mCurrentFilePath.lastIndexOf("/") + 1));

            startActivityForResult(intent, REQUEST_UPLOAD_PHOTO);
        }

        if (requestCode == REQUEST_UPLOAD_PHOTO && resultCode == RESULT_OK) {
            if (mContent instanceof FolderItemFragment) {
                ((FolderItemFragment) mContent).refresh();
            }
        }

        if (requestCode == REQUEST_LOAD_DATA_FROM_CLOUD) {
            mContent = FolderItemFragment.newInstance(FirebaseHelper.getFTPClient("myserverorangeftp"), "/");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, mContent)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onListFragmentInteraction(FTPFile item, String currentDirectory) {
        if (item.isDirectory()) {
            //Edge condition will add double forward slashes
            if (currentDirectory.length() == 1) currentDirectory = "";

            currentDirectory += "/" + item.getName();

            mContent = FolderItemFragment
                    .newInstance(FirebaseHelper.getFTPClient("myserverorangeftp"), currentDirectory);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, mContent)
                    .addToBackStack("store")
                    .commitAllowingStateLoss();
        }

        if (item.getName().endsWith(".jpg") || item.getName().endsWith(".png") || item.getName().endsWith(".mp4")) {

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

    @Override
    public void onCameraFABClick(View view, String currentDirectory) {

        mCurrentDirectory = currentDirectory;

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ae) {
                // Error occurred while creating the File
                StringWriter stackTrace = new StringWriter();
                ae.printStackTrace(new PrintWriter(stackTrace));
                Log.e("FetchFTPFileList", stackTrace.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.mkchaudh.nnataraj.orangeftp",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
}
