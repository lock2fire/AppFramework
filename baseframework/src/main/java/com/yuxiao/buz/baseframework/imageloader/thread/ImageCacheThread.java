package com.yuxiao.buz.baseframework.imageloader.thread;

import android.graphics.Bitmap;

import com.yuxiao.buz.baseframework.common.MD5Util;
import com.yuxiao.buz.baseframework.imageloader.entity.ImageTask;
import com.yuxiao.buz.baseframework.imageloader.cache.BitmapCache;
import com.yuxiao.buz.baseframework.imageloader.delegate.OnBitmapCacheListener;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

public class ImageCacheThread extends Thread {

    BlockingDeque<ImageTask> blockingDeque = new LinkedBlockingDeque<>();
    List<ImageTask> saveBitmapCacheList = new CopyOnWriteArrayList<>();
    List<ImageTask> helperList = new CopyOnWriteArrayList<>();
    OnBitmapCacheListener onBitmapCacheListener;
    BitmapCache bitmapCache;

    public ImageCacheThread(BitmapCache bitmapCache) {
        this.bitmapCache = bitmapCache;
    }

    public void setOnBitmapCacheListener(OnBitmapCacheListener onBitmapCacheListener) {
        this.onBitmapCacheListener = onBitmapCacheListener;
    }

    public void addCacheImgTask(ImageTask imageTask) {

        if(imageTask == null && imageTask.imageUri == null) {
            return;
        }

        Iterator<ImageTask> imageTaskIterator = helperList.iterator();
        boolean isExist = false;
        while (imageTaskIterator.hasNext()) {
            ImageTask iTask = imageTaskIterator.next();
            if(imageTask.imageUri.equals(iTask.imageUri)) {
                isExist = true;
                break;
            }
        }

        if(!isExist) {
            helperList.add(imageTask);
            blockingDeque.offer(imageTask);
        }
    }

    public void saveBitmapCache(ImageTask imageTask) {
        saveBitmapCacheList.add(imageTask);
        interrupt();
    }

    @Override
    public void run() {

        while (true) {

            // first of all, put bitmap to cache in the waiting list
            Iterator<ImageTask> iterator = saveBitmapCacheList.iterator();
            while (iterator.hasNext()) {
                ImageTask saveImageTask = iterator.next();
                Reference<Bitmap> reference = saveImageTask.bitmapWeakReference;
                if(reference != null && reference.get() != null) {
                    Bitmap bitmap = reference.get();
                    bitmapCache.put(saveImageTask, bitmap);
                }
                saveBitmapCacheList.remove(saveImageTask);
            }


            try {
                ImageTask imageTask = blockingDeque.takeLast();

                String fileName = MD5Util.MD5(imageTask.imageUri); // put here to encode to not block UI thread
                imageTask.md5 = fileName;

                if(imageTask == null) {
                    continue;
                }

                if(bitmapCache != null) {

                    Bitmap bitmap = bitmapCache.get(imageTask);
                    if(bitmap != null) {
                        imageTask.bitmapWeakReference = new WeakReference<Bitmap>(bitmap);
                    } else {
                        imageTask.bitmapWeakReference = null;
                    }
                    if(onBitmapCacheListener != null) {
                        onBitmapCacheListener.onBitmapRetrieved(imageTask);
                    }
                }

                helperList.remove(imageTask);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}
