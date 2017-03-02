package com.yuxiao.buz.baseframework.imageloader.delegate;

import android.graphics.Bitmap;

/**
 * Created by yu.xiao on 2016/5/25.
 */
public interface OnImageStateListener
{
    public void onImageStateChange(
            EImageStateType state,
            Bitmap image,
            Object extraData);
}
