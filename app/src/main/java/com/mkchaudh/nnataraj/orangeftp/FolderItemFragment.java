package com.mkchaudh.nnataraj.orangeftp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import com.mkchaudh.nnataraj.orangeftp.data.FTPConnectionCacher;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FolderItemFragment extends Fragment {

    private static final String ARG_FTP_SERVER_DETAILS = "ftp-server-details";
    private HashMap<String, String> mFtpServerDetails = null;

    private static final String ARG_CURRENT_DIRECTORY = "current-directory";
    private String mCurrentDirectory = null;

    private OnListFragmentInteractionListener mListener;

    List<FTPFile> items;
    MyFolderItemRecyclerViewAdapter folderItemRecyclerViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FolderItemFragment() {
    }

    public static FolderItemFragment newInstance(HashMap<String, String> ftpServerDetails, String currentDirectory) {
        FolderItemFragment fragment = new FolderItemFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FTP_SERVER_DETAILS, ftpServerDetails);
        args.putString(ARG_CURRENT_DIRECTORY, currentDirectory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mFtpServerDetails = (HashMap<String, String>) getArguments().getSerializable(ARG_FTP_SERVER_DETAILS);
            mCurrentDirectory = getArguments().getString(ARG_CURRENT_DIRECTORY);
        }
    }

    class FetchFTPFileList extends AsyncTask<FTPClient, Void, FTPFile[]> {

        @Override
        protected FTPFile[] doInBackground(FTPClient... ftpClients) {
            try {
                try {
                    ftpClients[0].changeWorkingDirectory(mCurrentDirectory);
                } catch (Exception ae) {
                    ftpClients[0].connect(mFtpServerDetails.get("hostname"), Integer.parseInt(mFtpServerDetails.get("port")));
                    ftpClients[0].login(mFtpServerDetails.get("username"), mFtpServerDetails.get("password"));
                    ftpClients[0].enterLocalPassiveMode();
                    ftpClients[0].changeWorkingDirectory(mCurrentDirectory);
                    FTPConnectionCacher.updateFTPConnection(mFtpServerDetails.get("servernickname"), ftpClients[0]);
                }

                return ftpClients[0].listFiles();
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
                Log.e("FetchFTPFileList", stackTrace.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(FTPFile[] ftpFiles) {
            if (ftpFiles != null) {
                items.clear();
                Collections.addAll(items, ftpFiles);
                folderItemRecyclerViewAdapter.notifyDataSetChanged();

                boolean isGallery = true;

                String[] filename = new String[ftpFiles.length];

                for (int i = 0; i < ftpFiles.length; i++) {
                    if (!(ftpFiles[i].getName().toLowerCase().endsWith(".jpg") || ftpFiles[i].getName().toLowerCase().endsWith(".png"))) {
                        isGallery = false;
                        break;
                    }
                    filename[i] = mCurrentDirectory + "/" + ftpFiles[i].getName();
                }

                if (isGallery) {
                    mListener.triggerImageGallery(filename);
                }
            }
        }
    }


    private class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = 10;
            }
            outRect.bottom = 10;
            outRect.left = 10;
            outRect.right = 10;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_folderitem, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_displayfolderdetails:
                mListener.getFolderInfo(mCurrentDirectory, mFtpServerDetails.get("servernickname"));
                return true;
            case R.id.action_update_ftp_client:
                Intent intent = new Intent(getActivity(), UpdateFTPClientActivity.class);
                intent.putExtra(UpdateFTPClientActivity.FTP_SERVER_NICKNAME, mFtpServerDetails.get("servernickname"));
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_folderitem_list, container, false);

        ((TextView) view.findViewById(R.id.path)).setText(mCurrentDirectory);

        view.findViewById(R.id.fab_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCameraFABClick(v, mCurrentDirectory);
            }
        });

        // Set the adapter
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        items = new ArrayList<>();
        folderItemRecyclerViewAdapter = new MyFolderItemRecyclerViewAdapter(items, mCurrentDirectory, mListener);
        recyclerView.setAdapter(folderItemRecyclerViewAdapter);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration());
        new FetchFTPFileList()
                .execute(FTPConnectionCacher.getFTPConnection(mFtpServerDetails.get("servernickname")));

        return view;
    }

    public void refresh() {
        new FetchFTPFileList()
                .execute(FTPConnectionCacher.getFTPConnection(mFtpServerDetails.get("servernickname")));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(FTPFile item, String currentDirectory);

        void onCameraFABClick(View view, String currentDirectory);

        void triggerImageGallery(String[] filename);

        void getFolderInfo(String folderPath, String ftpServerNickname);
    }
}
