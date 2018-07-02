package com.michaldrobny.iamok.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.evernote.android.job.util.support.PersistableBundleCompat;

import static com.michaldrobny.iamok.model.Constants.ARG_DAYS;
import static com.michaldrobny.iamok.model.Constants.ARG_LOCATION_LAT;
import static com.michaldrobny.iamok.model.Constants.ARG_LOCATION_LNG;
import static com.michaldrobny.iamok.model.Constants.ARG_MESSAGE;
import static com.michaldrobny.iamok.model.Constants.ARG_PHONE_NUMBERS;
import static com.michaldrobny.iamok.model.Constants.ARG_RESCHEDULED;
import static com.michaldrobny.iamok.model.Constants.ARG_TIME;
import static com.michaldrobny.iamok.model.Constants.ARG_TYPE;
import static com.michaldrobny.iamok.model.Constants.ARG_ZOOM;

/**
 * Created by Michal Drobny on 30/04/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public class ServiceWrapper implements Parcelable {

    private ServiceType type;
    private long millis;
    private String message;
    private String[] phoneNumbers;
    private int[] days;
    private boolean rescheduled;
    private double positionLatitude;
    private double positionLongitude;
    private double zoom;

    private ServiceWrapper(Parcel in) {
        this.type = ServiceType.values()[in.readInt()];
        this.millis = in.readLong();
        this.message = in.readString();

        int phoneNumbersLength = in.readInt();
        if (phoneNumbersLength > 0) {
            this.phoneNumbers = new String[phoneNumbersLength];
            in.readStringArray(this.phoneNumbers);
        }

        int daysLength = in.readInt();
        if (daysLength > 0) {
            this.days = new int[daysLength];
            in.readIntArray(this.days);
        }

        this.rescheduled = in.readInt() == 0;
        this.positionLatitude = in.readDouble();
        this.positionLongitude = in.readDouble();
        this.zoom = in.readDouble();
    }

    ServiceWrapper() {}

    public ServiceWrapper(@NonNull PersistableBundleCompat params) {
        this.type = ServiceType.values()[params.getInt(ARG_TYPE, 0)];
        this.millis = params.getLong(ARG_TIME, -1);
        this.message = params.getString(ARG_MESSAGE, "");
        this.phoneNumbers = params.getStringArray(ARG_PHONE_NUMBERS);
        this.days = params.getIntArray(ARG_DAYS);
        this.rescheduled = params.getBoolean(ARG_RESCHEDULED, false);
        this.positionLatitude = params.getDouble(ARG_LOCATION_LAT, 0);
        this.positionLongitude = params.getDouble(ARG_LOCATION_LNG, 0);
        this.zoom = params.getDouble(ARG_ZOOM, 0);
    }

    public ServiceWrapper(@NonNull Bundle bundle) {
        this.type = ServiceType.values()[bundle.getInt(ARG_TYPE, 0)];
        this.millis = bundle.getLong(ARG_TIME, -1);
        this.message = bundle.getString(ARG_MESSAGE, "");
        this.phoneNumbers = bundle.getStringArray(ARG_PHONE_NUMBERS);
        this.days = bundle.getIntArray(ARG_DAYS);
        this.rescheduled = bundle.getBoolean(ARG_RESCHEDULED, false);
        this.positionLatitude = bundle.getDouble(ARG_LOCATION_LAT, 0);
        this.positionLongitude = bundle.getDouble(ARG_LOCATION_LNG, 0);
        this.zoom = bundle.getDouble(ARG_ZOOM, 0);
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

    public double getPositionLatitude() {
        return positionLatitude;
    }

    public double getPositionLongitude() {
        return positionLongitude;
    }

    public double getZoom() {
        return zoom;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPhoneNumbers(String[] phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public void setDays(int[] days) {
        this.days = days;
    }

    public void setRescheduled(boolean rescheduled) {
        this.rescheduled = rescheduled;
    }

    public void setPositionLatitude(double positionLatitude) {
        this.positionLatitude = positionLatitude;
    }

    public void setPositionLongitude(double positionLongitude) {
        this.positionLongitude = positionLongitude;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type == null ? 0 : this.type.ordinal());
        dest.writeLong(this.millis);
        dest.writeString(TextUtils.isEmpty(this.message) ? "" : this.message);

        int phoneNumbersLength = this.phoneNumbers == null ? 0 : this.phoneNumbers.length;
        dest.writeInt(phoneNumbersLength);
        if (phoneNumbersLength > 0) dest.writeStringArray(this.phoneNumbers);

        int daysLength = this.days == null ? 0 : this.days.length;
        dest.writeInt(daysLength);
        if (daysLength > 0) dest.writeIntArray(this.days);

        dest.writeInt(this.rescheduled ? 0 : 1);
        dest.writeDouble(this.positionLatitude);
        dest.writeDouble(this.positionLongitude);
        dest.writeDouble(this.zoom);
    }

    public static final Parcelable.Creator<ServiceWrapper> CREATOR = new Parcelable.Creator<ServiceWrapper>() {

        @Override
        public ServiceWrapper createFromParcel(Parcel in) {
            return new ServiceWrapper(in);
        }

        @Override
        public ServiceWrapper[] newArray(int size) {
            return new ServiceWrapper[size];
        }
    };
}
