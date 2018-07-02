package com.michaldrobny.iamok.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.michaldrobny.iamok.BuildConfig;
import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.model.Constants;
import com.michaldrobny.iamok.model.ServiceType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimeInitiatorActivity extends PickContactActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    @BindView(R.id.activity_time_initiator_current_rb) AppCompatRadioButton specificTimeRadioButton;
    @BindView(R.id.activity_time_initiator_periodic_rb) AppCompatRadioButton periodicTimeRadioButton;

    @BindView(R.id.activity_time_initiator_current_tv) TextView currentTextView;
    @BindView(R.id.activity_time_initiator_periodic_tv) TextView periodicTextView;

    @BindView(R.id.activity_time_initiator_continue_b) Button continueButton;

    private boolean timePickerAlreadyShowed = false; // Android bug - time picker shows twice
    private boolean chosenPeriodic = false;

    private final int minuteDelay = BuildConfig.BUILD_TYPE.equals("debug") ? 1 : 5;

    private Calendar chosenSpecificCalendar, chosenPeriodicCalendar, tempCalendar = Calendar.getInstance();
    private ArrayList<Integer> selectedDaysForPeriodic = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_initiator);
        ButterKnife.bind(this);
        continueButton.setEnabled(false);
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
        showPickContactActivity();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (timePickerAlreadyShowed) {
            return; // android bug - showing time picker twice
        }

        timePickerAlreadyShowed = true;
        // show time picker with initial delay
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

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm   d.M.yyyy", Locale.getDefault());
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
        alertDialog.setTitle(getString(R.string.time_initiator_outdated));
        alertDialog.setMessage(getString(R.string.time_initiator_outdated_desc, minuteDelay));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showTimePicker(tempCalendar.get(Calendar.HOUR_OF_DAY), tempCalendar.get(Calendar.MINUTE));
                    }
                });
        alertDialog.show();
    }

    @Override
    protected void passArguments(Intent intent) {
        intent.putExtra(Constants.ARG_TYPE, chosenPeriodic ? ServiceType.PeriodicTime.ordinal() : ServiceType.SpecificTime.ordinal());
        intent.putExtra(Constants.ARG_TIME, chosenPeriodic ? chosenPeriodicCalendar.getTimeInMillis() : chosenSpecificCalendar.getTimeInMillis());
        intent.putIntegerArrayListExtra(Constants.ARG_DAYS, selectedDaysForPeriodic);
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
}
