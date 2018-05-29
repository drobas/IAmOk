package com.michaldrobny.iamok;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

/**
 * Created by Michal Drobny on 25/05/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public class PermissionManager {

    public static final int SMS_SEND_PERMISSION_REQUEST = 101;
    public static int READ_CONTACTS_PERMISSION_REQUEST = 10001;

    public static boolean isReadContactsPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isSmsSendPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestReadContactsPermission(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.permission_contacts_request_desc)
                    .setTitle(R.string.permission_contacts_request);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_REQUEST);
    }

    public static void requestSmsSendPermission(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.SEND_SMS)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.permission_sms_request_desc)
                    .setTitle(R.string.permission_sms_request);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS}, SMS_SEND_PERMISSION_REQUEST);
    }
}
