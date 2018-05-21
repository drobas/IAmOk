package com.michaldrobny.iamok.jobs.sms;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;

import com.michaldrobny.iamok.IAmOkApplication;
import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.model.ServiceParser;

import java.util.Random;

/**
 * Created by Michal Drobny on 16/05/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {

    public final static String ACTION = "com.michaldrobny.app.iamok.sms";
    public final static String RESULT_TAG = "result_tag";
    public final static String PARAMS_TAG = "params_tag";

    public final static int RESULT_SERVICE_UNAVAILABLE = 3638;
    public final static int RESULT_REQUEST_PERMISSION = 8393;


    @Override
    public void onReceive(Context context, Intent intent) {

        assert (intent.getExtras() != null);
        ServiceParser parser = new ServiceParser(intent.getBundleExtra(PARAMS_TAG));

        int resultCode = getResultData() != null ? getResultCode() :
                intent.getIntExtra(RESULT_TAG, SmsManager.RESULT_ERROR_GENERIC_FAILURE);

        Notification notification = new NotificationCompat.Builder(context, IAmOkApplication.NOTIFICATION_CHANNEL)
                .setContentTitle(context.getString(R.string.notification_sms_title))
                .setAutoCancel(true)
                .setShowWhen(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getNotificationDescriptionResource(context, resultCode, parser)))
                .setChannelId(IAmOkApplication.NOTIFICATION_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLocalOnly(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();
        NotificationManagerCompat.from(context).notify(new Random().nextInt(), notification);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(IAmOkApplication.LOCAL_NOTIFICATION_ACTION));
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
            case SMSBroadcastReceiver.RESULT_SERVICE_UNAVAILABLE:
                ending = context.getString(R.string.notification_sms_description_placeholder_error_reachability);
                break;
            case SMSBroadcastReceiver.RESULT_REQUEST_PERMISSION:
                ending = context.getString(R.string.notification_sms_description_placeholder_error_permission);
                break;
            default:
                ending = context.getString(R.string.notification_sms_description_placeholder_error_generic);
                break;
        }
        return context.getString(R.string.notification_sms_description, messageCut, phoneNumber, ending);
    }
}
