package com.example.hp.quote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationBuilderWithBuilderAccessor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static SharedPreferences pref;
    private static final String TAG = "MyFirebaseMsgService";
    private static final String actionLiked = "liked";
    private static final int NOTIFICATION_ID = 1593;
    private final String GROUP_KEY = "GROUP_KEY_RANDOM_NAME";



    public MyFirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        //Toast.makeText(this,"////////////////message recieved///",Toast.LENGTH_SHORT).show();
        Log.e("TAGGGGGGG","received yepieeeeeeeeeeeeeeeeeeee");
        int id = (int) System.currentTimeMillis();
        pref = getApplicationContext().getSharedPreferences("myfile",Context.MODE_PRIVATE);
        Map<String, String> data = remoteMessage.getData();
        SharedPreferences.Editor editor= pref.edit();
        editor.putString("message",data.get("message"));
        editor.putString("url",data.get("url"));
        editor.apply();
        editor.commit();

        SharedPreferences sharedPreferences = getSharedPreferences("NotificationData", 0);
        SharedPreferences.Editor editor1 = sharedPreferences.edit();
        editor1.putString(String.valueOf(id), data.get("message"));
        editor1.apply();
        editor1.commit();


        Intent intent = new Intent(this, CancelNotificationReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Quotes");
        inboxStyle.setSummaryText("");


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        notificationBuilder.setContentTitle("Quote of the day");
        notificationBuilder.setSmallIcon(R.drawable.doller);
        notificationBuilder.setContentIntent(pIntent);
        notificationBuilder.setDeleteIntent(pIntent);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.addAction(R.drawable.doller,"Unread Quotes",pIntent);


        if(notifications.length ==  0){
            Log.e("TAGGGG","first one");
            inboxStyle.addLine(data.get("message"));
            notificationBuilder.setStyle(inboxStyle);
        }
        else{
            Log.e("TAGGGGG","already");
            notificationManager.cancelAll();

            Map<String,String> ndata = (Map<String, String>) sharedPreferences.getAll();
            for(Map.Entry<String,String> entry : ndata.entrySet()){
                Log.e("dataaaaa",entry.getValue());
                inboxStyle.addLine(entry.getValue());
            }
            notificationBuilder.setStyle(inboxStyle);

        }
        notificationManager.notify(id, notificationBuilder.build());

// build notification
// the addAction re-use the same intent to keep the example short

    }





    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
    
}
