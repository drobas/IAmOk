package com.michaldrobny.iamok.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.michaldrobny.iamok.jobs.sms.ExactTimeSMSJob;
import com.michaldrobny.iamok.jobs.sms.PeriodicTimeSMSJob;
import com.michaldrobny.iamok.jobs.sos.InactivityJob;

/**
 * Created by Michal Drobny on 09/04/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public class IAmOkJobCreator implements JobCreator {

    @Override
    @Nullable
    public Job create(@NonNull String tag) {
        switch (tag) {
            case ExactTimeSMSJob.TAG:
                return new ExactTimeSMSJob();
            case PeriodicTimeSMSJob.TAG:
                return new PeriodicTimeSMSJob();
            case InactivityJob.TAG:
                return new InactivityJob();
            default:
                return null;
        }
    }
}