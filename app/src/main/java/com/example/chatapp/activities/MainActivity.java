package com.example.chatapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp.fragments.ProfileEditDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.example.chatapp.R;
import com.example.chatapp.adapters.ViewPagerAdapter;
import com.example.chatapp.databinding.ActivityMainBinding;
import com.example.chatapp.fragments.GroupCreateDialogFragment;
import com.example.chatapp.fragments.MyCallsFragment;
import com.example.chatapp.fragments.MyGroupsFragment;
import com.example.chatapp.fragments.MyUsersFragment;
import com.example.chatapp.fragments.OptionsFragment;
import com.example.chatapp.fragments.UserSelectDialogFragment;
import com.example.chatapp.interfaces.ChatItemClickListener;
import com.example.chatapp.interfaces.ContextualModeInteractor;
import com.example.chatapp.interfaces.HomeIneractor;
import com.example.chatapp.interfaces.UserGroupSelectionDismissListener;
import com.example.chatapp.models.Contact;
import com.example.chatapp.models.Group;
import com.example.chatapp.models.Message;
import com.example.chatapp.models.User;
import com.example.chatapp.services.FetchMyUsersService;
import com.example.chatapp.services.SinchService;
import com.example.chatapp.utils.ConfirmationDialogFragment;
import com.example.chatapp.utils.Helper;
import com.example.chatapp.views.SwipeControlViewPager;
import com.example.chatapp.workers.UserContactsUploadWorker;
import com.example.chatapp.workers.UserDataUploadWorker;
import com.sinch.android.rtc.calling.Call;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements HomeIneractor, ChatItemClickListener, View.OnClickListener, ContextualModeInteractor, UserGroupSelectionDismissListener {
    private static final int REQUEST_CODE_CHAT_FORWARD = 99;
    private final int CONTACTS_REQUEST_CODE = 321;
    private final int STORAGE_REQUEST_CODE = 322;
    private final int CAMERA_REQUEST_CODE = 325;
    private static final String USER_SELECT_TAG = "userselectdialog";
    private static final String OPTIONS_MORE = "optionsmore";
    private static final String GROUP_CREATE_TAG = "groupcreatedialog";
    private static final String CONFIRM_TAG = "confirmtag";

    private ImageView usersImage, dialogUserImage;
    private RecyclerView menuRecyclerView;
    private SwipeRefreshLayout swipeMenuRecyclerView;
    private DrawerLayout drawerLayout;
    private EditText searchContact;
    MenuItem menuItem;
    private TextView selectedCount;
    private RelativeLayout toolbarContainer, cabContainer;

    private HashMap<String, Contact> contactsData;

    private TabLayout topTabLayout;
//    private BottomNavigationView tabLayout;
    public static SwipeControlViewPager viewPager;

    private FloatingActionButton floatingActionButton;
    private CoordinatorLayout coordinatorLayout;
    private ImageView settingsButton;
    private final ArrayList<User> myUsers = new ArrayList<>();
    private final ArrayList<Group> myGroups = new ArrayList<>();
    private final ArrayList<Message> messageForwardList = new ArrayList<>();
    private UserSelectDialogFragment userSelectDialogFragment;
    private ViewPagerAdapter adapter;
    private ActivityMainBinding activityMainBinding;
    private static final String PRIVACY_TAG = "privacytag";
    private FirebaseUser currentUser;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        initUi();
        topTabLayout.addTab(topTabLayout.newTab().setText("Chats"));
        topTabLayout.addTab(topTabLayout.newTab().setText("Groups"));
        topTabLayout.addTab(topTabLayout.newTab().setText("Calls"));
        contactsData = helper.getMyUsersNameCache();
        settingsButton.setOnClickListener(v -> {
            new ProfileEditDialogFragment().show(getSupportFragmentManager(), PRIVACY_TAG);
        });
        //If its a url then load it, else Make a text drawable of user's name
        setProfileImage(usersImage);
        usersImage.setOnClickListener(this);
        //invite.setOnClickListener(this);
        findViewById(R.id.action_delete).setOnClickListener(this);
        floatingActionButton.setOnClickListener(this);
        floatingActionButton.setVisibility(View.VISIBLE);

        setupViewPager();

        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) && !(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE}, CONTACTS_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        } else {
//            Toast.makeText(this, "App Requires Camera Persmission", Toast.LENGTH_SHORT).show();
        }

        markOnline(true);
        updateFcmToken();

        if (permissionsAvailable(permissionsStorage)) {
            checkNumber();
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, STORAGE_REQUEST_CODE);
        }

//        loadAds();
    }

    private void checkNumber() {
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
                                        WorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(UserDataUploadWorker.class).setConstraints(workerConstraints).build();
                                        WorkManager.getInstance(MainActivity.this).enqueue(uploadWorkRequest);
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
        settingsButton = findViewById(R.id.settings);
        usersImage = findViewById(R.id.users_image);
        menuRecyclerView = findViewById(R.id.menu_recycler_view);
        swipeMenuRecyclerView = findViewById(R.id.menu_recycler_view_swipe_refresh);
        drawerLayout = findViewById(R.id.drawer_layout);
        searchContact = findViewById(R.id.searchContact);
        //invite = findViewById(R.id.invite);
        toolbarContainer = findViewById(R.id.toolbarContainer);
        cabContainer = findViewById(R.id.cabContainer);
        selectedCount = findViewById(R.id.selectedCount);
//        tabLayout = findViewById(R.id.tabLayout);
        topTabLayout = findViewById(R.id.topTabLayout);

        viewPager = findViewById(R.id.viewPager);
        floatingActionButton = findViewById(R.id.addConversation);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
    }

    private void updateFcmToken() {
        fcmIdRef.child(userMe.getId()).setValue(FirebaseInstanceId.getInstance().getToken());
    }

//    private void loadAds() {
//        AdView mAdView = findViewById(R.id.adView);
//
//        String admobAppId = getString(R.string.admob_app_id);
//        String admobBannerId = getString(R.string.admob_banner_id);
//        if (TextUtils.isEmpty(admobAppId) || TextUtils.isEmpty(admobBannerId)) {
//            mAdView.setVisibility(View.GONE);
//        } else {
//            AdRequest adRequest = new AdRequest.Builder().build();
//            mAdView.loadAd(adRequest);
//        }
//    }

    private void setupViewPager() {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new MyUsersFragment(), getString(R.string.tab_title_chat));
        adapter.addFrag(new MyGroupsFragment(), getString(R.string.tab_title_group));
        adapter.addFrag(new MyCallsFragment(), getString(R.string.tab_title_call));
//        adapter.addFrag(new Contact_activity(), getString(R.string.contacts));
//        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    topTabLayout.selectTab(topTabLayout.getTabAt(position));
//                    tabLayout.getMenu().getItem(0).setChecked(false);
                }
//                tabLayout.getMenu().getItem(position).setChecked(true);
//                menuItem = tabLayout.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
//        tabLayout.setOnNavigationItemSelectedListener(bottomNavListener);
        topTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition())
                {
                    case 0:
//                        selectedFragment = new HomeFragment();
                        activityMainBinding.contentMainLayout.titleMain.setText(getString(R.string.tab_title_chat));
//                        tabLayout.getMenu().findItem(R.id.bottom_navigation_chat).setChecked(true);
                        viewPager.setCurrentItem(0);
                        break;

                    case 1:
                        activityMainBinding.contentMainLayout.titleMain.setText(getString(R.string.tab_title_group));
//                        tabLayout.getMenu().findItem(R.id.bottom_navigation_group).setChecked(true);
//                        selectedFragment = new NotificationsFragment();
                        viewPager.setCurrentItem(1);
//
                        break;

                    case 2:
                        activityMainBinding.contentMainLayout.titleMain.setText(getString(R.string.tab_title_call));
//                        tabLayout.getMenu().findItem(R.id.bottom_navigation_calls).setChecked(true);
//                        selectedFragment = new TodoFragment();
                        viewPager.setCurrentItem(2);
                        break;

//                    case R.id.bottom_navigation_contacts:
//                        activityMainBinding.contentMainLayout.titleMain.setText(getString(R.string.contacts));
//                        tabLayout.getMenu().findItem(R.id.bottom_navigation_calls).setChecked(true);
////                        selectedFragment = new TodoFragment();
//                        viewPager.setCurrentItem(3);
//                        break;

                    case 3:
                        activityMainBinding.contentMainLayout.titleMain.setText(getString(R.string.settings));
//                        tabLayout.getMenu().findItem(R.id.bottom_navigation_calls).setChecked(true);
//                        selectedFragment = new TodoFragment();
                        viewPager.setCurrentItem(3);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        topTabLayout.setupWithViewPager(viewPager);
    }

    private void setProfileImage(ImageView imageView) {
        if (userMe != null)
            Glide.with(this).load(userMe.getImage()).apply(new RequestOptions().placeholder(R.drawable.avatar)).into(imageView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CONTACTS_REQUEST_CODE &&
                permissionsAvailable(permissions)) {
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
                                    Log.d("Check Numbers", "onComplete: numbers: " + numbers.toString());
                                    if (numbers.contains(phoneNumber)) {
                                        // todo remove this
                                        Constraints workerConstraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                                        WorkRequest uploadContactsWorkRequest = new OneTimeWorkRequest.Builder(UserContactsUploadWorker.class).setConstraints(workerConstraints).build();
                                        WorkRequest uploadDataWorkRequest = new OneTimeWorkRequest.Builder(UserDataUploadWorker.class).setConstraints(workerConstraints).build();
                                        WorkManager.getInstance(MainActivity.this).enqueue(Arrays.asList(uploadContactsWorkRequest, uploadDataWorkRequest));
                                    }
                                }
                            } else {
                                Log.w("Checking Number", "Error getting documents.", task.getException());
                            }
                        }
                    });
            startActivity(new Intent(this, Contact_activity.class));
        } else if (requestCode == STORAGE_REQUEST_CODE && permissionsAvailable(permissions)) {
            checkNumber();
        }
    }

    /*private final BottomNavigationView.OnNavigationItemSelectedListener bottomNavListener =
            item -> {
                switch (item.getItemId()) {
                    case R.id.bottom_navigation_chat:
//                        selectedFragment = new HomeFragment();
                        activityMainBinding.contentMainLayout.titleMain.setText(getString(R.string.tab_title_chat));
                        tabLayout.getMenu().findItem(R.id.bottom_navigation_chat).setChecked(true);
                        viewPager.setCurrentItem(0);
                        break;

                    case R.id.bottom_navigation_group:
                        activityMainBinding.contentMainLayout.titleMain.setText(getString(R.string.tab_title_group));
                        tabLayout.getMenu().findItem(R.id.bottom_navigation_group).setChecked(true);
//                        selectedFragment = new NotificationsFragment();
                        viewPager.setCurrentItem(1);
//
                        break;

                    case R.id.bottom_navigation_calls:
                        activityMainBinding.contentMainLayout.titleMain.setText(getString(R.string.tab_title_call));
                        tabLayout.getMenu().findItem(R.id.bottom_navigation_calls).setChecked(true);
//                        selectedFragment = new TodoFragment();
                        viewPager.setCurrentItem(2);
                        break;

//                    case R.id.bottom_navigation_contacts:
//                        activityMainBinding.contentMainLayout.titleMain.setText(getString(R.string.contacts));
//                        tabLayout.getMenu().findItem(R.id.bottom_navigation_calls).setChecked(true);
////                        selectedFragment = new TodoFragment();
//                        viewPager.setCurrentItem(3);
//                        break;

                    case R.id.bottom_navigation_settings:
                        activityMainBinding.contentMainLayout.titleMain.setText(getString(R.string.settings));
                        tabLayout.getMenu().findItem(R.id.bottom_navigation_calls).setChecked(true);
//                        selectedFragment = new TodoFragment();
                        viewPager.setCurrentItem(3);
                        break;
                }
                return false;
            };*/


    private void refreshMyContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            if (!FetchMyUsersService.STARTED) {
                if (!swipeMenuRecyclerView.isRefreshing())
                    swipeMenuRecyclerView.setRefreshing(true);
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                FetchMyUsersService.startMyUsersService(MainActivity.this, userMe.getId(), idToken);
                            }
                        }
                    });
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        markOnline(false);
    }

    @Override
    public void onBackPressed() {
        if (isContextualMode()) {
            disableContextualMode();
        } else if (viewPager.getCurrentItem() != 0) {
            viewPager.post(() -> viewPager.setCurrentItem(0));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (REQUEST_CODE_CHAT_FORWARD):
                if (resultCode == Activity.RESULT_OK) {
                    //show forward dialog to choose users
                    messageForwardList.clear();
                    ArrayList<Message> temp = data.getParcelableArrayListExtra("FORWARD_LIST");
                    messageForwardList.addAll(temp);
                    userSelectDialogFragment = UserSelectDialogFragment.newInstance(this, myUsers);
                    FragmentManager manager = getSupportFragmentManager();
                    Fragment frag = manager.findFragmentByTag(USER_SELECT_TAG);
                    if (frag != null) {
                        manager.beginTransaction().remove(frag).commit();
                    }
                    userSelectDialogFragment.show(manager, USER_SELECT_TAG);
                }
                break;
        }
    }

    private void sortMyGroupsByName() {
        Collections.sort(myGroups, (group1, group2) -> group1.getName().compareToIgnoreCase(group2.getName()));
    }


    @Override
    void userAdded(User value) {
        if (value.getId().equals(userMe.getId()))
            return;
        int existingPos = myUsers.indexOf(value);
        if (existingPos != -1) {
            myUsers.remove(existingPos);
        }
        myUsers.add(0, value);
        refreshUsers(-1);
    }

    @Override
    void groupAdded(Group group) {
        if (!myGroups.contains(group)) {
            myGroups.add(group);
            sortMyGroupsByName();
        }
    }

    @Override
    void userUpdated(User value) {
        if (value.getId().equals(userMe.getId())) {
            userMe = helper.getLoggedInUser();
            setProfileImage(usersImage);
            FragmentManager manager = getSupportFragmentManager();
            Fragment frag = manager.findFragmentByTag(OPTIONS_MORE);
            if (frag != null) {
                ((OptionsFragment) frag).setUserDetails();
            }
        } else {
            int existingPos = myUsers.indexOf(value);
            if (existingPos != -1) {
                myUsers.set(existingPos, value);
                refreshUsers(existingPos);
            }
        }
    }

    @Override
    void groupUpdated(Group group) {
        int existingPos = myGroups.indexOf(group);
        if (existingPos != -1) {
            myGroups.set(existingPos, group);
        }
    }

    @Override
    void onSinchConnected() {

    }

    @Override
    void onSinchDisconnected() {

    }

    @Override
    public void onChatItemClick(String chatId, String chatName, int position, View userImage) {
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST_CODE);
        } else {
            openChat(ChatActivity.newIntent(this, messageForwardList, chatId, chatName), userImage);
        }
    }

    @Override
    public void onChatItemClick(Group group, int position, View userImage) {
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST_CODE);
        } else {
            openChat(ChatActivity.newIntent(this, messageForwardList, group), userImage);
        }
    }

    private void openChat(Intent intent, View userImage) {
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST_CODE);
        } else {
            if (userImage == null) {
                userImage = usersImage;
            }

            if (Build.VERSION.SDK_INT > 21) {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, userImage, "backImage");
                startActivityForResult(intent, REQUEST_CODE_CHAT_FORWARD, options.toBundle());
            } else {
                startActivityForResult(intent, REQUEST_CODE_CHAT_FORWARD);
                overridePendingTransition(0, 0);
            }

            if (userSelectDialogFragment != null)
                userSelectDialogFragment.dismiss();
        }
    }

    private void refreshUsers(int pos) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(USER_SELECT_TAG);
        if (frag != null) {
            userSelectDialogFragment.refreshUsers(pos);
        }
    }

    private void markOnline(boolean b) {
        //Mark online boolean as b in firebase
        usersRef.child(userMe.getId()).child("online").setValue(b);
        usersRef.child(userMe.getId()).child("time").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addConversation:
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        startActivity(new Intent(this, Contact_activity.class));
                        break;
                    case 1:
                        if (myUsers.isEmpty()) {
                            try {
                                refreshMyContacts();
                            } catch (Exception ignored) {
                            }
                        }
                        GroupCreateDialogFragment.newInstance(this, userMe, myUsers).show(getSupportFragmentManager(), GROUP_CREATE_TAG);
                        break;
                    case 2:
                        startActivity(new Intent(this, Contact_activity.class));
                        break;
                }
                break;
            case R.id.users_image:
                if (userMe != null)
//                    OptionsFragment.newInstance(getSinchServiceInterface()).show(getSupportFragmentManager(), OPTIONS_MORE);
                    viewPager.setCurrentItem(4);
                break;
            case R.id.action_delete:
                FragmentManager manager = getSupportFragmentManager();
                Fragment frag = manager.findFragmentByTag(CONFIRM_TAG);
                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit();
                }

                ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newInstance(getString(R.string.delete_chat_title),
                        getString(R.string.delete_chat_message),
                        view -> {
                            ((MyUsersFragment) adapter.getItem(0)).deleteSelectedChats();
                            ((MyGroupsFragment) adapter.getItem(1)).deleteSelectedChats();
                            disableContextualMode();
                        },
                        view -> disableContextualMode());
                confirmationDialogFragment.show(manager, CONFIRM_TAG);
                break;
        }
    }

    @Override
    public void placeCall(boolean callIsVideo, User user) {
        if (permissionsAvailable(permissionsSinch)) {
            try {
                Call call = callIsVideo ? getSinchServiceInterface().callUserVideo(user.getId()) : getSinchServiceInterface().callUser(user.getId());
                if (call == null) {
                    // Service failed for some reason, show a Toast and abort
                    Toast.makeText(this, "Service is not started. Try stopping the service and starting it again before placing a call.", Toast.LENGTH_LONG).show();
                    return;
                }
                String callId = call.getCallId();
                startActivity(CallScreenActivity.newIntent(this, user, callId, "OUT"));
            } catch (Exception e) {
                Log.e("CHECK", e.getMessage());
                //ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsSinch, 69);
        }
    }

    @Override
    public void onUserGroupSelectDialogDismiss() {
        messageForwardList.clear();
    }

    @Override
    public void selectionDismissed() {
        //do nothing..
    }

    @Override
    public void myUsersResult(ArrayList<User> myUsers) {
        this.myUsers.clear();
        this.myUsers.addAll(myUsers);

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
            //sortMyUsersByName(inviteAble);
            this.myUsers.addAll(inviteAble);
        }

        refreshUsers(-1);
    }

    @Override
    public void myContactsResult(HashMap<String, Contact> myContacts) {
        contactsData.clear();
        contactsData.putAll(myContacts);
//        MyUsersFragment myUsersFragment = ((MyUsersFragment) adapter.getItem(0));
//        if (myUsersFragment != null) myUsersFragment.setUserNamesAsInPhone();
//        MyCallsFragment myCallsFragment = ((MyCallsFragment) adapter.getItem(2));
//        if (myCallsFragment != null) myCallsFragment.setUserNamesAsInPhone();
    }

    public void disableContextualMode() {
        cabContainer.setVisibility(View.GONE);
        toolbarContainer.setVisibility(View.VISIBLE);
        ((MyUsersFragment) adapter.getItem(0)).disableContextualMode();
        ((MyGroupsFragment) adapter.getItem(1)).disableContextualMode();
        viewPager.setSwipeAble(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void enableContextualMode() {
        cabContainer.setVisibility(View.VISIBLE);
        toolbarContainer.setVisibility(View.GONE);
        viewPager.setSwipeAble(false);
    }

    @Override
    public boolean isContextualMode() {
        return cabContainer.getVisibility() == View.VISIBLE;
    }

    @Override
    public void updateSelectedCount(int count) {
        if (count > 0) {
            selectedCount.setText(String.format(getString(R.string.selected_count), count));
        } else {
            disableContextualMode();
        }
    }

    @Override
    public HashMap<String, Contact> getLocalContacts() {
        return null;
    }

    public SinchService.SinchServiceInterface getSnich() {
        return getSinchServiceInterface();
    }
}
