package com.yuxiao.buz.baseframework.imageloader;

import android.content.Context;
import android.widget.ImageView;

/**
 * @author yu.xiao
 * @version 1.0
 * @description
 * @createDate 2017年02月28日
 */
public class Cavalli {

    // 先调用该函数，再调用start
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
