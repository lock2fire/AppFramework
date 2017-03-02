package com.yuxiao.buz.baseframework.imageloader.entity;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ImageTask {
    public Reference<Bitmap> bitmapWeakReference;
    public HashMap<String, String> headerMap;
    public String imageUri = ""; // may local path or web http link
    public String savePathDir = ""; // the directory to save pic
    public String md5 = "";
    public boolean isLocal;
    public int requireWidth;
    public int requireHeight;
    public volatile boolean isInterrupt; // if cancelled
}
