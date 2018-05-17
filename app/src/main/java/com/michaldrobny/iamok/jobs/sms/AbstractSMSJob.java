package com.michaldrobny.iamok.jobs.sms;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;

import com.evernote.android.job.Job;
import com.michaldrobny.iamok.BuildConfig;
import com.michaldrobny.iamok.model.ServiceParser;

/**
 * Created by Michal Drobny on 09/04/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public abstract class AbstractSMSJob extends Job {

    SMSJobResult send(Params params) {

        getContext().registerReceiver(new SMSBroadcastReceiver(), new IntentFilter(SMSBroadcastReceiver.ACTION));

        if (!isOnline()) {
            Intent intent = new Intent(SMSBroadcastReceiver.ACTION);
            getContext().sendBroadcast(intent);
            return SMSJobResult.NotReacheable;
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(SMSBroadcastReceiver.ACTION);
            getContext().sendBroadcast(intent);
            return SMSJobResult.NoPermission;
        }

        if (BuildConfig.DEBUG) {
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        } else {
            String[] phoneNumbers = params.getExtras().getStringArray(ServiceParser.ARG_PHONE_NUMBERS);
            String message = params.getExtras().getString(ServiceParser.ARG_MESSAGE, "");

            SmsManager smsManager = SmsManager.getDefault();
            for(String number : phoneNumbers) {
                PendingIntent sentPI = PendingIntent.getBroadcast(getContext(), 0, new Intent(SMSBroadcastReceiver.ACTION), 0);
                smsManager.sendTextMessage(number, null, message, sentPI, null);
            }
        }

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent("job-event"));
        return SMSJobResult.Success;
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