package com.mkchaudh.nnataraj.orangeftp;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

public class ViewVideoActivity extends AppCompatActivity {

    public static final String VIDEO_PATH = "videoPath";

    private static final String ARG_POSITION = "position";
    private int position = 0;

    private VideoView myVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_video);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            final String videoPath = extras.getString(VIDEO_PATH);
            if (videoPath != null) {

                MediaController mediaController = new MediaController(ViewVideoActivity.this);

                myVideoView = ((VideoView) findViewById(R.id.videoView));

                final ProgressDialog progressDialog = new ProgressDialog(ViewVideoActivity.this);
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                Uri videoUri = FileProvider.getUriForFile(this, "com.mkchaudh.nnataraj.orangeftp", new File(videoPath));

                myVideoView.setMediaController(mediaController);
                myVideoView.setVideoURI(videoUri);
                myVideoView.requestFocus();
                myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        progressDialog.dismiss();
                        myVideoView.seekTo(position);
                        if (position == 0) {
                            myVideoView.start();
                        } else {
                            myVideoView.pause();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(ARG_POSITION, myVideoView.getCurrentPosition());
        myVideoView.pause();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        position = savedInstanceState.getInt(ARG_POSITION);
        myVideoView.seekTo(position);
    }

}
