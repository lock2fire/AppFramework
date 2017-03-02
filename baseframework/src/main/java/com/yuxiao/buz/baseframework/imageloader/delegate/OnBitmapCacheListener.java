package com.yuxiao.buz.baseframework.imageloader.delegate;

import com.yuxiao.buz.baseframework.imageloader.entity.ImageTask;

public interface OnBitmapCacheListener {

    void onBitmapRetrieved(ImageTask imageTask);
}
