package com.example.chatapp;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.example.chatapp.receivers.ConnectivityReceiver;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;


/**
 * Created by mayank on 11/2/17.
 */

public class BaseApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ConnectivityReceiver.init(this);
//        Fabric.with(this, new Crashlytics());
        EmojiManager.install(new GoogleEmojiProvider());

    }
}
