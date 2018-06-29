package com.superh.hot.hotfix;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.meituan.robust.Patch;
import com.meituan.robust.RobustCallBack;
import com.superh.hot.BuildConfig;
import com.superh.hot.MyApplication;

import java.io.File;
import java.util.List;

import static com.superh.hot.hotfix.PatchManipulateImp.SP_HOT_VERSION;
import static com.superh.hot.hotfix.PatchManipulateImp.SP_PATH;
import static com.superh.hot.hotfix.PatchManipulateImp.SP_TABLE;


/**
 * 2018/6/28.
 *
 * @author superhu
 */
public class RobustCallBackImp implements RobustCallBack {

    public RobustCallBackImp(){
    }

    @Override
    public void onPatchListFetched(boolean result, boolean isNet, List<Patch> patches) {

    }

    @Override
    public void onPatchFetched(boolean result, boolean isNet, Patch patch) {

    }

    /**
     * 在补丁应用后，回调此方法
     * @param result 结果
     * @param patch  补丁
     *
     * 成功后，本地需要记录下补丁的版本号，在拦截器中传递，以备下次不再接受到此版本的信息
     */
    @Override
    public void onPatchApplied(boolean result, Patch patch) {
        Log.i("robust","补丁处理完成，结果>>>>>>"+result);
        if(result){
            SharedPreferences preferences = MyApplication.getInstance().getApplicationContext().getSharedPreferences(SP_TABLE, Context.MODE_PRIVATE);

            //如果有补丁记录和文件，且本地的补丁版本号和现在设置不相等(刚下载的新补丁版本)，则删除掉以前的文件
            String path = preferences.getString(SP_PATH,"");
            String hotfixVersion = PatchManipulateImp.getPatchVersion();
            Log.i("robust","补丁处理完成，本地、路径path>>>>>>"+path);
            Log.i("robust","补丁处理完成，本地、补丁版本号>>>>>>"+hotfixVersion);

            //本地记录版本号和补丁路径
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString(SP_HOT_VERSION, BuildConfig.VERSION_NAME + "_" + patch.getName());
            edit.putString(SP_PATH,patch.getLocalPath().substring(0,patch.getLocalPath().lastIndexOf(".")));//data/data/.../1.2.1.jar  截取.jar前面的路径存储 （ps:内部的getLocalPath后缀添加了.jar）
            edit.apply();
            PatchManipulateImp.hotfixVersion = patch.getName();

            //删除以前的补丁
            if(!TextUtils.isEmpty(path) && !hotfixVersion.equals(patch.getName())){
                boolean isDelSuccess = deleteFile(path+".jar");
                Log.i("robust","补丁处理完成，删除以前的文件>>>>>>"+path +"---是否成功："+isDelSuccess);
            }
        }
        PatchManipulateImp.isFixing = false;
    }

    /**
     * 日志记录
     * */
    @Override
    public void logNotify(String log, String where) {
        Log.i("robust","补丁日志>>>>>>"+log+"<-where-->"+where);
    }

    /**
     * 异常记录
     * */
    @Override
    public void exceptionNotify(Throwable throwable, String where) {
        Log.i("robust","补丁异常>>>>>>"+throwable.getMessage()+"<-where-->"+where);
    }



    private boolean deleteFile(String fileName) {
        File file = new File(fileName);
        return file.exists() && file.delete();
    }
}
