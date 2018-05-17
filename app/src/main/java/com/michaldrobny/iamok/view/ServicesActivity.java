package com.michaldrobny.iamok.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.michaldrobny.iamok.R;
import com.michaldrobny.iamok.model.Day;
import com.michaldrobny.iamok.model.ServiceParser;
import com.michaldrobny.iamok.model.ServiceType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        setTitle(R.string.activity_services_title);

        if (JobManager.instance().getAllJobRequests().isEmpty()){
            Intent intent = new Intent(ServicesActivity.this, TutorialActivity.class);
            startActivity(intent);
            finish();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                jobEventReceiver, new IntentFilter("job-event"));

        initFloatingButton();
        jobRequests = new ArrayList<JobRequest>(JobManager.instance().getAllJobRequests());
        servicesAdapter = new ServicesAdapter(this, jobRequests);
        servicesListView.setAdapter(servicesAdapter);

        Drawable fabSrc = getResources().getDrawable(android.R.drawable.ic_input_add);
        Drawable fabOrange = fabSrc.getConstantState().newDrawable();
        fabOrange.mutate().setColorFilter(getResources().getColor(R.color.appOrangeLight), PorterDuff.Mode.MULTIPLY);
        fab.setImageDrawable(fabOrange);
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

        private Context mContext;
        private List<JobRequest> servicesList;

        ServicesAdapter(@NonNull Context context, List<JobRequest> list) {
            super(context, 0 , list);
            mContext = context;
            servicesList = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.service_list_item, parent, false);

            final JobRequest request = servicesList.get(position);
            ServiceParser parser = new ServiceParser(request.getExtras());

            ImageButton image = listItem.findViewById(R.id.service_list_item_ib);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setTitle(getString(R.string.activity_service_item_delete_title));
                    alertDialog.setMessage(getString(R.string.activity_service_item_delete_description));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    request.cancelAndEdit();
                                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent("job-event"));
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {}
                            });
                    alertDialog.show();
                    request.cancelAndEdit();
                }
            });

            TextView name = listItem.findViewById(R.id.service_list_item_title);
            name.setText(ServiceType.getString(parser.getType()));

            TextView release = listItem.findViewById(R.id.service_list_item_description);
            switch (parser.getType()) {
                case SpecificTime:
                    SimpleDateFormat specificDateFormat = new SimpleDateFormat("H:mm d.M.yyyy",Locale.getDefault());
                    release.setText(specificDateFormat.format(new Date(parser.getMillis())));
                    break;
                case PeriodicTime:
                    SimpleDateFormat periodicDateFormat = new SimpleDateFormat("H:mm",Locale.getDefault());
                    StringBuilder sb = new StringBuilder();
                    int[] days = parser.getDays();
                    for (int i=0; i<days.length; i++) {
                        sb.append(Day.values()[days[i]].name());
                        if (i != days.length-1) {
                            sb.append(", ");
                        }
                    }
                    release.setText(sb.toString() + " at " + periodicDateFormat.format(new Date(parser.getMillis())));
            }


            return listItem;
        }
    }
}