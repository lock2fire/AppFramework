package com.yuxiao.buz.baseframework.imageloader.delegate;

/**
 * @author yu.xiao
 * @version 1.0
 * @description
 * @createDate 2017年02月28日
 */
public enum EImageStateType
{
    EParaIllegal(-5),
    ENetworkException(-4),
    ETaskInterrupted(-3),
    EMemoryLow(-2),
    EDecodeFail(-1),
    EDownloading(0),
    EDownloaded(1),
    EDecoding(2),
    EDecoded(3);

    private final int mValue;

    EImageStateType(int value)
    {
        mValue = value;
    }
}