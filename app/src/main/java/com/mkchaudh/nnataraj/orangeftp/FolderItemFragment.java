package com.mkchaudh.nnataraj.orangeftp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mkchaudh.nnataraj.orangeftp.data.FTPConnectionCacher;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.*;

import static android.app.Activity.RESULT_OK;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FolderItemFragment extends Fragment {

    private static final String ARG_FTP_SERVER_DETAILS = "ftp-server-details";
    private static final String ARG_CURRENT_DIRECTORY = "current-directory";
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_UPLOAD_PHOTO = 2;
    private String mCurrentPhotoPath = null;
    private HashMap<String, String> mFtpServerDetails = null;
    private String mCurrentDirectory = null;
    private OnListFragmentInteractionListener mListener;
    private List<FTPFile> items = null;
    private MyFolderItemRecyclerViewAdapter folderItemRecyclerViewAdapter = null;
    private RecyclerView recyclerView = null;

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
                try {
                    ftpClients[0].changeWorkingDirectory(mCurrentDirectory);
                } catch (Exception ae) {
                    final HashMap<String, String> ftpServerDetails = ftpServerDetailsReference.get();

                    if (ftpServerDetails == null)
                        return null;

                    ftpClients[0].connect(ftpServerDetails.get("hostname"), Integer.parseInt(ftpServerDetails.get("port")));
                    ftpClients[0].login(ftpServerDetails.get("username"), ftpServerDetails.get("password"));
                    ftpClients[0].enterLocalPassiveMode();
                    ftpClients[0].changeWorkingDirectory(mCurrentDirectory);
                    FTPConnectionCacher.updateFTPConnection(ftpServerDetails.get("servernickname"), ftpClients[0]);
                }

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
                    Collections.addAll(fileList, ftpFiles);
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use to upload
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK && mCurrentPhotoPath != null) {
            Intent intent = new Intent(getActivity(), UploadFileActivity.class);
            intent.putExtra(UploadFileActivity.FTP_SERVER_NICKNAME, mFtpServerDetails.get("servernickname"));
            intent.putExtra(UploadFileActivity.FULL_LOCAL_FILEPATH, mCurrentPhotoPath);
            intent.putExtra(UploadFileActivity.FULL_REMOTE_FILEPATH, mCurrentDirectory + "/" + mCurrentPhotoPath.substring(mCurrentPhotoPath.lastIndexOf("/") + 1));

            startActivityForResult(intent, REQUEST_UPLOAD_PHOTO);
        }

        if (requestCode == REQUEST_UPLOAD_PHOTO && resultCode == RESULT_OK) {
            items = new ArrayList<>();
            folderItemRecyclerViewAdapter = new MyFolderItemRecyclerViewAdapter(items, mCurrentDirectory, mListener);
            recyclerView.setAdapter(folderItemRecyclerViewAdapter);
            recyclerView.addItemDecoration(new VerticalSpaceItemDecoration());
            new FetchFTPFileList(mFtpServerDetails, items, folderItemRecyclerViewAdapter)
                    .execute(FTPConnectionCacher.getFTPConnection(mFtpServerDetails.get("servernickname")));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folderitem_list, container, false);

        ((TextView) view.findViewById(R.id.path)).setText(mCurrentDirectory);

        view.findViewById(R.id.fab_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ae) {
                        // Error occurred while creating the File
                        StringWriter stackTrace = new StringWriter();
                        ae.printStackTrace(new PrintWriter(stackTrace));
                        Log.e("FetchFTPFileList", stackTrace.toString());
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(getActivity(),
                                "com.mkchaudh.nnataraj.orangeftp",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });

        // Set the adapter
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        items = new ArrayList<>();
        folderItemRecyclerViewAdapter = new MyFolderItemRecyclerViewAdapter(items, mCurrentDirectory, mListener);
        recyclerView.setAdapter(folderItemRecyclerViewAdapter);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration());
        new FetchFTPFileList(mFtpServerDetails, items, folderItemRecyclerViewAdapter)
                .execute(FTPConnectionCacher.getFTPConnection(mFtpServerDetails.get("servernickname")));

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
        void onListFragmentInteraction(FTPFile item, String currentDirectory);
    }
}
