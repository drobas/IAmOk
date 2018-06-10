package com.michaldrobny.iamok.model;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.evernote.android.job.util.support.PersistableBundleCompat;

/**
 * Created by Michal Drobny on 30/04/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public class ServiceParser {

    public final static String ARG_TYPE = "arg_type";
    public final static String ARG_TIME = "arg_time";
    public final static String ARG_DAYS = "arg_days";
    public final static String ARG_MESSAGE = "arg_message";
    public final static String ARG_PHONE_NUMBERS = "arg_contact";
    public final static String ARG_RESCHEDULED = "arg_rescheduled";
    public final static String ARG_EDIT = "arg_edit";
    public final static String ARG_ID = "arg_id";

    private final ServiceType type;
    private final long millis;
    private final String message;
    private final String[] phoneNumbers;
    private final int[] days;
    private final boolean rescheduled;

    public ServiceParser(@NonNull PersistableBundleCompat params) {
        this.type = ServiceType.values()[params.getInt(ARG_TYPE, 0)];
        this.millis = params.getLong(ARG_TIME, -1);
        this.message = params.getString(ARG_MESSAGE, "");
        this.phoneNumbers = params.getStringArray(ARG_PHONE_NUMBERS);
        this.days = params.getIntArray(ARG_DAYS);
        this.rescheduled = params.getBoolean(ARG_RESCHEDULED, false);
    }

    public ServiceParser(@NonNull Bundle bundle) {
        this.type = ServiceType.values()[bundle.getInt(ARG_TYPE, 0)];
        this.millis = bundle.getLong(ARG_TIME, -1);
        this.message = bundle.getString(ARG_MESSAGE, "");
        this.phoneNumbers = bundle.getStringArray(ARG_PHONE_NUMBERS);
        this.days = bundle.getIntArray(ARG_DAYS);
        this.rescheduled = bundle.getBoolean(ARG_RESCHEDULED, false);
    }

    public ServiceType getType() {
        return type;
    }

    public long getMillis() {
        return millis;
    }

    public int[] getDays() {
        return days;
    }

    public String getMessage() {
        return message;
    }

    public String[] getPhoneNumbers() {
        return phoneNumbers;
    }

    public boolean isRescheduled() {
        return rescheduled;
    }

    public Bundle getBundle() {
        Bundle result = new Bundle();
        result.putInt(ARG_TYPE, type.ordinal());
        result.putLong(ARG_TIME, millis);
        result.putString(ARG_MESSAGE, message);
        result.putStringArray(ARG_PHONE_NUMBERS, phoneNumbers);
        result.putIntArray(ARG_DAYS, days);
        result.putBoolean(ARG_RESCHEDULED, rescheduled);
        return result;
    }
}
