package com.mkchaudh.nnataraj.orangeftp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.mkchaudh.nnataraj.orangeftp.data.FirebaseHelper;
import org.apache.commons.net.ftp.FTPFile;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements FolderItemFragment.OnListFragmentInteractionListener {

    private static final String ARG_CONTENT = "content";
    private Fragment mContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, ARG_CONTENT);
        } else {

            startActivity(new Intent(this, LoadDataSplashActivity.class));

            HashMap<String, String> ftpclient = new HashMap<>();
            ftpclient.put("servernickname", "myserverorangeftp");
            ftpclient.put("hostname", "192.168.1.1");
            ftpclient.put("port", "21");
            ftpclient.put("username", "orangeftp");
            ftpclient.put("password", "cis600android");
            FirebaseHelper.updateFTPClient("myserverorangeftp", ftpclient);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, mContent = FolderItemFragment
                            .newInstance(FirebaseHelper.getFTPClient("myserverorangeftp"), "/"))
                    .commit();
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

    @Override
    public void onListFragmentInteraction(FTPFile item, String currentDirectory) {
        if (item.isDirectory()) {
            //Edge condition will add double forward slashes
            if (currentDirectory.length() == 1) currentDirectory = "";

            currentDirectory += "/" + item.getName();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, mContent = FolderItemFragment
                            .newInstance(FirebaseHelper.getFTPClient("myserverorangeftp"), currentDirectory))
                    .addToBackStack("store")
                    .commit();

        }
    }
}
