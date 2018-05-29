package com.michaldrobny.iamok.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.michaldrobny.iamok.PermissionManager;
import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.jobs.sms.AbstractSMSJob;
import com.michaldrobny.iamok.jobs.sms.ExactTimeSMSJob;
import com.michaldrobny.iamok.jobs.sms.PeriodicTimeSMSJob;
import com.michaldrobny.iamok.model.Day;
import com.michaldrobny.iamok.model.ServiceParser;
import com.michaldrobny.iamok.model.ServiceType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SummaryActivity extends AppCompatActivity implements View.OnClickListener {

    private ServiceType type;
    private String message;
    private String[] numbers;
    private long millis;
    private ArrayList<Integer> days;

    @BindView(R.id.activity_summary_type_tv_input) TextView typeInput;
    @BindView(R.id.activity_summary_days_tv_input) TextView daysInput;
    @BindView(R.id.activity_summary_time_tv_input) TextView timeInput;
    @BindView(R.id.activity_summary_time_ll) LinearLayout timeLinearLayout;
    @BindView(R.id.activity_summary_time_days_ll) LinearLayout timeDaysLinearLayout;
    @BindView(R.id.activity_summary_numbers_tv_input) TextView numbersInput;
    @BindView(R.id.activity_summary_message_tv_input) TextView messageInput;
    @BindView(R.id.activity_summary_done_b) Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        ButterKnife.bind(this);

        parseValues();
        initViews();
    }

    private void parseValues() {
        Bundle bundle = getIntent().getExtras();
        assert (bundle != null);

        type = ServiceType.values()[bundle.getInt(ServiceParser.ARG_TYPE, 0)];
        message = bundle.getString(ServiceParser.ARG_MESSAGE, "");
        numbers = bundle.getStringArray(ServiceParser.ARG_PHONE_NUMBERS);
        millis = bundle.getLong(ServiceParser.ARG_TIME, -1);
        days = bundle.getIntegerArrayList(ServiceParser.ARG_DAYS);
    }

    private void initViews() {
        doneButton.setOnClickListener(this);
        typeInput.setText(type.name());
        messageInput.setText(message);
        numbersInput.setText(TextUtils.join(", ", numbers));

        switch (type) {
            case SpecificTime:
                timeLinearLayout.setVisibility(View.VISIBLE);
                timeDaysLinearLayout.setVisibility(View.GONE);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm d.M.yyyy", Locale.getDefault());
                timeInput.setText(simpleDateFormat.format(new Date(millis)));
                break;
            case PeriodicTime:
                timeLinearLayout.setVisibility(View.VISIBLE);
                timeDaysLinearLayout.setVisibility(View.VISIBLE);
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("H:mm", Locale.getDefault());
                timeInput.setText(simpleTimeFormat.format(new Date(millis)));
                StringBuilder sb = new StringBuilder();
                for (int i=0; i<days.size(); i++) {
                    sb.append(Day.values()[days.get(i)].name());
                    if (i != days.size()-1) {
                        sb.append(", ");
                    }
                }
                daysInput.setText(sb.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionManager.SMS_SEND_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onClick(doneButton);
        }
    }

    @Override
    public void onClick(View v) {
        if (!PermissionManager.isSmsSendPermissionGranted(this)) {
            PermissionManager.requestSmsSendPermission(this);
            return;
        }

        AbstractSMSJob.IAmOkServiceState state = AbstractSMSJob.IAmOkServiceState.Invalid;
        switch (type) {
            case SpecificTime:
                if (millis > 0 && numbers != null) {
                    ExactTimeSMSJob.scheduleJob(millis, numbers, message);
                    state = AbstractSMSJob.IAmOkServiceState.Scheduled;
                }
                break;
            case PeriodicTime:
                if (millis > 0 && numbers != null && !days.isEmpty()) {
                    PeriodicTimeSMSJob.scheduleJob(millis, days, numbers, message);
                    state = AbstractSMSJob.IAmOkServiceState.Scheduled;
                }
                break;
        }

        Snackbar snackbar = Snackbar.make(v.getRootView(), state.name(), Snackbar.LENGTH_LONG);
        if (state == AbstractSMSJob.IAmOkServiceState.Scheduled) {
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    finish();
                    startActivity(new Intent(SummaryActivity.this, ServicesActivity.class));
                }

                @Override
                public void onShown(Snackbar snackbar) {}
            });
        }
        snackbar.show();
    }
}