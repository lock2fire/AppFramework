package com.yuxiao.buz.baseframework.imageloader.delegate;

import android.graphics.Bitmap;

public interface OnImageStateListener
{
    void onImageStateChange(
            EImageStateType state,
            Bitmap image,
            Object extraData);
}
