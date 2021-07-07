package com.example.chatapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatapp.R;
import com.example.chatapp.adapters.ChatAdapter;
import com.example.chatapp.interfaces.HomeIneractor;
import com.example.chatapp.models.Chat;
import com.example.chatapp.models.User;
import com.example.chatapp.utils.Helper;
import com.example.chatapp.views.MyRecyclerView;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static android.content.ContentValues.TAG;

/**
 * Created by a_man on 30-12-2017.
 */

public class MyUsersFragment extends Fragment {
    private MyRecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private Realm rChatDb;
    private User userMe;
    private RealmResults<Chat> resultList;
    private final ArrayList<Chat> chatDataList = new ArrayList<>();


    private final RealmChangeListener<RealmResults<Chat>> chatListChangeListener = new RealmChangeListener<RealmResults<Chat>>() {
        @Override
        public void onChange(@NonNull RealmResults<Chat> element) {
            if (chatDataList != null && chatAdapter != null) {
                chatDataList.clear();
                chatDataList.addAll(rChatDb.copyFromRealm(element));
                chatAdapter.notifyDataSetChanged();
            }
        }
    };
    private HomeIneractor homeInteractor;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            homeInteractor = (HomeIneractor) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement HomeIneractor");
        }
    }

    @Override
    public void onResume() {
        try {
            homeInteractor = (HomeIneractor) getContext();
        } catch (ClassCastException e) {
            throw new ClassCastException(getContext().toString()
                    + " must implement HomeIneractor");
        }

        super.onResume();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Helper helper = new Helper(getContext());
        userMe = helper.getLoggedInUser();
        Realm.init(getContext());
        rChatDb = Helper.getRealmInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_recycler, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setEmptyView(view.findViewById(R.id.emptyView));
        recyclerView.setEmptyImageView(view.findViewById(R.id.emptyImage));
        recyclerView.setEmptyTextView(view.findViewById(R.id.emptyText));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RealmQuery<Chat> query = rChatDb.where(Chat.class).equalTo("myId", userMe.getId());//Query from chats whose owner is logged in user
        resultList = query.equalTo("group", false).sort("timeUpdated", Sort.DESCENDING).findAll();//ignore forward list of messages and get rest sorted according to time

        chatDataList.clear();
        chatDataList.addAll(rChatDb.copyFromRealm(resultList));
        chatAdapter = new ChatAdapter(getActivity(), chatDataList);
        recyclerView.setAdapter(chatAdapter);

        resultList.addChangeListener(chatListChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        homeInteractor = null;
        if (resultList != null)
            resultList.removeChangeListener(chatListChangeListener);
    }

    //Display user's name as saved in phone!
    public void setUserNamesAsInPhone() {
        homeInteractor = (HomeIneractor) getContext();
        if (homeInteractor != null) {
            Log.d(TAG, "setUserNamesAsInPhone: home interctor found");
        } else {
            Log.d(TAG, "setUserNamesAsInPhone: not found");
        }
        if (homeInteractor != null && chatDataList != null) {
//            Log.d(TAG, "setUserNamesAsInPhone: " + homeInteractor.getLocalContacts().toString());
            for (Chat chat : chatDataList) {
                String endTrim = Helper.getEndTrim(chat.getChatId());
                homeInteractor = (HomeIneractor) getContext();
                if (homeInteractor.getLocalContacts().containsKey(endTrim)) {
                    chat.setChatName(homeInteractor.getLocalContacts().get(endTrim).getName());
                }
            }
            if (chatAdapter != null)
                chatAdapter.notifyDataSetChanged();
        }
    }

    public void deleteSelectedChats() {
        rChatDb.beginTransaction();
        for (Chat chat : chatDataList) {
            if (chat.isSelected()) {
                Chat chatToDelete = rChatDb.where(Chat.class).equalTo("myId", userMe.getId()).equalTo("chatId", chat.getChatId()).findFirst();
                if (chatToDelete != null) {
                    RealmObject.deleteFromRealm(chatToDelete);
                }
            }
        }
        rChatDb.commitTransaction();
    }

    public void disableContextualMode() {
        if (chatAdapter != null)
            chatAdapter.disableContextualMode();
    }
}
