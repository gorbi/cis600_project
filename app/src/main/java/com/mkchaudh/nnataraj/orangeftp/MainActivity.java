package com.mkchaudh.nnataraj.orangeftp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mkchaudh.nnataraj.orangeftp.data.FTPConnectionCacher;
import com.mkchaudh.nnataraj.orangeftp.data.FilenameHelper;
import com.mkchaudh.nnataraj.orangeftp.data.FirebaseHelper;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements
        FolderItemFragment.OnListFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String ARG_CONTENT = "content";
    private Fragment mContent = null;

    private static final String ARG_CURRENT_DIRECTORY = "currentDirectory";
    private String mCurrentDirectory = "/";

    private static final String ARG_CURRENT_FTP_SERVER_NICKNAME = "currentFtpServerNickname";
    private String mCurrentFtpServerNickname = null;

    private static final String ARG_CURRENT_FILE_PATH = "currentFilePath";
    private String mCurrentFilePath = null;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_UPLOAD_PHOTO = 2;
    private static final int REQUEST_DOWNLOAD_FILE = 3;
    private static final int REQUEST_LOAD_DATA_FROM_CLOUD = 4;
    private static final int REQUEST_UPDATE_FTPCLIENT = 5;

    public static final int RESULT_FAILURE = -143;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        if (item.getItemId() == R.id.nav_add) {
            startActivityForResult(new Intent(this, UpdateFTPClientActivity.class), REQUEST_UPDATE_FTPCLIENT);
        } else {
            if (mCurrentFtpServerNickname != item.getTitle().toString()) {
                mCurrentFtpServerNickname = item.getTitle().toString();

                mContent = FolderItemFragment.newInstance(FirebaseHelper.getFTPClient(mCurrentFtpServerNickname), mCurrentDirectory);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, mContent)
                        .commitAllowingStateLoss();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

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
            mCurrentFtpServerNickname = savedInstanceState.getString(ARG_CURRENT_FTP_SERVER_NICKNAME);
        } else {
            startActivityForResult(new Intent(this, LoadDataActivity.class), REQUEST_LOAD_DATA_FROM_CLOUD);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeader = navigationView.getHeaderView(0);

        navigationHeader.findViewById(R.id.signOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                FirebaseHelper.reset();
                FTPConnectionCacher.reset();
                FilenameHelper.reset();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        ((TextView) navigationHeader.findViewById(R.id.name)).setText(user.getDisplayName());
        ((TextView) navigationHeader.findViewById(R.id.email)).setText(user.getEmail());

        Glide.with(this).load(user.getPhotoUrl()).into((ImageView) navigationHeader.findViewById(R.id.photo));

        refreshFTPClientList();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        try {
            getSupportFragmentManager().putFragment(outState, ARG_CONTENT, mContent);
        } catch (Exception ae) {

        }

        outState.putString(ARG_CURRENT_DIRECTORY, mCurrentDirectory);

        if (mCurrentFilePath != null)
            outState.putString(ARG_CURRENT_FILE_PATH, mCurrentFilePath);

        if (mCurrentFtpServerNickname != null)
            outState.putString(ARG_CURRENT_FTP_SERVER_NICKNAME, mCurrentFtpServerNickname);
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

        FilenameHelper.put(mCurrentFilePath, filename);

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

    private void refreshFTPClientList() {
        Menu menu = ((NavigationView) findViewById(R.id.nav_view)).getMenu();

        HashMap<String, HashMap<String, String>> ftpClients = FirebaseHelper.getFtpClients();
        Set<String> keys = ftpClients.keySet();

        menu.removeGroup(R.id.ftpclientgroup);

        for (String key : keys) {
            menu.add(R.id.ftpclientgroup, Menu.NONE, menu.size() + 1, key).setIcon(R.drawable.server);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DOWNLOAD_FILE && resultCode == RESULT_OK) {
            Log.d("MainActivity", "Successfully downloaded the file to " + mCurrentFilePath);
            if (mCurrentFilePath.endsWith(".jpg") || mCurrentFilePath.endsWith(".png")) {
                Intent intent = new Intent(this, ViewImageActivity.class);
                intent.putExtra(ViewImageActivity.IMAGE_PATH, mCurrentFilePath);
                startActivity(intent);
                /*Intent intent = new Intent(this, ImageGalleryActivity.class);
                String []images = {mCurrentFilePath};
                intent.putExtra(ImageGalleryActivity.ARRAY_IMAGE_PATHS, images);
                startActivity(intent);*/
            } else if (mCurrentFilePath.endsWith(".mp4")) {
                Intent intent = new Intent(this, ViewVideoActivity.class);
                intent.putExtra(ViewVideoActivity.VIDEO_PATH, mCurrentFilePath);
                startActivity(intent);
            } else if (mCurrentFilePath.endsWith(".txt")) {
                Intent intent = new Intent(this, ViewTextActivity.class);
                intent.putExtra(ViewTextActivity.FILE_PATH, mCurrentFilePath);
                startActivity(intent);
            }
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK && mCurrentFilePath != null) {
            Intent intent = new Intent(this, UploadFileActivity.class);
            intent.putExtra(UploadFileActivity.FTP_SERVER_NICKNAME, mCurrentFtpServerNickname);
            intent.putExtra(UploadFileActivity.FULL_LOCAL_FILEPATH, mCurrentFilePath);
            intent.putExtra(UploadFileActivity.FULL_REMOTE_FILEPATH, mCurrentDirectory + "/" + mCurrentFilePath.substring(mCurrentFilePath.lastIndexOf("/") + 1));

            startActivityForResult(intent, REQUEST_UPLOAD_PHOTO);
        }

        if (requestCode == REQUEST_UPLOAD_PHOTO && resultCode == RESULT_OK) {
            if (mContent instanceof FolderItemFragment) {
                ((FolderItemFragment) mContent).refresh();
            }
        }

        if (requestCode == REQUEST_LOAD_DATA_FROM_CLOUD || requestCode == REQUEST_UPDATE_FTPCLIENT) {
            refreshFTPClientList();
        }
    }

    @Override
    public void onListFragmentInteraction(FTPFile item, String currentDirectory) {
        if (item.isDirectory()) {
            //Edge condition will add double forward slashes
            if (currentDirectory.length() == 1) currentDirectory = "";

            currentDirectory += "/" + item.getName();

            mContent = FolderItemFragment
                    .newInstance(FirebaseHelper.getFTPClient(mCurrentFtpServerNickname), currentDirectory);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, mContent)
                    .addToBackStack("store")
                    .commitAllowingStateLoss();
        }

        if (item.getName().endsWith(".jpg") || item.getName().endsWith(".png") || item.getName().endsWith(".mp4") || item.getName().endsWith(".txt")) {

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
                intent.putExtra(UploadFileActivity.FTP_SERVER_NICKNAME, mCurrentFtpServerNickname);
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

    @Override
    public void triggerImageGallery(String[] filename) {
        Intent intent = new Intent(this, ImageGalleryActivity.class);
        intent.putExtra(ImageGalleryActivity.FTP_SERVER_NICKNAME, mCurrentFtpServerNickname);
        intent.putExtra(ImageGalleryActivity.ARRAY_IMAGE_PATHS, filename);
        startActivity(intent);
    }
}
