package com.mkchaudh.nnataraj.orangeftp.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import com.mkchaudh.nnataraj.orangeftp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewImageFragment extends Fragment {
    private static final String ARG_IMAGE_FILEPATH = "imageFilePath";

    private String mImageFilePath;

    public ViewImageFragment() {
        // Required empty public constructor
    }

    private void setPic(ImageView mImageView, int targetW, int targetH) {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImageFilePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mImageFilePath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imageFilePath Image File Path.
     * @return A new instance of fragment ViewImageFragment.
     */
    public static ViewImageFragment newInstance(String imageFilePath) {
        ViewImageFragment fragment = new ViewImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_FILEPATH, imageFilePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImageFilePath = getArguments().getString(ARG_IMAGE_FILEPATH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_image, container, false);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        setPic((ImageView) view.findViewById(R.id.imageView), metrics.widthPixels, metrics.heightPixels);

        return view;
    }
}
