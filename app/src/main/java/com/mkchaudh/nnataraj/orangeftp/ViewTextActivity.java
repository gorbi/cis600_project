package com.mkchaudh.nnataraj.orangeftp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.mkchaudh.nnataraj.orangeftp.data.FilenameHelper;

import java.io.*;

public class ViewTextActivity extends AppCompatActivity {

    public static final String FILE_PATH = "filePath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_text);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            final String filePath = extras.getString(FILE_PATH);
            if (filePath != null) {
                setTitle(FilenameHelper.get(filePath));
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));

                    StringBuilder fileContent = new StringBuilder();
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        fileContent.append(line).append("\n");
                    }
                    bufferedReader.close();

                    TextView content = (TextView) findViewById(R.id.content);

                    content.setText(fileContent.toString());

                    content.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {

                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                if (new File(filePath).delete())
                                    Log.d("ViewTextActivity", "Deleted text file at " + filePath);
                                FilenameHelper.reset();
                            }

                            return false;
                        }
                    });

                } catch (IOException ae) {
                    StringWriter stackTrace = new StringWriter();
                    ae.printStackTrace(new PrintWriter(stackTrace));
                    Log.e("ViewTextActivity", stackTrace.toString());
                    finish();
                }
            }
        }
    }

}

