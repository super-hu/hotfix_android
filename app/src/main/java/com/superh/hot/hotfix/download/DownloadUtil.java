package com.superh.hot.hotfix.download;

/**
 * 2018/6/28.
 *
 * @author superhu
 */
public class DownloadUtil {

    private static final String METHOD_GET = "GET";

    /**
     * 本身处在子线程中，不开启新的线程
     * */
    public static void downloadFileWithSelfThread(String url, CallBackUtil.CallBackFile callBack) {
        new RequestUtil(true,METHOD_GET,url,null,null,callBack);
    }
}
