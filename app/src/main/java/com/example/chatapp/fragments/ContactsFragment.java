package com.example.chatapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chatapp.databinding.FragmentContactsBinding;


public class ContactsFragment extends Fragment {

    FragmentContactsBinding contactsBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contactsBinding = FragmentContactsBinding.inflate(inflater, container, false);
        return contactsBinding.getRoot();
    }
}