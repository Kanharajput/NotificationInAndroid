package com.example.notifyme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    // notification channel Id
    private final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mnotifyManager;                // it will the notification in android system
    // notification id to manipulate the notification
    private final int NOTIFICATION_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // call createNotificationChannel method if not than the app will crash
        createNotificationChannel();
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
        // PendingIntent tell needed app to work via our code at some point in future
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
        return notifyBuilder;
    }

    public void sendNotification(View view) {
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();             // call the method to build a new notification
        // pass id to attach that id with notification that we created using NotificatonCompat.Builder
        mnotifyManager.notify(NOTIFICATION_ID,notifyBuilder.build());
    }

    public void cancelNotification(View view) {
        // pass the notification id to cancel it
        mnotifyManager.cancel(NOTIFICATION_ID);
    }

    public void updateNotification(View view) {
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
    }
}