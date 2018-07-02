package com.michaldrobny.iamok.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.michaldrobny.iamok.BuildConfig;
import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.model.Constants;
import com.michaldrobny.iamok.model.ServiceType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SOSInitiatorActivity extends PickContactActivity {

    private final int unitMultiplication = BuildConfig.BUILD_TYPE.equals("debug") ? 60000 : 3600000;

    @BindView(R.id.activity_sos_initiator_number_picker) NumberPicker numberPicker;
    @BindView(R.id.activity_sos_number_picker_unit) TextView numberPickerUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sosinitiator);
        ButterKnife.bind(this);
        initNumberPicker();
    }

    private void initNumberPicker() {
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);
        numberPickerUnit.setText(BuildConfig.BUILD_TYPE.equals("debug") ?
                getString(R.string.base_unit_minute) : getString(R.string.base_unit_hour));
    }

    @OnClick(R.id.activity_sos_initiator_continue_b) void continueClick() {
        showPickContactActivity();
    }

    @Override
    protected void passArguments(Intent intent) {
        intent.putExtra(Constants.ARG_TYPE, ServiceType.SOS.ordinal());
        intent.putExtra(Constants.ARG_TIME, numberPicker.getValue() * unitMultiplication);
    }
}
