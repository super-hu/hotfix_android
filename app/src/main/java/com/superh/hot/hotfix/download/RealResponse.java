package com.superh.hot.hotfix.download;

import java.io.InputStream;

/**
 * 2018/4/11.
 *
 * @author superhu
 */

public class RealResponse {

    public InputStream inputStream;
    public InputStream errorStream;
    public int code;
    public long contentLength;
    public Exception exception;
}
