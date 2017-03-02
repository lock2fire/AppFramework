package com.yuxiao.buz.baseframework.imageloader;

import android.content.Context;
import android.widget.ImageView;

public class Cavalli {

    // call the method first, then call start
    public static void setSavePath(String savePath) {
        ImageManager.getInstance().setDownloadPath(savePath);
    }

    public static void start(Context context) {
        ImageManager.getInstance().init(context);
    }

    public static void getOnlineImg(String url,
                             ImageView imageView,
                             int width,
                             int height) {
        ImageManager.getInstance().addImageTask(url,null,imageView,false,width,height,null);
    }

    public static void getLocalImg(String localPath,
                            ImageView imageView,
                            int width,
                            int height) {
        ImageManager.getInstance().addImageTask(localPath,null,imageView,true,width,height, null);
    }
}
