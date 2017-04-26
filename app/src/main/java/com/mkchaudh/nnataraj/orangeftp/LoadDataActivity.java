package com.mkchaudh.nnataraj.orangeftp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mkchaudh.nnataraj.orangeftp.data.FirebaseHelper;

import java.util.HashMap;

import static com.mkchaudh.nnataraj.orangeftp.MainActivity.RESULT_FAILURE;

public class LoadDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DatabaseReference ftpClientsRef = FirebaseHelper.getFtpClientsRef();

        setResult(RESULT_FAILURE);

        if (ftpClientsRef != null) {
            ftpClientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    setResult(RESULT_OK);
                    if (dataSnapshot.getValue() != null) {
                        FirebaseHelper.updateFTPClients((HashMap<String, HashMap<String, String>>) dataSnapshot.getValue());
                    }
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    finish();
                }
            });
        }

    }
}
