package com.mkchaudh.nnataraj.orangeftp;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.mkchaudh.nnataraj.orangeftp.data.FTPConnectionCacher;
import com.mkchaudh.nnataraj.orangeftp.data.FilenameHelper;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class ImageGalleryActivity extends AppCompatActivity {

    public static final String ARRAY_IMAGE_PATHS = "arrayImagePaths";
    public static final String FTP_SERVER_NICKNAME = "ftpServerNickname";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private String ftpServerNickname = null;
    private ArrayList<String> imagePaths = null;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private String createFile(String filename) throws IOException {

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

        FilenameHelper.put(file.getAbsolutePath(), filename);

        return file.getAbsolutePath();
    }

    public void setDownloadProgress(int value) {
        progressDialog.setProgress(value);
    }

    class DownloadFiles extends AsyncTask<String, Integer, Void> {

        final WeakReference<ProgressDialog> progressDialogWeakReference;

        DownloadFiles(final ProgressDialog progressDialog) {
            progressDialogWeakReference = new WeakReference<>(progressDialog);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            setDownloadProgress(values[0]);
        }

        @Override
        protected Void doInBackground(String... strings) {
            imagePaths.clear();
            for (int i = 0; i < strings.length; i++) {
                BufferedOutputStream bufferedOutputStream = null;
                try {
                    String localpath = createFile(new File(strings[i]).getName());
                    bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localpath));

                    FTPClient ftpClient = FTPConnectionCacher.getFTPConnection(ftpServerNickname);
                    FTPConnectionCacher.refreshFTPConnection(ftpServerNickname, ftpClient);

                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                    ftpClient.retrieveFile(strings[i], bufferedOutputStream);
                    bufferedOutputStream.close();

                    imagePaths.add(localpath);
                } catch (Exception ae) {
                    StringWriter stackTrace = new StringWriter();
                    ae.printStackTrace(new PrintWriter(stackTrace));
                    Log.e("DownloadFiles", stackTrace.toString());
                } finally {
                    try {
                        bufferedOutputStream.close();
                    } catch (Exception ae) {

                    }

                }
                publishProgress(i + 1);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final ProgressDialog progressDialog = progressDialogWeakReference.get();
            if (progressDialog != null) {
                progressDialog.dismiss();
                display();
            }
        }
    }

    private void display() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            imagePaths = new ArrayList<>();
            Collections.addAll(imagePaths,extras.getStringArray(ARRAY_IMAGE_PATHS));
            ftpServerNickname = extras.getString(FTP_SERVER_NICKNAME);
            if (imagePaths != null && imagePaths.size() > 0 && ftpServerNickname != null) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Downloading...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);
                progressDialog.show();
                progressDialog.setMax(imagePaths.size());

                new DownloadFiles(progressDialog).execute(imagePaths.toArray(new String[imagePaths.size()]));
            } else {
                finish();
            }
        } else {
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_gallery, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_IMAGE_PATH = "image_path";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(String imagePath) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_IMAGE_PATH, imagePath);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_image_gallery, container, false);

            Uri imageUri = FileProvider.getUriForFile(getActivity(), "com.mkchaudh.nnataraj.orangeftp", new File(getArguments().getString(ARG_IMAGE_PATH)));

            ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);

            Glide.with(getActivity()).load(imageUri).into(imageView);

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(imagePaths.get(position));
        }

        @Override
        public int getCount() {
            return imagePaths.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return FilenameHelper.get(imagePaths.get(position));
        }
    }
}
