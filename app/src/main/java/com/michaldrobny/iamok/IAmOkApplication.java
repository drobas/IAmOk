package com.michaldrobny.iamok;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;

import com.evernote.android.job.JobManager;
import com.michaldrobny.iamok.jobs.IAmOkJobCreator;
import com.michaldrobny.iamok.jobs.sms.SMSBroadcastReceiver;
import com.testfairy.TestFairy;

/**
 * Created by Michal Drobny on 05/04/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public class IAmOkApplication extends Application {

    public static final String NOTIFICATION_CHANNEL = "com.michaldrobny.app.iamok.notification";

    @Override
    public void onCreate() {
        super.onCreate();
        TestFairy.begin(this, "b998b9c07e1d51705f7737429d1077bf5551305c");
        JobManager.create(this).addJobCreator(new IAmOkJobCreator());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, "IAmOk Notification", NotificationManager.IMPORTANCE_LOW);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}
