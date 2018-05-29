package com.michaldrobny.iamok;

import android.app.Application;
import android.app.NotificationManager;

import com.evernote.android.job.JobManager;
import com.michaldrobny.iamok.jobs.IAmOkJobCreator;
import com.testfairy.TestFairy;

/**
 * Created by Michal Drobny on 05/04/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public class IAmOkApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TestFairy.begin(this, "b998b9c07e1d51705f7737429d1077bf5551305c");
        JobManager.create(this).addJobCreator(new IAmOkJobCreator());
        NotificationCreator.createNotificationChannel(this);
    }
}
