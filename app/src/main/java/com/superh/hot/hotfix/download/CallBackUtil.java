package com.superh.hot.hotfix.download;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 2018/6/28.
 *
 * @author superhu
 */

public abstract class CallBackUtil<T> {
    static Handler mMainHandler = new Handler(Looper.getMainLooper());


    public  void onProgress(float progress, long total ){}

    void onError(final RealResponse response){

        final String errorMessage;
        if(response.inputStream != null){
            errorMessage = getRetString(response.inputStream);
        }else if(response.errorStream != null) {
            errorMessage = getRetString(response.errorStream);
        }else if(response.exception != null) {
            errorMessage = response.exception.getMessage();
        }else {
            errorMessage = "";
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                onFailure(response.code,errorMessage);
            }
        });
    }
    void onSuccess(RealResponse response){
        final T obj = onParseResponse(response);
        if(obj==null){
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    onFailure(404,"下载失败");
                }
            });
            return;
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                onResponse(obj);
            }
        });
    }

    void onExistsFile(final T file){
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                onResponse(file);
            }
        });
    }

    /////////////////////
    void onExistsFileThread(final T file){
        onResponse(file);
    }

    void onSuccessThread(RealResponse response){
        final T obj = onParseResponse(response);
        if(obj==null){
            onFailure(404,"下载失败");
            return;
        }
        onResponse(obj);
    }

    void onErrorThread(final RealResponse response){
        final String errorMessage;
        if(response.inputStream != null){
            errorMessage = getRetString(response.inputStream);
        }else if(response.errorStream != null) {
            errorMessage = getRetString(response.errorStream);
        }else if(response.exception != null) {
            errorMessage = response.exception.getMessage();
        }else {
            errorMessage = "";
        }
        onFailure(response.code,errorMessage);
    }



    ////////////////

    /**
     * 解析response，执行在子线程
     */
    public abstract T onParseResponse(RealResponse response);

    /**
     * 访问网络失败后被调用，执行在UI线程
     */
    public abstract void onFailure(int code,String errorMessage);

    /**
     *
     * 访问网络成功后被调用，执行在UI线程
     */
    public abstract void onResponse(T response);


    /**
     * 下载文件时的回调类
     */
    public static abstract class CallBackFile extends CallBackUtil<File> {

        private static final String FILE_DAT = "hotfix";

        /**
         * 文件夹名称
         * */
        private String mDestFileDir;

        /**
         * 文件名称
         * */
        private final String mdestFileName;

        /**
         * @param destFileName：文件名
         */
        public CallBackFile(Context context, String destFileName){
            mdestFileName = destFileName;
            check(context.getApplicationContext());
        }

        private void check(Context context) {
            String path = context.getFilesDir().getPath() + File.separator + FILE_DAT + File.separator;
            File fixFile = new File(path);
            if (!fixFile.exists()){
                fixFile.mkdirs();
            }
            mDestFileDir =  fixFile.getAbsolutePath();
        }

        public File getDownloadFile(){
            if(TextUtils.isEmpty(mDestFileDir) || TextUtils.isEmpty(mdestFileName)){
                return null;
            }
            File dir = new File(mDestFileDir);
            if (!dir.exists()){
                dir.mkdirs();
            }
            return new File(dir, mdestFileName);
        }


        @Override
        public File onParseResponse(RealResponse response) {

            InputStream is = null;
            byte[] buf = new byte[1024*8];
            int len = 0;
            FileOutputStream fos = null;
            try{
                is = response.inputStream;
                final long total = response.contentLength;

                long sum = 0;

                File dir = new File(mDestFileDir);
                if (!dir.exists()){
                    dir.mkdirs();
                }
                File file = new File(dir, mdestFileName);
                fos = new FileOutputStream(file);
                while ((len = is.read(buf)) != -1){
                    sum += len;
                    fos.write(buf, 0, len);
                    final long finalSum = sum;
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onProgress(finalSum * 100.0f / total,total);
                        }
                    });
                }
                fos.flush();

                return file;

            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                try{
                    if (is != null) is.close();
                } catch (IOException e){
                }
                try{
                    if (fos != null) fos.close();
                } catch (IOException e){
                }

            }
            return null;
        }
    }


    private static String getRetString(InputStream is) {
        String buf;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            buf = sb.toString();
            return buf;

        } catch (Exception e) {
            return null;
        }
    }

}
