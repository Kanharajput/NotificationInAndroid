package com.example.notifyme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    // Create objects for all three buttons
    private Button notifyButton;
    private Button updateButton;
    private Button cancelButton;

    // notification channel Id
    private final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mnotifyManager;                // it will the notification in android system
    // notification id to manipulate the notification
    private final int NOTIFICATION_ID = 0;

    // unique string to pass as a action in Broadcast
    private static final String ACTION_NOTIFICATION_UPDATE = BuildConfig.APPLICATION_ID + "ACTION_UPDATE_NOTIFICATION";
    // unique string to passs as an actin for the Intent which is use to reply
    // the activity that notification is cancelled by the user
    private static final String ACTION_NOTIFICATION_CANCEL_BY_USER = BuildConfig.APPLICATION_ID + "ACTION_NOTIFICATION_CANCELLED";

    // initialise the NotificationReceiver class
    NotificationReceiver notificationReceiver = new NotificationReceiver();
    // initialise the NotificationReceiver class
    CancelNotificationReceiver cancelNotificationReceiver = new CancelNotificationReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // reference all the buttons
        notifyButton = findViewById(R.id.notify);
        updateButton = findViewById(R.id.update);
        cancelButton = findViewById(R.id.cancel);

        // doing not directly(example passing a onlick method name in xml)
        // because we need it to call without passing a view parameter
        // so doing this will save the code
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNotification();
            }
        });

        notifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelNotification();
            }
        });

        // call createNotificationChannel method if not than the app will crash
        createNotificationChannel();

        // when no notification is send then only notify button is enabled,
        changeStateOfButtons(true,false,false);

        // register intent filter to receive only that
        registerReceiver(notificationReceiver,new IntentFilter(ACTION_NOTIFICATION_UPDATE));
        // register the intent filter to receiver cancel notification when user do it manually
        registerReceiver(cancelNotificationReceiver,new IntentFilter(ACTION_NOTIFICATION_CANCEL_BY_USER));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(notificationReceiver);        // unregister the receiver to protect from data leaks
        unregisterReceiver(cancelNotificationReceiver);
        super.onDestroy();
    }

    public void createNotificationChannel() {
        // access the notification manager
        mnotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // if system sdk is 26 or higher than only create the notification channel
        // below sdk version 26 there is no notification channel facility
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                                                                        "Learning Notifications",
                                                                              NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification");
            mnotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        // launch MainActivity when click on notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        // launch mainActivity when click over the notification
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this,
                                                                    NOTIFICATION_ID,
                                                                    notificationIntent,
                                                                    PendingIntent.FLAG_IMMUTABLE);

        // creating notifyBuilder to build up notification for a particular channel and activity
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this,PRIMARY_CHANNEL_ID);
        notifyBuilder.setContentTitle("Notification reached to you");
        notifyBuilder.setContentText("this is the notification text");
        notifyBuilder.setSmallIcon(R.drawable.ic_android);
        notifyBuilder.setContentIntent(notificationPendingIntent);
        notifyBuilder.setAutoCancel(true);                            // it will close the notification when user clicks on it
        notifyBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);      // also applicable for devices running on android version 7.1 or lower
        notifyBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);         // for light, sound and vibration use the default settings

        // set intent and pending intent to receive that user cancel the notification manually
        Intent userCancleNotification = new Intent(ACTION_NOTIFICATION_CANCEL_BY_USER);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this,
                                                                              NOTIFICATION_ID,
                                                                              userCancleNotification,
                                                                              PendingIntent.FLAG_IMMUTABLE);

        // this intent is send when user clear all notification or click x in notification
        notifyBuilder.setDeleteIntent(cancelPendingIntent);
        return notifyBuilder;
    }

    // send notification when click on notify button
    private void sendNotification() {
        // intent to use in update button from the notification
        Intent updateIntent = new Intent(ACTION_NOTIFICATION_UPDATE);
        // it is like an implicit Intent but it's for notification buttons when on defined button it will work
        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(this,NOTIFICATION_ID,updateIntent,PendingIntent.FLAG_IMMUTABLE);
        // call the method to build a new notification
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
        // icon is only shown in android version below <=7.1,  update_action is the title of button
        // and updatePendingIntent add functions to that button.
        // like all buttons there is listener so for that button in notification who is the listener
        // that is BroaccastReceiver subclass is for that and we register that in the oncreate method of MainActivity
        notifyBuilder.addAction(R.drawable.update_icon,"update_action",updatePendingIntent);
        // pass id to attach that id with notification that we created using NotificationCompat.Builder
        mnotifyManager.notify(NOTIFICATION_ID,notifyBuilder.build());

        // when notification is send then it's button should be disabled.
        changeStateOfButtons(false,true,true);
    }

    // use to cancel the notification
    private void cancelNotification() {
        // pass the notification id to cancel it
        mnotifyManager.cancel(NOTIFICATION_ID);
        // when notification is canceled then it's button and update button should be disabled.
        changeStateOfButtons(true,false,false);
    }

    // use to update the notification
    private void updateNotification() {
        // convert the drawable into Bitmap
        Bitmap imgForNotification = BitmapFactory.decodeResource(getResources(),
                                                                    R.drawable.mascot_1);
        // get the object of NotificationCompat.Builder
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder();
        // update notification builder style
        notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                                                           .bigPicture(imgForNotification)
                                                           .setBigContentTitle("notification updated"));
        mnotifyManager.notify(NOTIFICATION_ID,notificationBuilder.build());
        // when notification is updated then it's button and notify button should be disabled.
        changeStateOfButtons(false,false,true);
    }

    // utility method to handle the state of buttons
    private void changeStateOfButtons(Boolean isNotifyEnable, Boolean isUpdateEnable, Boolean isCancelEnable) {
        notifyButton.setEnabled(isNotifyEnable);          // setEnable will change the state of button
        updateButton.setEnabled(isUpdateEnable);
        cancelButton.setEnabled(isCancelEnable);
    }

    // this is only register for MainActivity
    // inner class to receive broacast when user clicks a update button which is embedded in Notification
    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotification();                        // now we directly update the notification from the notification without opening the app
        }
    }

    // change the state of buttons as user cancel the notification manually
    // and the state of button is not changed yet
    public class CancelNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // enable notify button only
            changeStateOfButtons(true,false,false);
        }
    }
}