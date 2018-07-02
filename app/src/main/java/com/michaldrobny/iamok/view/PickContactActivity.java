package com.michaldrobny.iamok.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.michaldrobny.iamok.PermissionManager;
import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.model.Constants;

import static com.michaldrobny.iamok.PermissionManager.READ_CONTACTS_PERMISSION_REQUEST;

/**
 * Created by Michal Drobny on 02/07/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
abstract class PickContactActivity extends AppCompatActivity {
    protected void showPickContactActivity() {
        if (PermissionManager.isReadContactsPermissionGranted(this)) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(intent, READ_CONTACTS_PERMISSION_REQUEST);
        } else {
            PermissionManager.requestReadContactsPermission(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == READ_CONTACTS_PERMISSION_REQUEST && resultCode == Activity.RESULT_OK) {
            String phoneNumber;
            Uri uri = data.getData();
            assert (uri != null);
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) {
                showReadContactErrorDialog();
                return;
            }
            cursor.moveToFirst();
            phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            cursor.close();

            Intent intent = new Intent(PickContactActivity.this, MessageActivity.class);
            intent.putExtra(Constants.ARG_PHONE_NUMBERS, new String[]{phoneNumber});
            passArguments(intent);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_CONTACTS_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showPickContactActivity();
        }
    }

    private void showReadContactErrorDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.time_initiator_read_contact_error));
        alertDialog.setMessage(getString(R.string.time_initiator_read_contact_error_desc));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    protected abstract void passArguments(Intent intent);
}
