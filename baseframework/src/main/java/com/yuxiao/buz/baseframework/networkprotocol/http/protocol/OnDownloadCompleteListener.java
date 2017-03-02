package com.yuxiao.buz.baseframework.networkprotocol.http.protocol;

import java.util.HashMap;

public interface OnDownloadCompleteListener
{
    void onDownloadComplete(
            String url,
            HashMap<String, String> httpHeaders,
            HashMap<String, String> parameters,
            boolean isRequestWithPost,
            Object extra,
            String downloadFileCachePath);
}
