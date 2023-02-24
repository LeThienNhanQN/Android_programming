package com.creativestudio.lionvpn.service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.creativestudio.lionvpn.MainApplication;
import com.creativestudio.lionvpn.Preference;
import com.creativestudio.lionvpn.utils.Constant;

public class BootCompletedReceiver extends BroadcastReceiver {

    private int mJobId = 0;
    Preference preference=new Preference(MainApplication.getContext());
    private static final String TAG_BOOT_BROADCAST_RECEIVER = "BOOT_BROADCAST_RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName serviceComponent = new ComponentName(context, BackgroundJobService.class);
        String action = intent.getAction();
        String message = "BootDeviceReceiver onReceive, action is " + action;
        Log.e(TAG_BOOT_BROADCAST_RECEIVER, action +" "+ preference.getStringpreference(Constant.SELECTED_COUNTRY));
        if(Intent.ACTION_BOOT_COMPLETED.equals(action))
        {

            JobScheduler tm = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (tm != null) {
                JobInfo.Builder builder = new JobInfo.Builder(mJobId++, serviceComponent);
                builder.setMinimumLatency(0);
                tm.schedule(builder.build());
            }
        }
    }
}
