package com.mkchaudh.nnataraj.orangeftp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mkchaudh.nnataraj.orangeftp.FolderItemFragment.OnListFragmentInteractionListener;
import com.mkchaudh.nnataraj.orangeftp.data.Utilities;
import org.apache.commons.net.ftp.FTPFile;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FTPFile} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyFolderItemRecyclerViewAdapter extends RecyclerView.Adapter<MyFolderItemRecyclerViewAdapter.ViewHolder> {

    private final List<FTPFile> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final String mCurrentDirectory;

    public MyFolderItemRecyclerViewAdapter(List<FTPFile> items, String currentDirectory, OnListFragmentInteractionListener listener) {
        mValues = items;
        mCurrentDirectory = currentDirectory;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_folderitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        if (mValues.get(position).isDirectory())
            holder.mIconView.setImageResource(R.mipmap.folder);
        holder.mNameView.setText(mValues.get(position).getName());
        holder.mSizeView.setText(Utilities.getReadableSize(mValues.get(position).getSize()));
        holder.mTimestampView.setText(mValues.get(position).getTimestamp().getTime().toString());
        holder.mOwnerView.setText("Owner: " + mValues.get(position).getUser());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem, mCurrentDirectory);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mIconView;
        public final TextView mNameView, mSizeView, mTimestampView, mOwnerView;
        public FTPFile mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIconView = (ImageView) view.findViewById(R.id.icon);
            mNameView = (TextView) view.findViewById(R.id.name);
            mSizeView = (TextView) view.findViewById(R.id.size);
            mTimestampView = (TextView) view.findViewById(R.id.timestamp);
            mOwnerView = (TextView) view.findViewById(R.id.owner);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
