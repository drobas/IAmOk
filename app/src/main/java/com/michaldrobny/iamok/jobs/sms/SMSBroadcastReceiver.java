package com.michaldrobny.iamok.jobs.sms;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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

    @Override
    public void onReceive(Context context, Intent intent) {

        assert (intent.getExtras() != null);
        ServiceParser parser = new ServiceParser(intent.getExtras());
        int resultCode = getResultCode();

        Notification notification = new NotificationCompat.Builder(context, IAmOkApplication.NOTIFICATION_CHANNEL)
                .setContentTitle(context.getString(R.string.notification_sms_title))
                .setContentText(getNotificationDescriptionResource(context, getResultCode(), parser))
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLocalOnly(true)
                .build();
        NotificationManagerCompat.from(context).notify(new Random().nextInt(), notification);

        if (resultCode != Activity.RESULT_OK) {
            //ToDo schedule new event
        }
    }

    private String getNotificationDescriptionResource(Context context, int resultCode , ServiceParser parser) {
        String messageCut = parser.getMessage().length() > 10 ? parser.getMessage().substring(0, 10) + "..." : parser.getMessage();
        String phoneNumber = parser.getPhoneNumbers()[0];
        String ending = "";
        switch (resultCode) {
            case Activity.RESULT_OK:
                ending = context.getString(R.string.notification_sms_description_placeholder_ok);
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                ending = context.getString(R.string.notification_sms_description_placeholder_error_generic);
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
            case 0:
                ending = context.getString(R.string.notification_sms_description_placeholder_error_permission);
                break;
        }
        return context.getString(R.string.notification_sms_description, messageCut, phoneNumber, ending);
    }
}
