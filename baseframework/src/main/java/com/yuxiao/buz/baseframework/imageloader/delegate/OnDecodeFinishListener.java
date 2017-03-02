package com.yuxiao.buz.baseframework.imageloader.delegate;

import com.yuxiao.buz.baseframework.imageloader.entity.ImageTask;

public interface OnDecodeFinishListener {

    void decodeFinish(EImageStateType stateType, ImageTask imageTask);
}
