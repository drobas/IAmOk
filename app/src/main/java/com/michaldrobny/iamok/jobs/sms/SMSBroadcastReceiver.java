package com.michaldrobny.iamok.jobs.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import com.michaldrobny.iamok.NotificationCreator;
import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.model.ServiceParser;


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
        NotificationCreator.sendLocalNotification(
                context,
                context.getString(R.string.notification_sms_title),
                getNotificationDescriptionResource(context, resultCode, parser));
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
