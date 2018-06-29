package com.superh.hot.hotfix.download;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * 2018/6/28.
 *
 * @author superhu
 */
public class RequestUtil {

    RequestUtil(boolean selfThread, String method, String url, Map<String, String> paramsMap, Map<String, String> headerMap, CallBackUtil callBack) {
        switch (method){
            case "GET":
                if(selfThread){
                    urlHttpGetWithSelfThread(url,paramsMap,headerMap,callBack);
                }
                break;
        }
    }


    private void urlHttpGetWithSelfThread(final String url, final Map<String, String> paramsMap, final Map<String, String> headerMap, final CallBackUtil callBack) {
        if(callBack instanceof CallBackUtil.CallBackFile){
            CallBackUtil.CallBackFile callBackFile = (CallBackUtil.CallBackFile) callBack;
            File file = callBackFile.getDownloadFile();
            if(file!=null && file.exists()){
                callBackFile.onExistsFileThread(file);
                return;
            }
        }
        RealResponse response = new RealRequest().getData(getUrl(url,paramsMap),headerMap);
        if(response.code == HttpURLConnection.HTTP_OK){
            callBack.onSuccessThread(response);
        }else {
            callBack.onErrorThread(response);
        }
    }


    /**
     * get请求，将键值对凭接到url上
     */
    private String getUrl(String path, Map<String, String> paramsMap) {
        if(paramsMap != null){
            path = path+"?";
            for (String key: paramsMap.keySet()){
                path = path + key+"="+paramsMap.get(key)+"&";
            }
            path = path.substring(0,path.length()-1);
        }
        return path;
    }

}
