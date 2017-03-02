package com.yuxiao.buz.baseframework.imageloader.delegate;

import com.yuxiao.buz.baseframework.imageloader.entity.ImageTask;

/**
 * @author yu.xiao
 * @version 1.0
 * @description
 * @createDate 2017年02月28日
 */
public interface OnDownloadFinishListener {

    public void downloadFinish(EImageStateType stateType, ImageTask imageTask);
}
