package com.custmorhelper.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.custmorhelper.util.MyLog;

public class MainService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter);

        return super.onStartCommand(intent, flags, startId);
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            MyLog.d("jack", "intent.action:" + intent.getAction());

            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                MyLog.d("jack", "收到灭屏广播");
            }
        }
    };

}
