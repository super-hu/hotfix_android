package com.superh.hot.hotfix.download;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * 2018/4/11.
 *
 * @author superhu
 */

public class RealRequest {

    /**
     * get请求
     */
    RealResponse getData(String requestURL, Map<String, String> headerMap){
        HttpURLConnection conn = null;
        try {
            conn= getHttpURLConnection(requestURL,"GET");
            conn.setDoInput(true);
            if(headerMap != null){
                setHeader(conn,headerMap);
            }
            conn.connect();
            return getRealResponse(conn);
        } catch (Exception e) {
            return getExceptonResponse(conn, e);
        }
    }


    /**
     * 得到Connection对象，并进行一些设置
     */
    private HttpURLConnection getHttpURLConnection(String requestURL, String requestMethod) throws IOException {
        URL url = new URL(requestURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10*1000);
        conn.setReadTimeout(15*1000);
        conn.setRequestMethod(requestMethod);
        return conn;
    }

    /**
     * 设置请求头
     */
    private void setHeader(HttpURLConnection conn, Map<String, String> headerMap) {
        if(headerMap != null){
            for (String key: headerMap.keySet()){
                conn.setRequestProperty(key, headerMap.get(key));
            }
        }
    }

    /**
     * 当正常返回时，得到Response对象
     */
    private RealResponse getRealResponse(HttpURLConnection conn) throws IOException {
        RealResponse response = new RealResponse();
        response.code = conn.getResponseCode();
        response.contentLength = conn.getContentLength();
        response.inputStream = conn.getInputStream();
        response.errorStream = conn.getErrorStream();
        return response;
    }

    /**
     * 当发生异常时，得到Response对象
     */
    private RealResponse getExceptonResponse(HttpURLConnection conn, Exception e) {
        if(conn != null){
            conn.disconnect();
        }
        e.printStackTrace();
        RealResponse response = new RealResponse();
        response.exception = e;
        return response;
    }

}
