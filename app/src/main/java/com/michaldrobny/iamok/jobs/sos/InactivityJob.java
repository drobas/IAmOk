package com.michaldrobny.iamok.jobs.sos;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.michaldrobny.iamok.jobs.sms.ExactTimeSMSJob;
import com.michaldrobny.iamok.model.Constants;
import com.michaldrobny.iamok.model.ServiceWrapper;
import com.michaldrobny.iamok.model.ServiceType;

/**
 * Created by Michal Drobny on 20/06/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public class InactivityJob extends Job {

    public static final String TAG = "inactivity_job_tag";
    private static final long backoffMs = 300000; // 5 min
    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        return Result.SUCCESS;
    }

    public static void scheduleJob(long millis, @NonNull String[] phoneNumbers, @NonNull String message) {

        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putInt(Constants.ARG_TYPE, ServiceType.SOS.ordinal());
        extras.putLong(Constants.ARG_TIME, millis);
        extras.putString(Constants.ARG_MESSAGE, message);
        extras.putStringArray(Constants.ARG_PHONE_NUMBERS, phoneNumbers);

        new JobRequest.Builder(InactivityJob.TAG)
                .setExtras(extras)
                .startNow()
                .setBackoffCriteria(backoffMs, JobRequest.BackoffPolicy.LINEAR)
                .build()
                .schedule();
    }
}