package com.example.chatapp.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.example.chatapp.R;
import com.example.chatapp.activities.ImageViewerActivity;
import com.example.chatapp.models.Attachment;
import com.example.chatapp.models.AttachmentTypes;
import com.example.chatapp.models.User;
import com.example.chatapp.utils.FirebaseUploader;
import com.example.chatapp.utils.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfileEditDialogFragment extends BaseFullDialogFragment implements ImagePickerCallback {
    private static final int REQUEST_CODE_MEDIA_PERMISSION = 999;
    private static final int REQUEST_CODE_PICKER = 4321;
    private TextView userName, userPhoneNumber;
    private EditText userNameEdit, userStatus;
    private ImageView userImage;
    private ProgressBar userImageProgress;
    TextView editButton;
    private Helper helper;
    protected String[] permissionsCamera = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private User userMe;
    private ImagePicker imagePicker;
    private CameraImagePicker cameraPicker;
    private String pickerPath;
    private DatabaseReference usersRef;

    private static final String TAG = "ProfileEditDialogFragme";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new Helper(getActivity());
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference(Helper.REF_USERS);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit, container);
        userImage = view.findViewById(R.id.userImage);
        userImageProgress = view.findViewById(R.id.progressBar);
        userName = view.findViewById(R.id.userName);
        userNameEdit = view.findViewById(R.id.userNameEdit);
        editButton = view.findViewById(R.id.editButton);
        userStatus = view.findViewById(R.id.userStatus);
        userPhoneNumber = view.findViewById(R.id.userPhoneNumber);
        view.findViewById(R.id.changeImage).setOnClickListener(view1 -> pickProfileImage());
        view.findViewById(R.id.back).setOnClickListener(view12 -> dismiss());
        view.findViewById(R.id.done).setOnClickListener(view13 -> {
            Helper.closeKeyboard(getContext(), view13);
            updateUserNameAndStatus(userNameEdit.getText().toString().trim(), userStatus.getText().toString().trim());
            dismiss();
        });
        editButton.setOnClickListener(v -> {
            pickProfileImage();
        });
        Window window = getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO)
                window.setStatusBarColor(Color.TRANSPARENT);
            else
                window.setStatusBarColor(getActivity().getResources().getColor(R.color.colorSurface));
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUserDetails();
    }

    public void setUserDetails() {
        helper = new Helper(getContext());
        userMe = helper.getLoggedInUser();
        userName.setText(userMe.getNameToDisplay());
        userNameEdit.setText(userMe.getNameToDisplay());
        userPhoneNumber.setText(helper.getPhoneNumberForVerification());
        userStatus.setText(userMe.getStatus());
        Glide.with(this).load(userMe.getImage()).apply(new RequestOptions().placeholder(R.drawable.avatar)).into(userImage);
        userImage.setOnClickListener(v -> startActivity(ImageViewerActivity.newUrlInstance(getContext(), userMe.getImage())));
    }

    private void updateUserNameAndStatus(String updatedName, String updatedStatus) {
        if (TextUtils.isEmpty(updatedName)) {
            Toast.makeText(getContext(), R.string.validation_req_username, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(updatedStatus)) {
            Toast.makeText(getContext(), R.string.validation_req_status, Toast.LENGTH_SHORT).show();
        } else if (!userMe.getName().equals(updatedName) || !userMe.getStatus().equals(updatedStatus)) {
            userMe.setName(updatedName);
            userMe.setStatus(updatedStatus);
            usersRef.child(userMe.getId()).setValue(userMe);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_MEDIA_PERMISSION) {
            if (mediaPermissions().isEmpty()) {
                pickProfileImage();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 69:
                    Log.d(TAG, "onActivityResult: reached file upload");
                    Uri fileUrl = data.getData();
                    File file = ImagePicker.Companion.getFile(data);
                    if (file != null) {
                        Log.d(TAG, "onActivityResult: reached file upload");
                        Glide.with(this).load(file).apply(new RequestOptions().placeholder(R.drawable.avatar)).into(userImage);
                        userImageUploadTask(file, AttachmentTypes.IMAGE, null);
                    }
                case 70:
//                case Picker.PICK_IMAGE_DEVICE:
////                    Log.d(TAG, "onActivityResult: reached image");
////                    if (imagePicker == null) {
////                        imagePicker = new ImagePicker(this);
////                    }
////                    imagePicker.submit(data);
//                    break;
//                case ImagePicker.RESULT_ERROR:
//                    Toast.makeText(getContext(), ImagePicker.Companion.getError(data), Toast.LENGTH_SHORT).show();
//                case Picker.PICK_IMAGE_CAMERA:
//                    if (cameraPicker == null) {
//                        cameraPicker = new CameraImagePicker(this);
//                        cameraPicker.reinitialize(pickerPath);
//                    }
//                    cameraPicker.submit(data);
//                    break;
            }
        }
    }

    private void userImageUploadTask(final File fileToUpload, @AttachmentTypes.AttachmentType final int attachmentType, final Attachment attachment) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(getString(R.string.app_name)).child("ProfileImage").child(userMe.getId());
        FirebaseUploader firebaseUploader = new FirebaseUploader(new FirebaseUploader.UploadListener() {
            @Override
            public void onUploadFail(String message) {
                if (userImageProgress != null)
                    userImageProgress.setVisibility(View.GONE);
            }

            @Override
            public void onUploadSuccess(String downloadUrl) {
                if (userImageProgress != null) {
                    userImageProgress.setVisibility(View.GONE);
                }
                if (usersRef != null && userMe != null) {
                    usersRef.child(userMe.getId()).child("image").setValue(downloadUrl);
                    dismiss();
                }
            }

            @Override
            public void onUploadProgress(int progress) {

            }

            @Override
            public void onUploadCancelled() {
                if (userImageProgress != null) {
                    userImageProgress.setVisibility(View.GONE);
                }
            }
        }, storageReference);
        firebaseUploader.setReplace(true);
        firebaseUploader.uploadImage(getContext(), fileToUpload);
        userImageProgress.setVisibility(View.VISIBLE);
    }

    private void pickProfileImage() {
        if (mediaPermissions().isEmpty()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
            alertDialog.setMessage(R.string.get_image_title);
            alertDialog.setPositiveButton(R.string.get_image_camera, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

//                    cameraPicker = new CameraImagePicker(ProfileEditDialogFragment.this);
//                    cameraPicker.shouldGenerateMetadata(true);
//                    cameraPicker.shouldGenerateThumbnails(true);
//                    cameraPicker.setImagePickerCallback(ProfileEditDialogFragment.this);
//                    pickerPath = cameraPicker.pickImage();

                    ImagePicker.Companion.with(ProfileEditDialogFragment.this)
                            .cameraOnly()
                            .compress(1024)
                            .crop(1000, 1000)
                            .start(69);

                }
            });
            alertDialog.setNegativeButton(R.string.get_image_gallery, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

//                    imagePicker = new ImagePicker(ProfileEditDialogFragment.this);
//                    imagePicker.shouldGenerateMetadata(true);
//                    imagePicker.shouldGenerateThumbnails(true);
//                    imagePicker.setImagePickerCallback(ProfileEditDialogFragment.this);
//                    imagePicker.pickImage();

                    ImagePicker.Companion.with(ProfileEditDialogFragment.this)
                            .galleryOnly()
                            .compress(1024)
                            .crop(1000, 1000)
                            .start(69);

                }
            });
            alertDialog.create().show();
        } else {
            requestPermissions(permissionsCamera, REQUEST_CODE_MEDIA_PERMISSION);
        }
    }

    @Override
    public void onImagesChosen(List<ChosenImage> images) {
        Log.d(TAG, "onImagesChosen: reached got image");
        File fileToUpload = new File(Uri.parse(images.get(0).getOriginalPath()).getPath());
        Glide.with(this).load(fileToUpload).apply(new RequestOptions().placeholder(R.drawable.avatar)).into(userImage);
        userImageUploadTask(fileToUpload, AttachmentTypes.IMAGE, null);
    }

    @Override
    public void onError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // You have to save path in case your activity is killed.
        // In such a scenario, you will need to re-initialize the CameraImagePicker
        outState.putString("picker_path", pickerPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("picker_path")) {
                pickerPath = savedInstanceState.getString("picker_path");
            }
        }
    }

    private List<String> mediaPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : permissionsCamera) {
            if (ActivityCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        return missingPermissions;
    }
}
