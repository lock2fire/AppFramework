package com.yuxiao.buz.baseframework.imageloader.thread;


import com.yuxiao.buz.baseframework.imageloader.db.ImageDBManager;
import com.yuxiao.buz.baseframework.imageloader.delegate.EImageStateType;
import com.yuxiao.buz.baseframework.imageloader.delegate.OnDownloadFinishListener;
import com.yuxiao.buz.baseframework.imageloader.entity.ImageTask;
import com.yuxiao.buz.baseframework.networkprotocol.http.HttpRunner;
import com.yuxiao.buz.baseframework.networkprotocol.http.protocol.OnDownloadCompleteListener;
import com.yuxiao.buz.baseframework.networkprotocol.http.protocol.OnExceptionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author yu.xiao
 * @version 1.0
 * @description
 * @createDate 2017年02月28日
 */
public class ImageDownloadQueue {

    int threadNum = 1;

    List<ImageDownloaderThread> imageDownloaderThreadList = new ArrayList<>();
    BlockingDeque<ImageTask> imageTaskBlockingDeque = new LinkedBlockingDeque<>();
    OnDownloadFinishListener imgDownloadFinishListener;
    List<ImageTask> helperList = new CopyOnWriteArrayList<>();

    public ImageDownloadQueue(int threadNum) {
        this.threadNum = threadNum;
    }

    public void setImgDownloadFinishListener(OnDownloadFinishListener listener) {
        this.imgDownloadFinishListener = listener;
        for (ImageDownloaderThread imageDownloaderThread : imageDownloaderThreadList) {
            imageDownloaderThread.setOnDownloadFinishListener(listener);
        }
    }

    public void start() {
        for(int i = 0; i < threadNum; i++) {
            ImageDownloaderThread imageDownloaderThread = new ImageDownloaderThread(imageTaskBlockingDeque, helperList);
            imageDownloaderThreadList.add(imageDownloaderThread);
            imageDownloaderThread.setOnDownloadFinishListener(imgDownloadFinishListener);
            imageDownloaderThread.start();
        }
    }

    public void addDownloadImgTask(ImageTask imageTask) {

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
            imageTaskBlockingDeque.offer(imageTask);
        }

    }

    private static class ImageDownloaderThread extends Thread {
        BlockingDeque<ImageTask> blockingDeque;
        List<ImageTask> helperList;
        OnDownloadFinishListener onDownloadFinishListener;

        ImageDownloaderThread(BlockingDeque<ImageTask> blockingDeque, List<ImageTask> helperList) {
            this.blockingDeque = blockingDeque;
            this.helperList = helperList;
        }

        public void setOnDownloadFinishListener(OnDownloadFinishListener listener) {
            this.onDownloadFinishListener = listener;
        }

        OnDownloadCompleteListener onDownloadCompleteListener = new OnDownloadCompleteListener() {
            @Override
            public void onDownloadComplete(String url, HashMap<String, String> httpHeaders, HashMap<String, String> parameters, boolean isRequestWithPost, Object extra, String downloadFileCachePath) {
                ImageDBManager.getInstance(null).updateImageStatus((ImageTask) extra, 2);
                if(onDownloadFinishListener != null) {
                    onDownloadFinishListener.downloadFinish(EImageStateType.EDownloaded, (ImageTask) extra);
                }
                helperList.remove((ImageTask) extra);
            }
        };

        OnExceptionListener onExceptionListener = new OnExceptionListener() {
            @Override
            public void onHttpException(String url, HashMap<String, String> httpHeaders, HashMap<String, String> parameters, boolean isRequestWithPost, Object extra, HttpRunner.EHttpState state, String stateDes) {
                // 如果下载失败把状态值设置为-1，但是在测试阶段可以先设置为0
                ImageDBManager.getInstance(null).updateImageStatus((ImageTask) extra, 0); // -1;
                if(onDownloadFinishListener != null) {
                    onDownloadFinishListener.downloadFinish(EImageStateType.EDownloaded, (ImageTask) extra);
                }
                helperList.remove((ImageTask) extra);
            }
        };

        @Override
        public void run() {

            while (!this.isInterrupted()) {

                try {

                    final ImageTask imageTask = blockingDeque.takeLast();

                    if(imageTask == null) {
                        continue;
                    }

                    if(imageTask.isInterrupt) {
                        helperList.remove(imageTask);
                        continue;
                    }

                    if(ImageDBManager.getInstance(null).isImageTaskExist(imageTask)) {
                        int status = ImageDBManager.getInstance(null).readImageTaskStatus(imageTask);
                        if(status == -1 || status == 1) {
                            continue;
                        } else if(status == 2) {
                            File file = new File(imageTask.savePathDir+imageTask.md5);
                            if(file.isFile() && file.exists() && file.length() > 0) {
                                if(onDownloadFinishListener != null) {
                                    onDownloadFinishListener.downloadFinish(EImageStateType.EDownloaded, imageTask);
                                }
                            } else {
                                file.delete();
                            }
                        }
                    } else {
                        ImageDBManager.getInstance(null).addImage(imageTask);
                    }

                    ImageDBManager.getInstance(null).updateImageStatus(imageTask, 1);

                    HttpRunner.doGetHttpDownloadRequest(imageTask.imageUri,
                            imageTask.headerMap,
                            imageTask.savePathDir+imageTask.md5,
                            0,
                            imageTask,
                            new HttpRunner.ConnectionInterruptSignal(imageTask.isInterrupt),
                            null,
                            null,
                            onDownloadCompleteListener,
                            onExceptionListener);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
