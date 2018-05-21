package com.michaldrobny.iamok.jobs.sms;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.michaldrobny.iamok.model.ServiceParser;
import com.michaldrobny.iamok.model.ServiceType;

import java.util.ArrayList;
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

        ServiceParser parser = new ServiceParser(params.getExtras());
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(parser.getMillis());
        new JobRequest.Builder(PeriodicTimeSMSJob.TAG)
                .setExtras(params.getExtras())
                .setExact(getEarliestEventInMillis(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), parser.getDays()))
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setBackoffCriteria(backoffMs, JobRequest.BackoffPolicy.LINEAR)
                .build()
                .schedule();

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

    public static void scheduleJob(long millis, @NonNull ArrayList<Integer> days, @NonNull String[] phoneNumbers, @NonNull String message) {

        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(millis);
        int[] daysArray = ServiceParser.convertIntegers(days);

        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putInt(ServiceParser.ARG_TYPE, ServiceType.PeriodicTime.ordinal());
        extras.putString(ServiceParser.ARG_MESSAGE, message);
        extras.putStringArray(ServiceParser.ARG_PHONE_NUMBERS, phoneNumbers);
        extras.putLong(ServiceParser.ARG_TIME, millis);
        extras.putIntArray(ServiceParser.ARG_DAYS, daysArray);

        new JobRequest.Builder(PeriodicTimeSMSJob.TAG)
                .setExtras(extras)
                .setExact(getEarliestEventInMillis(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), daysArray))
                .build()
                .schedule();
    }

    private static long getEarliestEventInMillis(int hour, int minute, int[] days) {
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);

        // Sunday is 1, Monday is 2...
        if (dayOfWeek == 1) {
            dayOfWeek = 6;
        } else {
            dayOfWeek -= 2;
        }

        Arrays.sort(days);
        int nextEventDay = -1;
        int nowHour = now.get(Calendar.HOUR_OF_DAY);
        int nowMinute = now.get(Calendar.MINUTE);
        for (int i : days) {
            if (i > dayOfWeek || (i == dayOfWeek && (hour > nowHour || (hour == nowHour && minute > nowMinute)))) {
                nextEventDay = i;
                break;
            }
        }

        if (nextEventDay == -1) {
            nextEventDay = days[0] + 7;
        }

        Calendar nextEventCalendar = Calendar.getInstance();
        nextEventDay -= dayOfWeek;

        nextEventCalendar.add(Calendar.DAY_OF_MONTH, nextEventDay);
        nextEventCalendar.set(Calendar.HOUR_OF_DAY, hour);
        nextEventCalendar.set(Calendar.MINUTE, minute);

        return nextEventCalendar.getTimeInMillis() - System.currentTimeMillis();
    }


}
