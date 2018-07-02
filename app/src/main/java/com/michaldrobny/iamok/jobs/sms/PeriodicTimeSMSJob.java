package com.michaldrobny.iamok.jobs.sms;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.michaldrobny.iamok.model.Constants;
import com.michaldrobny.iamok.model.ServiceWrapper;
import com.michaldrobny.iamok.model.ServiceType;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by Michal Drobny on 09/04/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public class PeriodicTimeSMSJob extends AbstractSMSJob {

    public static final String TAG = "periodic_time_sms_tag";

    @Override
    @NonNull
    protected Job.Result onRunJob(Job.Params params) {

        scheduleNewJob(params);

        switch (send(params)) {
            case Success:
                return Job.Result.SUCCESS;
            case NoPermission:
                //ToDo: check permission;
                return Result.FAILURE;
            case NotReacheable:
                return Result.FAILURE;
        }

        return Job.Result.SUCCESS;
    }
    public static void scheduleJob(long millis, @NonNull int[] days, @NonNull String[] phoneNumbers, @NonNull String message) {

        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(millis);

        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putInt(Constants.ARG_TYPE, ServiceType.PeriodicTime.ordinal());
        extras.putString(Constants.ARG_MESSAGE, message);
        extras.putStringArray(Constants.ARG_PHONE_NUMBERS, phoneNumbers);
        extras.putLong(Constants.ARG_TIME, millis);
        extras.putIntArray(Constants.ARG_DAYS, days);

        new JobRequest.Builder(PeriodicTimeSMSJob.TAG)
                .setExtras(extras)
                .setExact(getEarliestNextEventInMillis(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), days))
                .build()
                .schedule();
    }

    private void scheduleNewJob(Job.Params params) {
        ServiceWrapper parser = new ServiceWrapper(params.getExtras());
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(parser.getMillis());
        new JobRequest.Builder(PeriodicTimeSMSJob.TAG)
                .setExtras(params.getExtras())
                .setExact(getEarliestNextEventInMillis(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), parser.getDays()))
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setBackoffCriteria(backoffMs, JobRequest.BackoffPolicy.LINEAR)
                .build()
                .schedule();
    }


    private static long getEarliestNextEventInMillis(int hour, int minute, int[] days) {
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);

        // Sunday is 1, Monday is 2...
        if (dayOfWeek == 1) {
            dayOfWeek = 6;
        } else {
            dayOfWeek -= 2;
        }

        Arrays.sort(days);
        int earliestNextEventDay = -1;
        int nowHour = now.get(Calendar.HOUR_OF_DAY);
        int nowMinute = now.get(Calendar.MINUTE);
        for (int i : days) {
            if (i > dayOfWeek || (i == dayOfWeek && (hour > nowHour || (hour == nowHour && minute > nowMinute)))) {
                earliestNextEventDay = i;
                break;
            }
        }

        if (earliestNextEventDay == -1) {
            earliestNextEventDay = days[0] + 7;
        }

        int countOfdaysForNextEvent = earliestNextEventDay - dayOfWeek;

        Calendar nextEventCalendar = Calendar.getInstance();
        nextEventCalendar.add(Calendar.DAY_OF_MONTH, countOfdaysForNextEvent);
        nextEventCalendar.set(Calendar.HOUR_OF_DAY, hour);
        nextEventCalendar.set(Calendar.MINUTE, minute);

        return nextEventCalendar.getTimeInMillis() - System.currentTimeMillis();
    }


}
