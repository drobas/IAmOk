package com.michaldrobny.iamok.view;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.model.Constants;
import com.michaldrobny.iamok.model.ServiceWrapper;
import com.michaldrobny.iamok.model.ServiceType;

public class InitiatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initiator);
    }

    private void showSnackbar(View view, int message) {
        Snackbar snackbar = Snackbar
                .make(view.getRootView(), message, Snackbar.LENGTH_SHORT);

        View snackbarView = snackbar.getView();
        int snackbarTextId = android.support.design.R.id.snackbar_text;
        TextView textView = (TextView)snackbarView.findViewById(snackbarTextId);
        textView.setTextColor(getResources().getColor(R.color.appOrange));
        snackbar.show();
    }

    public void placeButtonOnClick(View view) {
        Intent intent = new Intent(InitiatorActivity.this, PlaceInitiatorActivity.class);
        intent.putExtra(Constants.ARG_TYPE, ServiceType.Place.ordinal());
        startActivity(intent);
    }

    public void sosButtonOnClick(View view) {
        Intent intent = new Intent(InitiatorActivity.this, SOSInitiatorActivity.class);
        intent.putExtra(Constants.ARG_TYPE, ServiceType.SOS.ordinal());
        startActivity(intent);
    }

    public void timeButtonOnClick(View view) {
        Intent intent = new Intent(InitiatorActivity.this, TimeInitiatorActivity.class);
        intent.putExtra(Constants.ARG_TYPE, ServiceType.SpecificTime.ordinal());
        startActivity(intent);
    }
}