package com.mkchaudh.nnataraj.orangeftp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FolderItemFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FolderItemFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static FolderItemFragment newInstance(int columnCount) {
        FolderItemFragment fragment = new FolderItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    class FetchFTPFileList extends AsyncTask<FTPClient, Void, FTPFile[]> {

        private final WeakReference<MyFolderItemRecyclerViewAdapter> folderItemRecyclerViewAdapterWeakReference;
        private final WeakReference<String> hostnameReference, usernameReference, passwordReference;
        private final WeakReference<Integer> portReference;
        private final WeakReference<List<FTPFile>> fileListReference;

        FetchFTPFileList(final String hostname, final Integer port, final String username, final String password, final List<FTPFile> fileList, final MyFolderItemRecyclerViewAdapter folderItemRecyclerViewAdapter) {
            hostnameReference = new WeakReference<>(hostname);
            portReference = new WeakReference<>(port);
            usernameReference = new WeakReference<>(username);
            passwordReference = new WeakReference<>(password);
            fileListReference = new WeakReference<>(fileList);
            folderItemRecyclerViewAdapterWeakReference = new WeakReference<>(folderItemRecyclerViewAdapter);
        }

        @Override
        protected FTPFile[] doInBackground(FTPClient... ftpClients) {
            try {
                if (!ftpClients[0].isConnected()) {
                    final String hostname = hostnameReference.get(), username = usernameReference.get(), password = passwordReference.get();
                    final Integer port = portReference.get();

                    if (hostname == null
                            || username == null
                            || password == null
                            || port == null)
                        return null;


                    ftpClients[0].connect(hostname, port);
                    ftpClients[0].login(username, password);
                }
                ftpClients[0].enterLocalPassiveMode();
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
                    for (FTPFile ftpFile : ftpFiles) {
                        fileList.add(ftpFile);
                    }
                    folderItemRecyclerViewAdapter.notifyDataSetChanged();
                }
            }
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
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            List<FTPFile> items = new ArrayList<>();
            MyFolderItemRecyclerViewAdapter folderItemRecyclerViewAdapter = new MyFolderItemRecyclerViewAdapter(items, mListener);
            recyclerView.setAdapter(folderItemRecyclerViewAdapter);
            new FetchFTPFileList("192.168.1.1", 21, "xxxxxx", "xxxxxxx", items, folderItemRecyclerViewAdapter)
                    .execute(new FTPClient());
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
