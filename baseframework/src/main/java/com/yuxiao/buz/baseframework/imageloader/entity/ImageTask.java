package com.yuxiao.buz.baseframework.imageloader.entity;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yu.xiao
 * @version 1.0
 * @description
 * @createDate 2017年02月28日
 */
public class ImageTask {
    public Reference<Bitmap> bitmapWeakReference;
    public HashMap<String, String> headerMap;
    public String imageUri = ""; // 可能是本地也可能是远程的路径
    public String savePathDir = ""; // 保存路径，是一个文件夹
    public String md5 = "";
    public boolean isLocal;
    public int requireWidth;
    public int requireHeight;
    public volatile boolean isSaveBitmap;
    public volatile boolean isInterrupt; // 是否被中断
}
