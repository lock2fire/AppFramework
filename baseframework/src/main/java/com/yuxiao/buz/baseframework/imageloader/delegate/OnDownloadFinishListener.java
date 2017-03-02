package com.yuxiao.buz.baseframework.imageloader.delegate;

import com.yuxiao.buz.baseframework.imageloader.entity.ImageTask;

public interface OnDownloadFinishListener {

    void downloadFinish(EImageStateType stateType, ImageTask imageTask);
}
