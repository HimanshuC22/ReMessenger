package com.example.chatapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.chatapp.R;
import com.example.chatapp.activities.MainActivity;
import com.example.chatapp.activities.SplashActivity;
import com.example.chatapp.models.User;
import com.example.chatapp.services.SinchService;
import com.example.chatapp.utils.ConfirmationDialogFragment;
import com.example.chatapp.utils.Helper;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * Created by a_man on 01-01-2018.
 */

public class OptionsFragment extends BaseFullDialogFragment {
    private static final String CONFIRM_TAG = "confirmtag";
    private static final String PRIVACY_TAG = "privacytag";
    private static final String PROFILE_EDIT_TAG = "profileedittag";
    private ImageView userImage;

    private TextView userName, userStatus, userNumber;
    ImageView backButton;
    private SharedPreferences sharedPreferences;
    private Helper helper;
    private SinchService.SinchServiceInterface sinchServiceInterface;
    private User userMe;
    private String selected;
    private TextView deleteAccount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container, false);
        userImage = view.findViewById(R.id.userImage);
        userName = view.findViewById(R.id.userName);
        userStatus = view.findViewById(R.id.userStatus);
        userNumber = view.findViewById(R.id.userNumberPhone);
        deleteAccount = view.findViewById(R.id.deleteAccount);
        backButton = view.findViewById(R.id.backProfile);
        backButton.setOnClickListener(v -> {
            dismiss();
        });

        helper = new Helper(getContext());
        deleteAccount.setOnClickListener(view1 -> {
            deleteAndLogout();
        });
        Activity act = (MainActivity) getActivity();
        sinchServiceInterface = ((MainActivity) act).getSnich();
        setUserDetails();

        view.findViewById(R.id.userDetailContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ProfileEditDialogFragment().show(getChildFragmentManager(), PROFILE_EDIT_TAG);
            }
        });
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.closeKeyboard(getContext(), view);
                dismiss();
            }
        });


        view.findViewById(R.id.themeOptions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                View veiw = getLayoutInflater().inflate(R.layout.theme_dailog, null);
                builder.setView(veiw);
                TextView cancel = veiw.findViewById(R.id.cancelText);
                TextView okay = veiw.findViewById(R.id.okayText);
                RadioGroup radioGroup = veiw.findViewById(R.id.radioDarkGroup);
                String sharedTheme = sharedPreferences.getString("Theme", "Light Theme");
                switch (sharedTheme) {
                    case "System Default":
                        radioGroup.check(R.id.radioButtonSystemDefault);
                        break;
                    case "Dark Theme":
                        radioGroup.check(R.id.radioButtonLight);
                        break;
                    case "System Dark":
                        radioGroup.check(R.id.radioButtonDark);
                        break;
                }
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                cancel.setOnClickListener(v -> alertDialog.cancel());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                okay.setOnClickListener(v -> {
                    RadioButton button = (RadioButton) veiw.findViewById(radioGroup.getCheckedRadioButtonId());
                    selected = button.getText().toString();
                    editor.putString("Theme", selected);
                    editor.apply();
                    Log.d(TAG, "theme " + selected);
                    toggleTheme();
                    alertDialog.cancel();
                });


            }
        });
        view.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.openShareIntent(getContext(), null, String.format(getString(R.string.download_message), getString(R.string.app_name)));
            }
        });
        view.findViewById(R.id.rate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.openPlayStore(getContext());
            }
        });
        view.findViewById(R.id.contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.openSupportMail(getContext());
            }
        });
        view.findViewById(R.id.privacy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PrivacyPolicyDialogFragment().show(getChildFragmentManager(), PRIVACY_TAG);
            }
        });

        view.findViewById(R.id.recent_calls).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.viewPager.setCurrentItem(2);

            }
        });
        view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newInstance(getString(R.string.logout_title),
                        getString(R.string.logout_message),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FirebaseAuth.getInstance().signOut();
                                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Helper.BROADCAST_LOGOUT));
                                sinchServiceInterface.stopClient();
                                helper.logout();
                                helper.clearAllUserData();
                                clearApplicationData();
                                Log.d(TAG, "yes clicked " + helper.getLoggedInUser() + helper.isLoggedIn());
                                Toast.makeText(getContext(), "Logged Out", Toast.LENGTH_SHORT).show();
//                                Intent i = new Intent(getActivity(), SplashActivity.class);
//                                getActivity().startActivity(i);
                                exitApplication(getActivity());
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        });
                confirmationDialogFragment.show(getChildFragmentManager(), CONFIRM_TAG);
            }
        });
        return view;
    }

    public void deleteAndLogout() {
        ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newInstance(getString(R.string.logout_title),
                getString(R.string.deleteMessage),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "onClick: " + helper.getPhoneNumberForVerification());
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        databaseReference.child("users").child(helper.getPhoneNumberForVerification()).getRef().removeValue();
                        databaseReference.child("inbox").child(helper.getPhoneNumberForVerification()).getRef().removeValue();
                        databaseReference.child("user_fcm_ids").child(helper.getPhoneNumberForVerification()).getRef().removeValue();

                        FirebaseAuth.getInstance().signOut();
                        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Helper.BROADCAST_LOGOUT));
                        sinchServiceInterface.stopClient();
                        helper.logout();
                        helper.clearAllUserData();
                        clearApplicationData();
                        Log.d(TAG, "yes clicked " + helper.getLoggedInUser() + helper.isLoggedIn());
                        Toast.makeText(getContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getActivity(), SplashActivity.class);
                        getActivity().startActivity(i);
//                        exitApplication(getActivity());
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
        confirmationDialogFragment.show(getChildFragmentManager(), CONFIRM_TAG);
    }

    public static void exitApplication(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        context.startActivity(intent);
    }

    public void toggleTheme() {
        switch (selected) {
            case "System Default":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                return;
            case "Dark Theme":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return;
            case "Light Theme":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void setUserDetails() {
        helper = new Helper(getContext());
        userMe = helper.getLoggedInUser();
        userName.setText(userMe.getNameToDisplay());
        userStatus.setText(userMe.getStatus());
        userNumber.setText(helper.getPhoneNumberForVerification());
        Glide.with(this).load(userMe.getImage()).apply(new RequestOptions().placeholder(R.drawable.avatar)).into(userImage);
    }

    public static OptionsFragment newInstance(SinchService.SinchServiceInterface sinchServiceInterface) {
        OptionsFragment fragment = new OptionsFragment();
        fragment.sinchServiceInterface = sinchServiceInterface;
        return fragment;
    }

    public void clearApplicationData() {
        File cache = getActivity().getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "File /data/data/APP_PACKAGE/" + s + " DELETED");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
