package com.michaldrobny.iamok.model;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.evernote.android.job.util.support.PersistableBundleCompat;

import java.util.Iterator;
import java.util.List;

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

    private final ServiceType type;
    private final long millis;
    private final String message;
    private final String[] phoneNumbers;
    private final int[] days;

    public ServiceParser(@NonNull PersistableBundleCompat params) {
        this.type = ServiceType.values()[params.getInt(ARG_TYPE, 0)];
        this.millis = params.getLong(ARG_TIME, -1);
        this.message = params.getString(ARG_MESSAGE, "");
        this.phoneNumbers = params.getStringArray(ARG_PHONE_NUMBERS);
        this.days = params.getIntArray(ARG_DAYS);
    }

    public ServiceParser(@NonNull Bundle bundle) {
        this.type = ServiceType.values()[bundle.getInt(ARG_TYPE, 0)];
        this.millis = bundle.getLong(ARG_TIME, -1);
        this.message = bundle.getString(ARG_MESSAGE, "");
        this.phoneNumbers = bundle.getStringArray(ARG_PHONE_NUMBERS);
        this.days = bundle.getIntArray(ARG_DAYS);
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

    public static int[] convertIntegers(List<Integer> integers) {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++) ret[i] = iterator.next();
        return ret;
    }
}
