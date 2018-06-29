package com.superh.hot;

import android.app.Application;

/**
 * 2018/6/28.
 *
 * @author huchao
 */
public class MyApplication extends Application {


    private static MyApplication INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    public static MyApplication getInstance(){
        return INSTANCE;
    }

}
