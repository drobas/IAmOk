package com.michaldrobny.iamok.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.jobs.sms.AbstractSMSJob;
import com.michaldrobny.iamok.model.ServiceParser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MessageActivity extends AppCompatActivity {

    @BindView(R.id.activity_message_et) EditText messageEditText;
    @BindView(R.id.activity_message_continue_b) Button continueButton;

    @OnClick (R.id.activity_message_continue_b) void continueButtonClick() {
        Intent intent = new Intent(MessageActivity.this, SummaryActivity.class);
        Bundle extras = getIntent().getExtras();
        assert(extras != null);
        extras.putString(ServiceParser.ARG_MESSAGE, messageEditText.getText().toString());
        extras.putBoolean(ServiceParser.ARG_EDIT, true);
        intent.putExtras(extras);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);

        continueButton.setEnabled(false);
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                continueButton.setEnabled(s.toString().trim().length()!=0);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
}