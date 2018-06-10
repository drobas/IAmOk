package com.michaldrobny.iamok.jobs.sms;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;

import com.evernote.android.job.Job;
import com.michaldrobny.iamok.BuildConfig;
import com.michaldrobny.iamok.NotificationCreator;
import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.model.ServiceParser;

/**
 * Created by Michal Drobny on 09/04/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public abstract class AbstractSMSJob extends Job {

    private final static String ACTION = "com.michaldrobny.app.iamok.sms";
    private final static String PARAMS_TAG = "params_tag";
    private final static int RESULT_SERVICE_UNAVAILABLE = 3638;
    private final static int RESULT_REQUEST_PERMISSION = 8393;

    static final long backoffMs = 300000; // 5 min

    private BroadcastReceiver smsBroadcastReceiver;

    SMSJobResult send(Params params) {

        ServiceParser parser = new ServiceParser(params.getExtras());

        if (!isOnline()) {
            params.getExtras().putBoolean(ServiceParser.ARG_RESCHEDULED, true);
            sendNotification(getContext(), RESULT_SERVICE_UNAVAILABLE, parser);
            return SMSJobResult.NotReacheable;
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            params.getExtras().putBoolean(ServiceParser.ARG_RESCHEDULED, true);
            sendNotification(getContext(), RESULT_REQUEST_PERMISSION, parser);
            return SMSJobResult.NoPermission;
        }

        if (!BuildConfig.DEBUG) {
            SmsManager smsManager = SmsManager.getDefault();
            registerSMSBroadcastReceiver(getContext());
            for(String number : parser.getPhoneNumbers()) {
                Intent intent = new Intent(ACTION);
                intent.putExtra(PARAMS_TAG, parser.getBundle());
                PendingIntent sentPI = PendingIntent.getBroadcast(getContext(), 0, intent, 0);
                smsManager.sendTextMessage(number, null, parser.getMessage(), sentPI, null);
            }
            unregisterSMSBroadcastReceiver(getContext());
        } else {
            sendNotification(getContext(), Activity.RESULT_OK, parser);
        }

        return SMSJobResult.Success;
    }

    private void registerSMSBroadcastReceiver(Context context) {
        smsBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                assert (intent.getExtras() != null);
                sendNotification(context, getResultCode(), new ServiceParser(intent.getBundleExtra(PARAMS_TAG)));
            }
        };
        context.registerReceiver(smsBroadcastReceiver, new IntentFilter(ACTION));
    }

    private void unregisterSMSBroadcastReceiver(Context context) {
        if (smsBroadcastReceiver != null) {
            context.unregisterReceiver(smsBroadcastReceiver);
            smsBroadcastReceiver = null;
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void sendNotification(Context context, int resultCode, ServiceParser parser) {
        NotificationCreator.sendLocalNotification(
                context,
                context.getString(R.string.notification_sms_title),
                getNotificationDescriptionResource(context, resultCode, parser));
        NotificationCreator.sendLocalBroadcast(
                context,
                NotificationCreator.LOCAL_BROADCAST_SERVICE_CHANGE);
    }

    private String getNotificationDescriptionResource(Context context, int resultCode , ServiceParser parser) {
        String messageCut = parser.getMessage().length() > 10 ? parser.getMessage().substring(0, 10) + "..." : parser.getMessage();
        String phoneNumber = parser.getPhoneNumbers()[0];
        String ending = "";
        switch (resultCode) {
            case Activity.RESULT_OK:
                ending = context.getString(R.string.notification_sms_description_placeholder_ok);
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                ending = context.getString(R.string.notification_sms_description_placeholder_error_no_service);
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                ending = context.getString(R.string.notification_sms_description_placeholder_error_null_pdu);
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                ending = context.getString(R.string.notification_sms_description_placeholder_error_radio_off);
                break;
            case RESULT_SERVICE_UNAVAILABLE:
                ending = context.getString(R.string.notification_sms_description_placeholder_error_reachability);
                break;
            case RESULT_REQUEST_PERMISSION:
                ending = context.getString(R.string.notification_sms_description_placeholder_error_permission);
                break;
            default:
                ending = context.getString(R.string.notification_sms_description_placeholder_error_generic);
                break;
        }
        return context.getString(R.string.notification_sms_description, messageCut, phoneNumber, ending);
    }

    public enum IAmOkServiceState {
        Scheduled,
        Invalid
    }
}