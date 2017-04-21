package com.mkchaudh.nnataraj.orangeftp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import com.google.firebase.database.*;
import org.apache.commons.net.ftp.FTPFile;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements FolderItemFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /*Map<String, Map> ftpclients = new HashMap<>();

        Map<String,String> ftpclient = new HashMap<>();
        ftpclient.put("servernickname","myserverroomies");
        ftpclient.put("hostname","192.168.1.1");
        ftpclient.put("port","21");
        ftpclient.put("username","xxxxxxx");
        ftpclient.put("password","xxxxxxx");
        ftpclients.put("myserverroomies",ftpclient);
        ftpclient = new HashMap<>();
        ftpclient.put("servernickname","myserverorangeftp");
        ftpclient.put("hostname","192.168.1.1");
        ftpclient.put("port","21");
        ftpclient.put("username","xxxxxxx");
        ftpclient.put("password","xxxxxxx");
        ftpclients.put("myserverorangeftp",ftpclient);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("ftpclients").setValue(ftpclients); */

        DatabaseReference ftpServerDetails = FirebaseDatabase.getInstance().getReference("ftpclients").child("myserverorangeftp");
        ftpServerDetails.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final HashMap<String, String> ftpServerDetails = (HashMap<String, String>) dataSnapshot.getValue();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, FolderItemFragment.newInstance(ftpServerDetails, "/"))
                        .commit();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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
            final String newCurrentDirectory = currentDirectory + "/" + item.getName();
            DatabaseReference ftpServerDetails = FirebaseDatabase.getInstance().getReference("ftpclients").child("myserverorangeftp");
            ftpServerDetails.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final HashMap<String, String> ftpServerDetails = (HashMap<String, String>) dataSnapshot.getValue();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment, FolderItemFragment.newInstance(ftpServerDetails, newCurrentDirectory))
                            .addToBackStack("store")
                            .commit();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
