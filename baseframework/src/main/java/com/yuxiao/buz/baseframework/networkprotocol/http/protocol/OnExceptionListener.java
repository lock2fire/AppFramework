package com.yuxiao.buz.baseframework.networkprotocol.http.protocol;

import com.yuxiao.buz.baseframework.networkprotocol.http.HttpRunner;

import java.util.HashMap;

public interface OnExceptionListener
{
    void onHttpException(
            String url,
            HashMap<String, String> httpHeaders,
            HashMap<String, String> parameters,
            boolean isRequestWithPost,
            Object extra,
            HttpRunner.EHttpState state,
            String stateDes);
}
