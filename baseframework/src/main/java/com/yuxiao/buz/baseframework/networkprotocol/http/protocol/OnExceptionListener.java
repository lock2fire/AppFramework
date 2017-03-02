package com.yuxiao.buz.baseframework.networkprotocol.http.protocol;

import com.yuxiao.buz.baseframework.networkprotocol.http.HttpRunner;

import java.util.HashMap;

/**
 * @author yu.xiao
 * @version 1.0
 * @description
 * @createDate 2017年02月28日
 */

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
