package com.yuxiao.buz.baseframework.common;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class RootPathFinder {
    static String packageName = null;

    public static void init(Context context, String...paths) {
        setPackageName(context);
        if(paths != null) {
            for (String path : paths) {
                FileUtil.mkDir(getOwnerPath(context)+path);
                if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    FileUtil.mkDir(getRootPath(context)+path);
                }
            }
        }
    }

    public static void setPackageName(Context context) {
        packageName = context.getPackageName();
    }

    public static String getRootPath(Context context) {
        if(packageName == null) {
            if(context == null) {
                throw new NullPointerException("context can not be null when packageName is not set");
            }
            setPackageName(context);
        }
        String path = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            path = getExternalStorageDirectory();
        } else {
            if(context == null) {
                throw new NullPointerException("context can not be null when there's no SD card");
            }
            path = getOwnerPath(context);
        }
        return path;
    }

    private static String getOwnerPath(Context context){
        return context.getFilesDir().toString()+File.separator;
    }

    private static String getExternalStorageDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + packageName + File.separator;
    }
}
