package com.sangbo.androidautoupdate;

import android.app.Application;

import com.sangbo.autoupdate.CheckVersion;

/**
 * Created by sangbo on 16-5-19.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CheckVersion.checkUrl = "http://sangbo.me/app/autoUpdate/appVersion.txt";

    }
}
