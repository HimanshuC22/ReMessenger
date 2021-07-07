package com.example.chatapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.chatapp.R;
import com.example.chatapp.utils.Helper;

public class SplashActivity extends AppCompatActivity {

    Helper helper;
    SharedPreferences sharedPreferences;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        helper = new Helper(this);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String theme = sharedPreferences.getString("Theme", "Light Theme");
        if (theme != null)
            switch (theme) {
                case "System Default":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
                case "Dark Theme":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case "Light Theme":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
            }

        Log.d(TAG, "splash log in " + helper.isLoggedIn() + helper.getLoggedInUser());

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (helper.getLoggedInUser() == null) {
//            Toast.makeText(SplashActivity.this, "GETSTARTED", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SplashActivity.this, GetStarted.class));
                } else {
//            Toast.makeText(SplashActivity.this, "Main", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
                finish();
            }
        }, 1500);


    }
}
