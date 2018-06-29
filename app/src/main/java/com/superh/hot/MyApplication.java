package com.superh.hot;

import android.app.Application;

import com.meituan.robust.PatchExecutor;
import com.superh.hot.hotfix.PatchManipulateImp;
import com.superh.hot.hotfix.RobustCallBackImp;

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
        new PatchExecutor(this,new PatchManipulateImp(),new RobustCallBackImp()).start();
    }

    public static MyApplication getInstance(){
        return INSTANCE;
    }

}
