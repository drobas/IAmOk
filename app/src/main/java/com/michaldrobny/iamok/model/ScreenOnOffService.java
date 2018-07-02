package com.michaldrobny.iamok.model;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.evernote.android.job.JobManager;
import com.google.gson.Gson;
import com.michaldrobny.iamok.BuildConfig;
import com.michaldrobny.iamok.jobs.sos.InactivityJob;

/**
 * Created by Michal Drobny on 20/06/2018.
 * Copyright © 2018 Michal Drobny. All rights reserved.
 */
public class ScreenOnOffService extends Service {

    private BroadcastReceiver screenStateReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerScreenOnOffReceiver(restoreServiceWrapper(intent));
        return START_STICKY;
    }

    private ServiceWrapper restoreServiceWrapper(Intent intent) {
        Gson gson = new Gson();
        SharedPreferences pref = getApplicationContext().getSharedPreferences(BuildConfig.APPLICATION_ID, 0);
        if (intent == null) {
            String serviceWrapperJson = pref.getString(Constants.ARG_WRAPPER_SERVICE, null);
            return gson.fromJson(serviceWrapperJson, ServiceWrapper.class);
        } else {
            ServiceWrapper serviceWrapper = intent.getParcelableExtra(Constants.ARG_WRAPPER_SERVICE);
            String serviceWrapperJson = gson.toJson(serviceWrapper);
            pref.edit().putString(Constants.ARG_WRAPPER_SERVICE, serviceWrapperJson).apply();
            return serviceWrapper;
        }
    }

    private void registerScreenOnOffReceiver(ServiceWrapper serviceWrapper) {
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenStateReceiver = new ScreenStateBroadcastReceiver(serviceWrapper);
        registerReceiver(screenStateReceiver, screenStateFilter);
    }

    @Override
    public void onDestroy() {
        if (screenStateReceiver != null) {
            unregisterReceiver(screenStateReceiver);
            screenStateReceiver = null;
        }
        super.onDestroy();
    }

    class ScreenStateBroadcastReceiver extends BroadcastReceiver {

        private final ServiceWrapper serviceWrapper;

        public ScreenStateBroadcastReceiver(ServiceWrapper serviceWrapper) {
            this.serviceWrapper = serviceWrapper;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                scheduleInactivityJob();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                removeInactivityJobs();
            }
        }

        private void scheduleInactivityJob() {
            InactivityJob.scheduleJob(serviceWrapper.getMillis(), serviceWrapper.getPhoneNumbers(), serviceWrapper.getMessage());
        }

        private void removeInactivityJobs() {
            JobManager.instance().cancelAllForTag(InactivityJob.TAG);
        }
    }
}
