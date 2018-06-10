package com.michaldrobny.iamok.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.michaldrobny.iamok.NotificationCreator;
import com.michaldrobny.iamok.PermissionManager;
import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.jobs.sms.AbstractSMSJob;
import com.michaldrobny.iamok.jobs.sms.ExactTimeSMSJob;
import com.michaldrobny.iamok.jobs.sms.PeriodicTimeSMSJob;
import com.michaldrobny.iamok.model.Day;
import com.michaldrobny.iamok.model.ServiceParser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SummaryActivity extends AppCompatActivity {

    private ServiceParser parser;
    private int jobRequestId = -1;
    private boolean isEdit;

    @BindView(R.id.activity_summary_type_tv_input) TextView typeInput;
    @BindView(R.id.activity_summary_days_tv_input) TextView daysInput;
    @BindView(R.id.activity_summary_time_tv_input) TextView timeInput;
    @BindView(R.id.activity_summary_time_ll) LinearLayout timeLinearLayout;
    @BindView(R.id.activity_summary_time_days_ll) LinearLayout timeDaysLinearLayout;
    @BindView(R.id.activity_summary_numbers_tv_input) TextView numbersInput;
    @BindView(R.id.activity_summary_message_tv_input) TextView messageInput;
    @BindView(R.id.activity_summary_done_b) Button doneButton;
    @BindView(R.id.activity_summary_cancel_b) Button cancelButton;
    @BindView(R.id.activity_summary_cancel_padding) View cancelPadding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        assert (bundle != null);
        isEdit = getIntent().getBooleanExtra(ServiceParser.ARG_EDIT, true);
        if (isEdit) {
            parser = new ServiceParser(bundle);
        } else {
            jobRequestId = bundle.getInt(ServiceParser.ARG_ID);
            parser = new ServiceParser(JobManager.instance().getJobRequest(jobRequestId).getExtras());
        }
        initViews();
    }

    private void initViews() {
        typeInput.setText(parser.getType().name());
        messageInput.setText(parser.getMessage());
        numbersInput.setText(TextUtils.join(", ", parser.getPhoneNumbers()));
        doneButton.setText(isEdit ? R.string.base_done : R.string.base_stop);
        cancelPadding.setVisibility(isEdit ? View.GONE : View.VISIBLE);
        cancelButton.setVisibility(isEdit ? View.GONE : View.VISIBLE);

        switch (parser.getType()) {
            case SpecificTime:
                timeLinearLayout.setVisibility(View.VISIBLE);
                timeDaysLinearLayout.setVisibility(View.GONE);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:mm d.M.yyyy", Locale.getDefault());
                timeInput.setText(simpleDateFormat.format(new Date(parser.getMillis())));
                break;
            case PeriodicTime:
                timeLinearLayout.setVisibility(View.VISIBLE);
                timeDaysLinearLayout.setVisibility(View.VISIBLE);
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("H:mm", Locale.getDefault());
                timeInput.setText(simpleTimeFormat.format(new Date(parser.getMillis())));
                StringBuilder sb = new StringBuilder();
                int[] days = parser.getDays();
                for (int i=0; i<days.length; i++) {
                    sb.append(Day.values()[days[i]].name());
                    if (i != days.length-1) {
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
            doneClick(doneButton);
        }
    }

    @OnClick(R.id.activity_summary_done_b)
    public void doneClick(View v) {
        if (isEdit) {
            scheduleAction();
        } else {
            deleteAction();
        }
    }

    @OnClick(R.id.activity_summary_cancel_b)
    public void cancelClick(View v) {
        onBackPressed();
    }

    private void deleteAction() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.services_item_delete));
        alertDialog.setMessage(getString(R.string.services_item_delete_desc));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        JobRequest request = JobManager.instance().getJobRequest(jobRequestId);
                        request.cancelAndEdit();
                        onBackPressed();
                        NotificationCreator.sendLocalBroadcast(
                                SummaryActivity.this,
                                NotificationCreator.LOCAL_BROADCAST_SERVICE_CHANGE);
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                });
        alertDialog.show();
    }

    private void scheduleAction() {
        if (!PermissionManager.isSmsSendPermissionGranted(this)) {
            PermissionManager.requestSmsSendPermission(this);
            return;
        }

        AbstractSMSJob.IAmOkServiceState state = AbstractSMSJob.IAmOkServiceState.Invalid;
        switch (parser.getType()) {
            case SpecificTime:
                if (parser.getMillis() > 0 && parser.getPhoneNumbers() != null) {
                    ExactTimeSMSJob.scheduleJob(parser.getMillis(), parser.getPhoneNumbers(), parser.getMessage());
                    state = AbstractSMSJob.IAmOkServiceState.Scheduled;
                }
                break;
            case PeriodicTime:
                int[] days = parser.getDays();
                if (parser.getMillis() > 0 && parser.getPhoneNumbers() != null && days.length != 0) {
                    PeriodicTimeSMSJob.scheduleJob(parser.getMillis(), days, parser.getPhoneNumbers(), parser.getMessage());
                    state = AbstractSMSJob.IAmOkServiceState.Scheduled;
                }
                break;
        }

        Snackbar snackbar = Snackbar.make(doneButton.getRootView(), state.name(), Snackbar.LENGTH_SHORT);
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