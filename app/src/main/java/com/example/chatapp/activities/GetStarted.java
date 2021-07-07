package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;


public class GetStarted extends AppCompatActivity {

    TextView terms;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);
        terms = findViewById(R.id.terms);
        terms.setOnClickListener(v -> {
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Privacy & Policies")
                    .setCancelable(true)
                    .setMessage("Privee Talk built the Privee Talk app as a Free app. This SERVICE is provided by Privee\n" +
                            "Talk at no cost and is intended for use as is.\n" +
                            "This page is used to inform visitors regarding my policies with the collection, use, and\n" +
                            "disclosure of Personal Information if anyone decided to use my Service.\n" +
                            "If you choose to use my Service, then you agree to the collection and use of information\n" +
                            "in relation to this policy. The Personal Information that I collect is used for providing and\n" +
                            "improving the Service. I will not use or share your information with anyone except as\n" +
                            "described in this Privacy Policy.\n" +
                            "The terms used in this Privacy Policy have the same meanings as in our Terms and\n" +
                            "Conditions, which is accessible at Privee Talk unless otherwise defined in this Privacy\n\n" +
                            "Policy.\n\n" +
                            "Information Collection and Use\n\n" +
                            "For a better experience, while using our Service, I may require you to provide us with\n" +
                            "certain personally identifiable information, including but not limited to Contacts List.\n" +
                            "The information that I request will be retained on your device and is not collected by me\n" +
                            "in any way.\n\n" +
                            "The app does use third party services that may collect information used to identify you.\n" +
                            "Link to privacy policy of third party service providers used by the app\n" +
                            "● Google Play Services")
                    .setPositiveButton("CLOSE", null)
                    .show();

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        });
        findViewById(R.id.getStarted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GetStarted.this, SignInActivity.class);
                startActivity(i);
                finish();

            }
        });

    }
}