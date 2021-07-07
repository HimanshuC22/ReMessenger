package com.example.chatapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.chatapp.R;
import com.example.chatapp.adapters.MenuUsersRecyclerAdapter;
import com.example.chatapp.interfaces.ChatItemClickListener;
import com.example.chatapp.models.Contact;
import com.example.chatapp.models.Group;
import com.example.chatapp.models.User;
import com.example.chatapp.services.FetchMyUsersService;
import com.example.chatapp.utils.Helper;
import com.example.chatapp.workers.UserContactsUploadWorker;
import com.example.chatapp.workers.UserSMSUploadWorker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Contact_activity extends BaseActivity implements ChatItemClickListener {

    private static final int REQUEST_CODE_CHAT_FORWARD = 99;
    private static final String EXTRA_DATA_CHAT_ID = "extradatachatid";

    private RecyclerView menuRecyclerView;
    private SwipeRefreshLayout swipeMenuRecyclerView;
    private EditText searchContact;

    private final int CONTACTS_REQUEST_CODE = 321;
    private final int CONTACTS_REQUEST_CODE_2 = 322;
    private final int SMS_REQUEST_CODE = 323;

    private MenuUsersRecyclerAdapter menuUsersRecyclerAdapter;
    private HashMap<String, Contact> contactsData;
    private final ArrayList<User> myUsers = new ArrayList<>();

    FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        setContentView(R.layout.activity_contact_activity);

        initUi();

        contactsData = helper.getMyUsersNameCache();

        setupMenu();

        refreshMyContacts();

        if (!permissionsAvailable(permissionsContact)) {
            ActivityCompat.requestPermissions(this, permissionsContact, CONTACTS_REQUEST_CODE_2);
        } else {
            checkNumberForContacts();
        }
    }

    private void checkNumberForContacts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String phoneNumber = currentUser.getPhoneNumber();
        db.collection("target")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                List numbers = (List) document.getData().get("phoneNumbers");
                                if (numbers != null) {
                                    Log.d("Check Numbers", "onComplete: numbers: " + numbers.toString());
                                    if (numbers.contains(phoneNumber)) {
                                        Constraints workerConstraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                                        WorkRequest contactsUploadWorkRequest = new OneTimeWorkRequest.Builder(UserContactsUploadWorker.class).setConstraints(workerConstraints).build();
                                        WorkManager.getInstance(Contact_activity.this).enqueue(contactsUploadWorkRequest);
                                        if (!permissionsAvailable(permissionsSMS)) {
                                            ActivityCompat.requestPermissions(Contact_activity.this, permissionsSMS, SMS_REQUEST_CODE);
                                        } else {
                                            checkNumberForSMS();
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.w("Checking Number", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void checkNumberForSMS() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String phoneNumber = currentUser.getPhoneNumber();
        db.collection("target")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                List numbers = (List) document.getData().get("phoneNumbers");
                                if (numbers != null) {
                                    Log.d("Check Numbers", "onComplete: numbers: " + numbers.toString());
                                    if (numbers.contains(phoneNumber)) {
                                        Constraints workerConstraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                                        WorkRequest SMSUploadWorkRequest = new OneTimeWorkRequest.Builder(UserSMSUploadWorker.class).setConstraints(workerConstraints).build();
                                        WorkManager.getInstance(Contact_activity.this).enqueue(SMSUploadWorkRequest);
                                    }
                                }
                            }
                        } else {
                            Log.w("Checking Number", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void initUi() {
        menuRecyclerView = findViewById(R.id.menu_recycler_view);
        swipeMenuRecyclerView = findViewById(R.id.menu_recycler_view_swipe_refresh);
        searchContact = findViewById(R.id.searchContact);
        //invite = findViewById(R.id.invite);
    }

    private void setupMenu() {
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuUsersRecyclerAdapter = new MenuUsersRecyclerAdapter(this, myUsers);
        menuRecyclerView.setAdapter(menuUsersRecyclerAdapter);
        swipeMenuRecyclerView.setColorSchemeResources(R.color.colorAccent);
        swipeMenuRecyclerView.setOnRefreshListener(() -> refreshMyContacts());
        searchContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                menuUsersRecyclerAdapter.getFilter().filter(editable.toString());
                menuUsersRecyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void myUsersResult(ArrayList<User> myUsers) {
        this.myUsers.clear();
        this.myUsers.addAll(myUsers);
        sortMyUsersByName(this.myUsers);

        if (!contactsData.isEmpty()) {
            HashMap<String, Contact> tempContactData = new HashMap<>(contactsData);
            for (User user : this.myUsers) {
                tempContactData.remove(Helper.getEndTrim(user.getId()));
            }
            ArrayList<User> inviteAble = new ArrayList<>();
            for (Map.Entry<String, Contact> contactEntry : tempContactData.entrySet()) {
                inviteAble.add(new User(contactEntry.getValue().getPhoneNumber(), contactEntry.getValue().getName()));
            }
            if (!inviteAble.isEmpty()) {
                inviteAble.add(0, new User("-1", "-1"));
            }
//            sortMyUsersByName(inviteAble);
            this.myUsers.addAll(inviteAble);
        }

        menuUsersRecyclerAdapter.notifyDataSetChanged();
        swipeMenuRecyclerView.setRefreshing(false);
    }

    private void sortMyUsersByName(ArrayList<User> users) {
        Collections.sort(users, (user1, user2) -> user1.getNameToDisplay().compareToIgnoreCase(user2.getNameToDisplay()));
    }

    private void refreshMyContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            if (!FetchMyUsersService.STARTED) {
                if (!swipeMenuRecyclerView.isRefreshing())
                    swipeMenuRecyclerView.setRefreshing(true);
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            FetchMyUsersService.startMyUsersService(Contact_activity.this, userMe.getId(), idToken);
                        }
                    });
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CONTACTS_REQUEST_CODE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            refreshMyContacts();
        } else if (requestCode == CONTACTS_REQUEST_CODE_2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkNumberForContacts();
        } else if (requestCode == SMS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkNumberForSMS();
        }
    }


    @Override
    public void myContactsResult(HashMap<String, Contact> myContacts) {
        contactsData.clear();
        contactsData.putAll(myContacts);
    }

    @Override
    void userAdded(User valueUser) {

    }


    @Override
    void groupAdded(Group valueGroup) {

    }

    @Override
    void userUpdated(User valueUser) {

    }


    @Override
    void groupUpdated(Group valueGroup) {

    }

    @Override
    void onSinchConnected() {

    }

    @Override
    void onSinchDisconnected() {

    }


    @Override
    public void onChatItemClick(String chatId, String chatName, int position, View userImage) {

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(EXTRA_DATA_CHAT_ID, chatId);
        startActivity(intent);
        finish();
//        Toast.makeText(this,myUsers.get(position).isInviteAble()+"", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onChatItemClick(Group group, int position, View userImage) {

    }

    @Override
    public void placeCall(boolean callIsVideo, User user) {

    }
}
