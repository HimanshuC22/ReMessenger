package com.example.chatapp.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.chatapp.utils.Helper;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e("FCM", "onMessageReceived");
//
//        Map<String, String> map = remoteMessage.getData();
//        HashMap dataHashMap =
//                (map instanceof HashMap)
//                        ? (HashMap) map
//                        : new HashMap<>(map);
//        if (SinchHelpers.isSinchPushPayload(map)) {
//            if (foregrounded()) {
//                return;
//            } else {
//                Intent intent = new Intent(this, IncomingCallScreenActivity.class);
//                NotificationCallVo callVo = new NotificationCallVo();
//                callVo.setData(dataHashMap);
//                intent.putExtra("Parcelable", callVo);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        }

        if (new Helper(this).isLoggedIn()) {
            Intent intent = new Intent(this, FirebaseChatService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 500, pendingIntent);
            Log.e("FCM", "scheduled");
        }
    }

    public static boolean foregrounded() {
        ActivityManager.RunningAppProcessInfo appProcessInfo =
                new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND
                || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

}
