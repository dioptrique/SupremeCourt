package com.example.skynet.supremecourt;

import android.app.Application;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.InputStream;

public class MyApplication extends Application {
    static AppData data;

    @Override
    public void onCreate() {
        super.onCreate();
        data = new AppData(this);
    }
}
