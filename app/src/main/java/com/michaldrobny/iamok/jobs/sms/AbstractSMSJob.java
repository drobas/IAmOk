package com.michaldrobny.iamok.jobs.sms;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;

import com.evernote.android.job.Job;
import com.michaldrobny.iamok.BuildConfig;
import com.michaldrobny.iamok.model.ServiceParser;

/**
 * Created by Michal Drobny on 09/04/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public abstract class AbstractSMSJob extends Job {

    static final long backoffMs = 300000; // 5 min

    private final SMSBroadcastReceiver smsBroadcastReceiver = new SMSBroadcastReceiver();

    SMSJobResult send(Params params) {

        getContext().registerReceiver(smsBroadcastReceiver, new IntentFilter(SMSBroadcastReceiver.ACTION));
        ServiceParser parser = new ServiceParser(params.getExtras());

        if (!isOnline()) {
            params.getExtras().putBoolean(ServiceParser.ARG_RESCHEDULED, true);
            sendBroadcast(SMSBroadcastReceiver.RESULT_SERVICE_UNAVAILABLE, parser.getBundle());
            return SMSJobResult.NotReacheable;
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            params.getExtras().putBoolean(ServiceParser.ARG_RESCHEDULED, true);
            sendBroadcast(SMSBroadcastReceiver.RESULT_REQUEST_PERMISSION, parser.getBundle());
            return SMSJobResult.NoPermission;
        }

        if (!BuildConfig.DEBUG) {
            SmsManager smsManager = SmsManager.getDefault();
            for(String number : parser.getPhoneNumbers()) {
                Intent intent = new Intent(SMSBroadcastReceiver.ACTION);
                intent.putExtra(SMSBroadcastReceiver.PARAMS_TAG, parser.getBundle());
                PendingIntent sentPI = PendingIntent.getBroadcast(getContext(), 0, intent, 0);
                smsManager.sendTextMessage(number, null, parser.getMessage(), sentPI, null);
            }
            getContext().unregisterReceiver(smsBroadcastReceiver);
        } else {
            sendBroadcast(Activity.RESULT_OK, parser.getBundle());
        }

        return SMSJobResult.Success;
    }

    private void sendBroadcast(int resultCode, Bundle params) {
        Intent intent = new Intent(SMSBroadcastReceiver.ACTION);
        intent.putExtra(SMSBroadcastReceiver.RESULT_TAG, resultCode);
        intent.putExtra(SMSBroadcastReceiver.PARAMS_TAG, params);
        getContext().sendBroadcast(intent);
        getContext().unregisterReceiver(smsBroadcastReceiver);
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public enum IAmOkServiceState {
        Scheduled,
        Invalid
    }
}