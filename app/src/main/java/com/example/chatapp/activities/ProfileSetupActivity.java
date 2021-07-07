package com.example.chatapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.example.chatapp.R;
import com.example.chatapp.databinding.ProfileSetupActivityBinding;
import com.example.chatapp.models.Attachment;
import com.example.chatapp.models.AttachmentTypes;
import com.example.chatapp.models.User;
import com.example.chatapp.utils.FirebaseUploader;
import com.example.chatapp.utils.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfileSetupActivity extends AppCompatActivity implements ImagePickerCallback {
    ProfileSetupActivityBinding profileSetupActivityBinding;
    Helper helper;
    User userMe;
    private static final int REQUEST_CODE_MEDIA_PERMISSION = 999;

    private ImagePicker imagePicker;
    private ProgressBar userImageProgress;
    protected String[] permissionsCamera = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String pickerPath;
    private DatabaseReference usersRef;
    private ImageView userImage;
    private CameraImagePicker cameraPicker;
    String doneOrNot;
    SharedPreferences sharedPreferences;
    private static final String TAG = "ProfileSetupActivity";

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onCreate(@Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("picker_path")) {
                pickerPath = savedInstanceState.getString("picker_path");
            }
        }
        Log.d(TAG, "onCreate: ProfileSetupAcitivity");
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        doneOrNot = sharedPreferences.getString("ProfileSetup", "No");
        if (doneOrNot.equals("Yes")) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Profile Setup", Toast.LENGTH_SHORT).show();
            profileSetupActivityBinding = ProfileSetupActivityBinding.inflate(getLayoutInflater());
            setContentView(profileSetupActivityBinding.getRoot());
            helper = new Helper(this);
            userMe = helper.getLoggedInUser();
            if (userMe != null)
                profileSetupActivityBinding.userImage.setOnClickListener(v -> startActivity(ImageViewerActivity.newUrlInstance(this, userMe.getImage())));
            setupView();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setupView() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference(Helper.REF_USERS);
        profileSetupActivityBinding.userNameEdit.setText(userMe.getNameToDisplay());
        profileSetupActivityBinding.userImage.setOnClickListener(v -> {
            pickProfileImage();
        });
        profileSetupActivityBinding.proceedButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Theme", "Yes");
            editor.apply();
            updateUserNameAndStatus(profileSetupActivityBinding.userNameEdit.getText().toString().trim());
        });
    }

    private void updateUserNameAndStatus(String updatedName) {
        if (TextUtils.isEmpty(updatedName)) {
            Toast.makeText(this, R.string.validation_req_username, Toast.LENGTH_SHORT).show();
        } else if (!userMe.getName().equals(updatedName)) {
            userMe.setName(updatedName);
            usersRef.child(userMe.getId()).setValue(userMe);
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void pickProfileImage() {
        if (mediaPermissions().isEmpty()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(R.string.get_image_title);
            alertDialog.setPositiveButton(R.string.get_image_camera, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                    cameraPicker = new CameraImagePicker(ProfileSetupActivity.this);
                    cameraPicker.shouldGenerateMetadata(true);
                    cameraPicker.shouldGenerateThumbnails(true);

                    cameraPicker.setImagePickerCallback(ProfileSetupActivity.this);
                    pickerPath = cameraPicker.pickImage();
                }
            });
            alertDialog.setNegativeButton(R.string.get_image_gallery, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                    imagePicker = new ImagePicker(ProfileSetupActivity.this);
                    imagePicker.shouldGenerateMetadata(true);
                    imagePicker.shouldGenerateThumbnails(true);
                    imagePicker.setImagePickerCallback(ProfileSetupActivity.this);
                    imagePicker.pickImage();
                }
            });
            alertDialog.create().show();
        } else {
            requestPermissions(permissionsCamera, REQUEST_CODE_MEDIA_PERMISSION);
        }
    }

    private List<String> mediaPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : permissionsCamera) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        return missingPermissions;
    }

    @Override
    public void onImagesChosen(List<ChosenImage> images) {
        File fileToUpload = new File(Uri.parse(images.get(0).getOriginalPath()).getPath());
        Glide.with(this).load(fileToUpload).apply(new RequestOptions().placeholder(R.drawable.avatar)).into(profileSetupActivityBinding.userImage);
        userImageUploadTask(fileToUpload, AttachmentTypes.IMAGE, null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // You have to save path in case your activity is killed.
        // In such a scenario, you will need to re-initialize the CameraImagePicker
        outState.putString("picker_path", pickerPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Picker.PICK_IMAGE_DEVICE:
                    if (imagePicker == null) {
                        imagePicker = new ImagePicker(this);
                    }
                    imagePicker.submit(data);
                    break;
                case Picker.PICK_IMAGE_CAMERA:
                    if (cameraPicker == null) {
                        cameraPicker = new CameraImagePicker(this);
                        cameraPicker.reinitialize(pickerPath);
                    }
                    cameraPicker.submit(data);
                    break;
            }
        }
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
                }
                Log.d(TAG, "onUploadSuccess: success");
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
        firebaseUploader.uploadImage(this, fileToUpload);
        profileSetupActivityBinding.progressBar.setVisibility(View.VISIBLE);
    }
}
