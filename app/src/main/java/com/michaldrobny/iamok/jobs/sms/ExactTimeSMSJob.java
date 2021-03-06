package com.michaldrobny.iamok.jobs.sms;

import android.support.annotation.NonNull;

import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.michaldrobny.iamok.model.Constants;
import com.michaldrobny.iamok.model.ServiceWrapper;
import com.michaldrobny.iamok.model.ServiceType;

/**
 * Created by Michal Drobny on 09/04/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public class ExactTimeSMSJob extends AbstractSMSJob {

    public static final String TAG = "exact_time_sms_tag";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        switch (send(params)) {
            case Success:
                return Result.SUCCESS;
            case NoPermission:
                //ToDo: check permission;
                return Result.RESCHEDULE;
            case NotReacheable:
                return Result.RESCHEDULE;
        }

        return Result.SUCCESS;
    }

    public static void scheduleJob(long millis, @NonNull String[] phoneNumbers, @NonNull String message) {

        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putInt(Constants.ARG_TYPE, ServiceType.SpecificTime.ordinal());
        extras.putLong(Constants.ARG_TIME, millis);
        extras.putString(Constants.ARG_MESSAGE, message);
        extras.putStringArray(Constants.ARG_PHONE_NUMBERS, phoneNumbers);

        new JobRequest.Builder(ExactTimeSMSJob.TAG)
                .setExtras(extras)
                .setExact(millis - System.currentTimeMillis())
                .setBackoffCriteria(backoffMs, JobRequest.BackoffPolicy.LINEAR)
                .build()
                .schedule();
    }
}