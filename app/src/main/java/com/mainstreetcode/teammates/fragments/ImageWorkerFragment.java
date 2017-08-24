package com.mainstreetcode.teammates.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.theartofdev.edmodo.cropper.CropImage;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

/**
 * Inner fragment hosting code for interacting with image and cropping APIs
 */

public class ImageWorkerFragment extends Fragment {

    public static final int GALLERY_CHOOSER = 1;
    public static final String TAG = "ImageWorkerFragment";

    public static ImageWorkerFragment newInstance() {
        return new ImageWorkerFragment();
    }

    public static void attach(BaseFragment host) {
        if (getInstance(host) != null) return;

        ImageWorkerFragment instance = ImageWorkerFragment.newInstance();

        host.getChildFragmentManager().beginTransaction()
                .add(instance, makeTag(host))
                .commit();
    }

    public static void requestCrop(BaseFragment host) {
        ImageWorkerFragment instance = getInstance(host);

        if (instance == null) return;

        boolean noPermit = SDK_INT >= M && ContextCompat.checkSelfPermission(host.getActivity(),
                WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;

        if (noPermit)
            instance.requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, GALLERY_CHOOSER);
        else instance.startImagePicker();
    }

    public static void detach(BaseFragment host) {
//        if (getInstance(host) != null) return;
//
//        ImageWorkerFragment instance = ImageWorkerFragment.newInstance();
//        instance.setTargetFragment(host, ImageWorkerFragment.GALLERY_CHOOSER);
//
//        host.getFragmentManager().beginTransaction()
//                .remove(instance)
//                .commit();
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case GALLERY_CHOOSER:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startImagePicker();
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Fragment target = getParentFragment();

        if (target == null || (!(target instanceof CropListener))) return;

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_CHOOSER) {
                CropImage.activity(data.getData())
                        .setFixAspectRatio(true)
                        .setAspectRatio(1, 1)
                        .setMinCropWindowSize(80, 80)
                        .setMaxCropResultSize(1000, 1000)
                        .setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                        .start(getContext(), this);
            }
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                ((CropListener) target).onImageCropped(resultUri);
            }
        }
    }

    private static String makeTag(BaseFragment host) {
        return TAG + "-" + host.getStableTag();
    }

    @Nullable
    private static ImageWorkerFragment getInstance(BaseFragment host) {
        return (ImageWorkerFragment) host.getChildFragmentManager().findFragmentByTag(makeTag(host));
    }

    private void startImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_CHOOSER);
    }

    public interface CropListener {
        void onImageCropped(Uri uri);
    }

    public interface ImagePickerListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onImageClick();
    }
}
