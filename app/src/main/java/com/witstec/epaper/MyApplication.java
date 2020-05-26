package com.witstec.epaper;

import android.app.Application;

import com.witstec.ble.tagsdk.EPaperSdk;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
         EPaperSdk.init(this);
        EPaperSdk.setDebugMode(true);
    }

}
