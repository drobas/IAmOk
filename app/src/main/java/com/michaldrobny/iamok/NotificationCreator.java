package com.michaldrobny.iamok;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Random;

/**
 * Created by Michal Drobny on 24/05/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public class NotificationCreator {

    public static final String LOCAL_BROADCAST_SERVICE_CHANGE = "com.michaldrobny.app.iamok.service_change";
    private static final String NOTIFICATION_CHANNEL = "com.michaldrobny.app.iamok.notification";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, "IAmOk Notification", android.app.NotificationManager.IMPORTANCE_LOW);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            NotificationManager manager = context.getSystemService(android.app.NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void sendLocalNotification(Context fromContext, String title, String description) {
        Notification notification = new NotificationCompat.Builder(fromContext, NOTIFICATION_CHANNEL)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                .setChannelId(NOTIFICATION_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLocalOnly(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();
        NotificationManagerCompat.from(fromContext).notify(new Random().nextInt(), notification);
    }

    public static void sendLocalBroadcast(Context fromContext, String action) {
        LocalBroadcastManager.getInstance(fromContext).sendBroadcast(new Intent(action));
    }
}
