package com.custmorhelper.app;

import android.app.Application;

import com.custmorhelper.manager.GlobleManager;

public class AppApplication extends Application {

    private static final String TAG = AppApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        GlobleManager.getInstance(this);
    }
}
