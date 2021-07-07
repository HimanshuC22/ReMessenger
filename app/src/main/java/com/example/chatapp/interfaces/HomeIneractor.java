package com.example.chatapp.interfaces;

import com.example.chatapp.models.Contact;

import java.util.HashMap;

/**
 * Created by a_man on 01-01-2018.
 */

public interface HomeIneractor {
    HashMap<String, Contact> getLocalContacts();
}
