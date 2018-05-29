package com.michaldrobny.iamok.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.michaldrobny.iamok.NotificationCreator;
import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.Utils;
import com.michaldrobny.iamok.model.ServiceParser;
import com.michaldrobny.iamok.model.ServiceType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.michaldrobny.iamok.NotificationCreator.LOCAL_BROADCAST_SERVICE_CHANGE;

public class ServicesActivity extends AppCompatActivity {

    private ServicesAdapter servicesAdapter;
    private List<JobRequest> jobRequests;
    private BroadcastReceiver jobEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (servicesAdapter != null) {
                jobRequests.clear();
                jobRequests.addAll(JobManager.instance().getAllJobRequests());
                servicesAdapter.notifyDataSetChanged();
            }
        }
    };

    @BindView(R.id.activity_services_lw) ListView servicesListView;
    @BindView(R.id.fab) FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        ButterKnife.bind(this);
        setTitle(R.string.services_title);

        if (JobManager.instance().getAllJobRequests().isEmpty()){
            Intent intent = new Intent(ServicesActivity.this, TutorialActivity.class);
            startActivity(intent);
            finish();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(jobEventReceiver, new IntentFilter(LOCAL_BROADCAST_SERVICE_CHANGE));

        initFloatingButton();
        jobRequests = new ArrayList<JobRequest>(JobManager.instance().getAllJobRequests());
        servicesAdapter = new ServicesAdapter(this, jobRequests);
        servicesListView.setAdapter(servicesAdapter);
    }

    private void initFloatingButton() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ServicesActivity.this, InitiatorActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                jobEventReceiver);
        super.onDestroy();
    }

    protected class ServicesAdapter extends ArrayAdapter<JobRequest> {

        private Context context;
        private List<JobRequest> servicesList;

        ServicesAdapter(@NonNull Context context, List<JobRequest> list) {
            super(context, 0 , list);
            this.context = context;
            servicesList = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null) {
                listItem = LayoutInflater.from(context).inflate(R.layout.service_list_item, parent, false);
            }

            final JobRequest request = servicesList.get(position);
            ServiceParser parser = new ServiceParser(request.getExtras());

            ImageButton image = listItem.findViewById(R.id.service_list_item_ib);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle(getString(R.string.services_item_delete));
                    alertDialog.setMessage(getString(R.string.services_item_delete_desc));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    request.cancelAndEdit();
                                    NotificationCreator.sendLocalBroadcast(
                                            ServicesActivity.this,
                                            NotificationCreator.LOCAL_BROADCAST_SERVICE_CHANGE);
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {}
                            });
                    alertDialog.show();
                }
            });

            TextView nameTv = listItem.findViewById(R.id.service_list_item_title);
            nameTv.setText(ServiceType.getString(parser.getType()));

            TextView whenTv = listItem.findViewById(R.id.service_list_item_description);
            switch (parser.getType()) {
                case SpecificTime:
                    SimpleDateFormat specificDateFormat = new SimpleDateFormat("H:mm d.M.yyyy", Locale.getDefault());
                    String whenFormatted = specificDateFormat.format(new Date(parser.getMillis()));
                    whenTv.setText(parser.isRescheduled() ?
                            getString(R.string.services_item_rescheduled, whenFormatted) : whenFormatted);
                    break;
                case PeriodicTime:
                    SimpleDateFormat periodicDateFormat = new SimpleDateFormat("H:mm", Locale.getDefault());
                    whenTv.setText(
                            getString(R.string.services_item_periodic,
                                    Utils.concatenateDays(parser.getDays()),
                                    periodicDateFormat.format(new Date(parser.getMillis()))));
            }

            return listItem;
        }
    }
}