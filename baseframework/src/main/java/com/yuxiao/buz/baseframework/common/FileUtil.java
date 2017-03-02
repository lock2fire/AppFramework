package com.yuxiao.buz.baseframework.common;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

public class FileUtil {
    final static int MIN_AVAILABLE_DISK_SIZE_IN_M = 50; // 50M
    final static int AVAILABLE_DISK_SIZE = 50*1024*1024; // 50M
    // ------------ create or get --------------
    public static boolean mkDir(String path) {
        File f = new File(path);
        if(!f.exists()) {
            return f.mkdirs();
        }
        return true;
    }

    public static File getFile(String absPath) {
        try {
            File f = new File(absPath);
            if(!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }

            if(!f.exists()) {
                f.createNewFile();
            }

            return f;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getFile(String path, String name) {
        if(!mkDir(path)) {
            return null;
        }
        File file = new File(path, name);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File createFileWithSizeLimited(String path, String name) {
        if(getFreeSize(path) < MIN_AVAILABLE_DISK_SIZE_IN_M) {
            return null;
        } else {
            return new File(path, name);
        }
    }

    public static File createFileWithSizeLimited(String absPath) {
        if(getFreeSize(absPath) < MIN_AVAILABLE_DISK_SIZE_IN_M) {
            return null;
        } else {
            return new File(absPath);
        }
    }

    // ----------del -------------
    public static boolean delFile(String path, String name) {
        boolean flag = false;
        File file = new File(path, name);
        if(file.isFile()) {
            flag = file.delete();
        }
        return flag;
    }

    public static boolean delFile(String absPath) {
        boolean flag = false;
        File file = new File(absPath);
        if(file.isFile()) {
            flag = file.delete();
        }
        return flag;
    }
    // -------- other -----------------
    public static long getFreeSize(String path) {
        StatFs sf = new StatFs(path);
        // single block size(Byte)
        long blockSize = 1024;
        try {
            blockSize = sf.getBlockSize();
        } catch (Exception e) {
            if(Build.VERSION.SDK_INT >= 18) {
                try {
                    blockSize = sf.getBlockSizeLong();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        // free block count
        long freeBlocks = AVAILABLE_DISK_SIZE;
        try {
            freeBlocks = sf.getAvailableBlocks();
        } catch (Exception e) {
            if(Build.VERSION.SDK_INT >= 18) {
                try {
                    freeBlocks = sf.getAvailableBlocksLong();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        // return freeBlocks * blockSize; // unit Byte
        // return (freeBlocks * blockSize)/1024; //unit KB
        return (freeBlocks * blockSize) / 1024 / 1024; // unit MB
    }
}
