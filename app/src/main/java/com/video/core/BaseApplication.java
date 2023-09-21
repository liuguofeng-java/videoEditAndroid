package com.video.core;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

/**
 * @author liuguofeng
 * @date 2023/04/24 11:22
 **/
public class BaseApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getInstance() {
        return mContext;
    }


}
