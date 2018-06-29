package com.superh.hot.hotfix;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import com.meituan.robust.Patch;
import com.meituan.robust.PatchManipulate;
import com.superh.hot.BuildConfig;
import com.superh.hot.MyApplication;
import com.superh.hot.hotfix.download.CallBackUtil;
import com.superh.hot.hotfix.download.DownloadUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * 2018/6/28.
 *
 * @author superhu
 */
public class PatchManipulateImp extends PatchManipulate {

    private static final String TAG = "robust";

    /**
     * 热修复信息存储xml名称
     * */
    static final String SP_TABLE = "robust_patch";

    /**
     * 格式：app版本号_补丁版本号
     * */
    static final String SP_HOT_VERSION="sp_hot_version";

    /**
     * 本地补丁文件路径记录
     * */
    static final String SP_PATH = "sp_path";

    /**
     * 是否正在修复 防止重复操作
     * */
    public static volatile boolean isFixing;

    public static String hotfixVersion = null;

    private HotfixVersion version;

    public PatchManipulateImp(HotfixVersion hotfixVersion){
        this.version=hotfixVersion;
        isFixing = true;
    }

    public PatchManipulateImp(){
        isFixing = true;
    }

    @Override
    protected List<Patch> fetchPatchList(Context context) {

        List<Patch> patches = new ArrayList<>();
        Patch patch = new Patch();

        //初始化调用
        if(version==null){
            Log.i(TAG,"获取path列表 初始化>>>>>>");
            String version = getPatchVersion();
            SharedPreferences preferences = MyApplication.getInstance().getApplicationContext().getSharedPreferences(SP_TABLE, Context.MODE_PRIVATE);
            String path = preferences.getString(SP_PATH,"");
            if(!TextUtils.isEmpty(path) && !TextUtils.isEmpty(version)){
                patch.setName(version);
                patch.setLocalPath(path);
            }
        }else{
            Log.i(TAG,"获取path列表 传参>>>>>>");
            patch.setName(version.getName());
            patch.setUrl(version.getUrl());
        }
        patch.setPatchesInfoImplClassFullName("cn.superh.patch.PatchesInfoImpl");
        patches.add(patch);
        return patches;
    }


    /**
     * @param patch
     * @return
     * 可以在这里下载你的补丁，检查补丁是否在手机中
     */
    @Override
    protected boolean ensurePatchExist(final Patch patch) {
        String localPath = patch.getLocalPath();//.jar
        if(!localPath.equals(".jar") && !localPath.equals("null.jar")){//本地有补丁文件
            File file =new File(localPath);
            if(file.exists()){
                Log.i(TAG,"本地存在path>>>>>>");
                return true;
            }else{
                //SP有数据，但是本地文件不存在了的异常情况
                Log.i(TAG,"本地无path，但有sp,清空数据>>>>>>");
                clearData();
            }
        }

        //无下载链接和文件名称，无法下载
        if(TextUtils.isEmpty(patch.getUrl()) || TextUtils.isEmpty(patch.getName())){
            Log.i(TAG,"url null or name null>>>>>>");
            isFixing = false;
            return false;
        }

        //本地没有文件，去下载
        final boolean[] isSuccess = {false};
        //文件保存在 data/data/hotfix/patch.getName（）.jar
        DownloadUtil.downloadFileWithSelfThread(patch.getUrl(),new CallBackUtil.CallBackFile(MyApplication.getInstance().getApplicationContext(),patch.getName()+".jar") {
            @Override
            public void onFailure(int i, String s) {
                Log.i(TAG,"path下载失败case>>>>>>"+s);
            }
            @Override
            public void onResponse(File file) {
                //下载完成 当前线程中。
                if(file!=null && file.exists()){
                    Log.i(TAG,"path下载成功后路径>>>>>>"+file.getAbsolutePath());
                    //xxx.jar 截取  去除掉后面的.jar ps: 框架内部getLocalPath自行添加了.jar 需要去除
                    String localPath = file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf("."));
                    patch.setLocalPath(localPath);
                    isSuccess[0] = true;
                }
            }
        });
        if(!isSuccess[0]){
            isFixing = false;
        }
        return isSuccess[0];
    }


    /**
     * @param context
     * @param patch
     * @return
     * 验证补丁 ps:美团每次加载补丁后都会删除补丁，so,我们给美团的是临时的文件
     */
    @Override
    protected boolean verifyPatch(Context context, Patch patch) {
        //设置临时补丁路径
        patch.setTempPath(context.getCacheDir()+ File.separator+"robust"+ File.separator + "patch");
        try {
            //复制一份临时的补丁文件
            copy(patch.getLocalPath(), patch.getTempPath());
        }catch (Exception e){
            e.printStackTrace();
            isFixing = false;
            return false;
        }
        return true;
    }

    private void clearData(){
        SharedPreferences preferences = MyApplication.getInstance().getApplicationContext().getSharedPreferences(SP_TABLE, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.remove(SP_HOT_VERSION);
        edit.remove(SP_PATH);
        edit.apply();
        hotfixVersion = null;
    }


    private void copy(String srcPath, String dstPath) throws IOException {
        File src=new File(srcPath);
        if(!src.exists()){
            throw new RuntimeException("source patch does not exist ");
        }
        File dst=new File(dstPath);
        if(!dst.getParentFile().exists()){
            dst.getParentFile().mkdirs();
        }
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    /**
     * 获取补丁版本号
     *
     * 判断本地记录的app版本号是否相等  不相等则(可能是大版本升级了)： 清空本地保存数据
     * */
    public static String getPatchVersion(){
        //null比较 not Text.isEmpty  防止重复的执行SP获取消耗时间，后面的hotfixVersion="" 合适时机再次获取SP
        if(hotfixVersion!=null){
            return hotfixVersion;
        }
        SharedPreferences preferences = MyApplication.getInstance().getApplicationContext().getSharedPreferences(SP_TABLE, Context.MODE_PRIVATE);
        String version = preferences.getString(SP_HOT_VERSION,"");
        if(!TextUtils.isEmpty(version) && version.contains("_")){
            String[] versions =  version.split("_");
            if(versions.length>1){
                String appVersion = versions[0];
                hotfixVersion = versions[1];
                if(!appVersion.equals(BuildConfig.VERSION_NAME)){
                    //记录的app版本号不一致，升级了。 移除本地存储的  ps。文件 路径地址 在下次patch成功后删除 #{link RobustCallBackImp.onPatchApplied}
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.remove(SP_HOT_VERSION);
                    edit.apply();
                    hotfixVersion = "";
                    return hotfixVersion;
                }
            }
        }
        hotfixVersion = hotfixVersion==null?"":hotfixVersion;
        return hotfixVersion;
    }
}
