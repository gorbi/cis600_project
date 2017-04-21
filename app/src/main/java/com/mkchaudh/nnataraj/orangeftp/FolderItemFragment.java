package com.mkchaudh.nnataraj.orangeftp;

import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mkchaudh.nnataraj.orangeftp.data.FTPClientCacher;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FolderItemFragment extends Fragment {

    private static final String ARG_FTP_SERVER_DETAILS = "ftp-server-details";
    private static final String ARG_CURRENT_DIRECTORY = "current-directory";
    private HashMap<String, String> mFtpServerDetails = null;
    private String mCurrentDirectory = null;
    private OnListFragmentInteractionListener mListener;

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

        private final WeakReference<MyFolderItemRecyclerViewAdapter> folderItemRecyclerViewAdapterWeakReference;
        private final WeakReference<HashMap<String, String>> ftpServerDetailsReference;
        private final WeakReference<List<FTPFile>> fileListReference;

        FetchFTPFileList(final HashMap<String, String> ftpServerDetails, final List<FTPFile> fileList, final MyFolderItemRecyclerViewAdapter folderItemRecyclerViewAdapter) {
            ftpServerDetailsReference = new WeakReference<>(ftpServerDetails);
            fileListReference = new WeakReference<>(fileList);
            folderItemRecyclerViewAdapterWeakReference = new WeakReference<>(folderItemRecyclerViewAdapter);
        }

        @Override
        protected FTPFile[] doInBackground(FTPClient... ftpClients) {
            try {

                String servernickname = null;

                if (!ftpClients[0].isConnected()) {
                    final HashMap<String, String> ftpServerDetails = ftpServerDetailsReference.get();

                    if (ftpServerDetails == null)
                        return null;

                    servernickname = ftpServerDetails.get("servernickname");

                    ftpClients[0].connect(ftpServerDetails.get("hostname"), Integer.parseInt(ftpServerDetails.get("port")));
                    ftpClients[0].login(ftpServerDetails.get("username"), ftpServerDetails.get("password"));
                }
                ftpClients[0].enterLocalPassiveMode();

                if (servernickname != null)
                    FTPClientCacher.updateFTPClient(servernickname, ftpClients[0]);

                ftpClients[0].changeWorkingDirectory(mCurrentDirectory);

                return ftpClients[0].listFiles();
            } catch (IOException ae) {
                StringWriter stackTrace = new StringWriter();
                ae.printStackTrace(new PrintWriter(stackTrace));
                Log.e("FetchFTPFileList", stackTrace.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(FTPFile[] ftpFiles) {
            if (ftpFiles != null) {
                final List<FTPFile> fileList = fileListReference.get();
                final MyFolderItemRecyclerViewAdapter folderItemRecyclerViewAdapter = folderItemRecyclerViewAdapterWeakReference.get();

                if (fileList != null && folderItemRecyclerViewAdapter != null) {
                    fileList.clear();
                    Collections.addAll(fileList,ftpFiles);
                    folderItemRecyclerViewAdapter.notifyDataSetChanged();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folderitem_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            List<FTPFile> items = new ArrayList<>();
            MyFolderItemRecyclerViewAdapter folderItemRecyclerViewAdapter = new MyFolderItemRecyclerViewAdapter(items, mListener);
            recyclerView.setAdapter(folderItemRecyclerViewAdapter);
            recyclerView.addItemDecoration(new VerticalSpaceItemDecoration());
            new FetchFTPFileList(mFtpServerDetails, items, folderItemRecyclerViewAdapter)
                    .execute(FTPClientCacher.getFTPClient(mFtpServerDetails.get("servernickname")));
        }
        return view;
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
        // TODO: Update argument type and name
        void onListFragmentInteraction(FTPFile item);
    }
}
