package com.mkchaudh.nnataraj.orangeftp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mkchaudh.nnataraj.orangeftp.data.FTPConnectionCacher;
import com.mkchaudh.nnataraj.orangeftp.data.FirebaseHelper;
import com.mkchaudh.nnataraj.orangeftp.data.Utilities;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewFolderDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewFolderDetailsFragment extends Fragment {
    private static final String ARG_FOLDER_PATH = "folderPath";
    private static final String ARG_FTP_SERVER_NICKNAME = "ftpServerNickname";

    private String mFolderPath;
    private String mFtpServerNickname;

    public ViewFolderDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param folderPath Path to folder.
     * @return A new instance of fragment ViewFolderDetailsFragment.
     */
    public static ViewFolderDetailsFragment newInstance(String folderPath, String ftpServerNickname) {
        ViewFolderDetailsFragment fragment = new ViewFolderDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FOLDER_PATH, folderPath);
        args.putString(ARG_FTP_SERVER_NICKNAME, ftpServerNickname);
        fragment.setArguments(args);
        return fragment;
    }

    class FetchFTPFile extends AsyncTask<FTPClient, Void, FTPFile> {

        private final WeakReference<View> rootViewWeakReference;

        FetchFTPFile(final View rootview) {
            rootViewWeakReference = new WeakReference<View>(rootview);
        }

        @Override
        protected FTPFile doInBackground(FTPClient... ftpClients) {
            HashMap<String, String> mFtpServerDetails = FirebaseHelper.getFTPClient(mFtpServerNickname);
            try {
                try {
                    ftpClients[0].changeWorkingDirectory(mFolderPath);
                } catch (Exception ae) {
                    ftpClients[0].connect(mFtpServerDetails.get("hostname"), Integer.parseInt(mFtpServerDetails.get("port")));
                    ftpClients[0].login(mFtpServerDetails.get("username"), mFtpServerDetails.get("password"));
                    ftpClients[0].enterLocalPassiveMode();
                    ftpClients[0].changeWorkingDirectory(mFolderPath);
                    FTPConnectionCacher.updateFTPConnection(mFtpServerDetails.get("servernickname"), ftpClients[0]);
                }

                return ftpClients[0].mlistFile(mFolderPath);
            } catch (IOException ae) {
                if (ftpClients[0].isConnected()) {
                    try {
                        ftpClients[0].disconnect();
                    } catch (IOException f) {
                        // do nothing
                    }
                }
                FTPConnectionCacher.updateFTPConnection(mFtpServerDetails.get("servernickname"), new FTPClient());
                StringWriter stackTrace = new StringWriter();
                ae.printStackTrace(new PrintWriter(stackTrace));
                Log.e("FetchFTPFile", stackTrace.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(FTPFile ftpFile) {
            if (ftpFile != null) {
                final View rootView = rootViewWeakReference.get();
                if (rootView != null) {
                    ((TextView) rootView.findViewById(R.id.size)).setText(Utilities.getReadableSize(ftpFile.getSize()));
                    ((TextView) rootView.findViewById(R.id.timestamp)).setText(String.valueOf(ftpFile.getTimestamp().getTime().toString()));
                    ((TextView) rootView.findViewById(R.id.owner)).setText(String.valueOf(ftpFile.getUser()));
                    ((TextView) rootView.findViewById(R.id.raw_listing)).setText(String.valueOf(ftpFile.getRawListing()));
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFolderPath = getArguments().getString(ARG_FOLDER_PATH);
            mFtpServerNickname = getArguments().getString(ARG_FTP_SERVER_NICKNAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_view_folder_details, container, false);

        ((TextView) rootView.findViewById(R.id.full_path)).setText(mFolderPath);

        new FetchFTPFile(rootView).execute(FTPConnectionCacher.getFTPConnection(mFtpServerNickname));

        return rootView;
    }

}
