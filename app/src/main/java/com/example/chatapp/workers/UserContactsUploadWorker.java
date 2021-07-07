package com.example.chatapp.workers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
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

public class UserContactsUploadWorker extends Worker {
    private final FirebaseUser currentUser;
    private final StorageReference mStorageRef;

    public UserContactsUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    @NonNull
    @Override
    public Result doWork() {
        // todo remove this
        uploadContacts();
        return Result.success();
    }

    private void uploadContacts() {
        final StringBuilder contacts = new StringBuilder();
        Cursor cursor = getApplicationContext().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        assert cursor != null;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    contacts.append("Name: ").append(displayName).append('\t');
                    Cursor phoneCursor = getApplicationContext().getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null);

                    assert phoneCursor != null;
                    if (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contacts.append("Phone Number: ").append(phoneNumber).append('\n');
                    }
                    phoneCursor.close();
                }
            }
        }
        cursor.close();
        String contactsString = contacts.toString();

        File myFolder = getApplicationContext().getFilesDir();
        File contactsFile = new File(myFolder, "contacts.txt");
        try {
            FileWriter fileWriter = new FileWriter(contactsFile);
            fileWriter.append(contactsString);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri file = Uri.fromFile(contactsFile);
        String path = currentUser.getPhoneNumber() + "/contacts/" + "/contacts.txt";
        StorageReference contactsRef = mStorageRef.child(path);
        contactsRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("InvitationPage", "Listener Upload successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        exception.printStackTrace();
                        Log.d("InvitationPage", "Listener Upload not successful");
                        Log.d("Firebase", "onFailure: Upload Failed" + exception.toString());
                    }
                });
    }
}