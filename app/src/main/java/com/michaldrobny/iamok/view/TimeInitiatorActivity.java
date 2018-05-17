package com.michaldrobny.iamok.view;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.michaldrobny.iamok.BuildConfig;
import com.michaldrobny.iamok.IAmOkApplication;
import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.model.ServiceParser;
import com.michaldrobny.iamok.model.ServiceType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimeInitiatorActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    @BindView(R.id.activity_time_initiator_current_rb) AppCompatRadioButton specificTimeRadioButton;
    @BindView(R.id.activity_time_initiator_periodic_rb) AppCompatRadioButton periodicTimeRadioButton;

    @BindView(R.id.activity_time_initiator_current_tv) TextView currentTextView;
    @BindView(R.id.activity_time_initiator_periodic_tv) TextView periodicTextView;

    @BindView(R.id.activity_time_initiator_continue_b) Button continueButton;

    private static int CONTACT_REQUEST = 10011;
    private static int READ_CONTACTS_PERMISSION_REQUEST = 10001;

    private boolean timePickerAlreadyShowed = false; // Android bug - time picker shows twice
    private boolean chosenPeriodic = false;
    private final int minuteDelay = BuildConfig.DEBUG ? 1 : 5;

    private Calendar chosenSpecificCalendar;
    private Calendar chosenPeriodicCalendar;
    private Calendar tempCalendar = Calendar.getInstance();
    private ArrayList<Integer> selectedDaysForPeriodic = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_initiator);
        ButterKnife.bind(this);
        continueButton.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_CONTACTS_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            continueClick();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
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

            Intent intent = new Intent(TimeInitiatorActivity.this, MessageActivity.class);
            intent.putExtra(ServiceParser.ARG_TYPE, chosenPeriodic ? ServiceType.PeriodicTime.ordinal() : ServiceType.SpecificTime.ordinal());
            intent.putExtra(ServiceParser.ARG_PHONE_NUMBERS, new String[]{phoneNumber});
            intent.putExtra(ServiceParser.ARG_TIME, chosenPeriodic ? chosenPeriodicCalendar.getTimeInMillis() : chosenSpecificCalendar.getTimeInMillis());
            intent.putIntegerArrayListExtra(ServiceParser.ARG_DAYS, selectedDaysForPeriodic);
            startActivity(intent);
        }
    }

    @OnClick(R.id.activity_time_initiator_current_ll) void chooseSpecificTimeClick() {
        chosenPeriodic = false;
        timePickerAlreadyShowed = false;
        showDatePicker();
    }

    @OnClick(R.id.activity_time_initiator_periodic_tv) void choosePeriodicTimeClick() {
        chosenPeriodic = true;
        showTimePicker(
                (chosenPeriodicCalendar == null ? tempCalendar.get(Calendar.HOUR_OF_DAY) : chosenPeriodicCalendar.get(Calendar.HOUR_OF_DAY)),
                (chosenPeriodicCalendar == null ? tempCalendar.get(Calendar.MINUTE) : chosenPeriodicCalendar.get(Calendar.MINUTE)) + minuteDelay + 1);
    }

    @OnClick(R.id.activity_time_initiator_continue_b) void continueClick() {
        if (isReadContactsPermissionGranted()) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(intent, CONTACT_REQUEST);
        } else {
            requestSendSmsPermission();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (timePickerAlreadyShowed) {
            return; // android bug - showing time picker twice
        }

        timePickerAlreadyShowed = true;
        tempCalendar.set(year, month, dayOfMonth);
        if (tempCalendar.getTimeInMillis() - System.currentTimeMillis() < minuteDelay) {
            tempCalendar.add(Calendar.MINUTE, minuteDelay + 1);
        }
        showTimePicker(tempCalendar.get(Calendar.HOUR_OF_DAY), tempCalendar.get(Calendar.MINUTE));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (chosenPeriodic) {
            onPeriodicTimeSet(hourOfDay, minute);
        } else {
            onSpecificDateTimeSet(hourOfDay, minute);
        }
    }

    private void onPeriodicTimeSet(int hourOfDay, int minute) {
        if (chosenPeriodicCalendar == null) {
            chosenPeriodicCalendar = Calendar.getInstance();
        }

        chosenPeriodicCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        chosenPeriodicCalendar.set(Calendar.MINUTE, minute);
        chosenPeriodicCalendar.set(Calendar.SECOND, 0);

        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("H:mm", Locale.getDefault());
        periodicTextView.setTextColor(getResources().getColor(R.color.appBlue));
        periodicTextView.setText(simpleTimeFormat.format(chosenPeriodicCalendar.getTime()));

        if (!selectedDaysForPeriodic.isEmpty()) {
            periodicTimeRadioButton.setChecked(true);
            specificTimeRadioButton.setChecked(false);
            continueButton.setEnabled(true);
        }
    }

    private void onSpecificDateTimeSet(int hourOfDay, int minute) {
        tempCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        tempCalendar.set(Calendar.MINUTE, minute);

        Calendar minutesWithDelay = Calendar.getInstance();
        minutesWithDelay.add(Calendar.MINUTE, minuteDelay);
        if (tempCalendar.compareTo(minutesWithDelay) < 0) {
            showOutdatedAlertDialog();
            return;
        }

        if (chosenSpecificCalendar == null) {
            chosenSpecificCalendar = Calendar.getInstance();
        }

        chosenSpecificCalendar.set(
                tempCalendar.get(Calendar.YEAR),
                tempCalendar.get(Calendar.MONTH),
                tempCalendar.get(Calendar.DAY_OF_MONTH));
        chosenSpecificCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        chosenSpecificCalendar.set(Calendar.MINUTE, minute);
        chosenSpecificCalendar.set(Calendar.SECOND, 0);

        specificTimeRadioButton.setChecked(true);
        periodicTimeRadioButton.setChecked(false);
        continueButton.setEnabled(true);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm d.M.yyyy", Locale.getDefault());
        currentTextView.setTextColor(getResources().getColor(R.color.appBlue));
        currentTextView.setText(simpleDateFormat.format(chosenSpecificCalendar.getTime()));
    }

    private void showDatePicker() {
        DatePickerDialog dpd = new DatePickerDialog(this, this,
                tempCalendar.get(Calendar.YEAR),
                tempCalendar.get(Calendar.MONTH),
                tempCalendar.get(Calendar.DATE));
        dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dpd.show();
    }

    private void showTimePicker(int hour, int minute) {
        new TimePickerDialog(TimeInitiatorActivity.this, this, hour, minute,true).show();
    }

    private void showOutdatedAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.activity_time_initiator_outdated_title));
        alertDialog.setMessage(getString(R.string.activity_time_initiator_outdated_description, minuteDelay));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showTimePicker(tempCalendar.get(Calendar.HOUR_OF_DAY), tempCalendar.get(Calendar.MINUTE));
                    }
                });
        alertDialog.show();
    }

    private void showReadContactErrorDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.activity_time_initiator_read_contact_error_title));
        alertDialog.setMessage(getString(R.string.activity_time_initiator_read_contact_error_description));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        continueClick();
                    }
                });
        alertDialog.show();
    }

    public void onPeriodicDayClick(View view) {
        TextView tv = (TextView) view;
        Integer dayNumber = Integer.parseInt((String)view.getTag());
        if (selectedDaysForPeriodic.contains(dayNumber)) {
            selectedDaysForPeriodic.remove(dayNumber);
            tv.setTextColor(getResources().getColor(R.color.appOrange));

            if (selectedDaysForPeriodic.isEmpty()) {
                periodicTimeRadioButton.setChecked(false);
                continueButton.setEnabled(chosenSpecificCalendar != null);
                if (chosenSpecificCalendar != null) {
                    specificTimeRadioButton.setChecked(true);
                }
            }
        } else {
            selectedDaysForPeriodic.add(dayNumber);
            tv.setTextColor(getResources().getColor(R.color.appBlue));
            if (chosenPeriodicCalendar != null) {
                periodicTimeRadioButton.setChecked(true);
                specificTimeRadioButton.setChecked(false);
                continueButton.setEnabled(true);
            }
        }
    }

    private boolean isReadContactsPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSendSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_contacts_request_message)
                    .setTitle(R.string.dialog_contacts_request_title);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_REQUEST);
    }
}
