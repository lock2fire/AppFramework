package com.yuxiao.buz.baseframework.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.yuxiao.buz.baseframework.imageloader.cache.MemoryCache;
import com.yuxiao.buz.baseframework.imageloader.db.ImageDBManager;
import com.yuxiao.buz.baseframework.imageloader.delegate.EImageStateType;
import com.yuxiao.buz.baseframework.imageloader.delegate.OnBitmapCacheListener;
import com.yuxiao.buz.baseframework.imageloader.delegate.OnDecodeFinishListener;
import com.yuxiao.buz.baseframework.imageloader.delegate.OnDownloadFinishListener;
import com.yuxiao.buz.baseframework.imageloader.delegate.OnImageStateListener;
import com.yuxiao.buz.baseframework.imageloader.entity.ImageTask;
import com.yuxiao.buz.baseframework.imageloader.thread.ImageCacheThread;
import com.yuxiao.buz.baseframework.imageloader.thread.ImageDecodeQueue;
import com.yuxiao.buz.baseframework.imageloader.thread.ImageDownloadQueue;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class ImageManager {

    private static final int INTERVAL_TIME = 16;
    private static final int TAG_INT = R.id.image_tag;
    private static ImageManager instance = null;
    private String downloadPath = "";
    private Bitmap bitmapHolder;

    private Handler handler = new Handler(Looper.getMainLooper());

    private CopyOnWriteArrayList<ImageTask> deliveryList = new CopyOnWriteArrayList<>();
    private HashMap<String, List<WeakReference<ImageView>>> imageViewsMap = new HashMap<>();

    private Runnable deliveryRunnable = new Runnable() {
        @Override
        public void run() {
            ImageTask imageTask = null;
            Iterator<ImageTask> it = deliveryList.iterator();
            if(it.hasNext()) {
                imageTask = it.next();
                Bitmap bitmap = imageTask.bitmapWeakReference.get();
                if(bitmap != null) {
                    synchronized (ImageManager.this) {
                        List<WeakReference<ImageView>> list = imageViewsMap.get(imageTask.imageUri);
                        if(list.isEmpty()) {
                            deliveryList.remove(imageTask);
                        } else {
                            WeakReference<ImageView> reference = list.remove(0);
                            if(reference.get() != null) {
                                if(imageTask.imageUri.equals(reference.get().getTag(TAG_INT))) {
                                    reference.get().setImageBitmap(bitmap);
                                }
                            }
                            if(list.isEmpty()) {
                                deliveryList.remove(imageTask);
                            }
                        }
                    }
                }
            }
            if(!deliveryList.isEmpty()) {
                handler.postDelayed(this, INTERVAL_TIME);
            }
        }
    };


    public static synchronized ImageManager getInstance() {
        if(instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }

    ImageDownloadQueue imageDownloadQueue = new ImageDownloadQueue(3);
    ImageDecodeQueue imageDecodeQueue = new ImageDecodeQueue(2);
    ImageCacheThread imageCacheThread = new ImageCacheThread(new MemoryCache());

    public void init(Context context) {
        bitmapHolder = Bitmap.createBitmap(1,1, Bitmap.Config.RGB_565);
        imageDownloadQueue.setImgDownloadFinishListener(onDownloadFinishListener);
        imageDecodeQueue.setOnDecodeFinishListener(onDecodeFinishListener);
        imageCacheThread.setOnBitmapCacheListener(onBitmapCacheListener);
        ImageDBManager.getInstance(context);
        imageDownloadQueue.start();
        imageDecodeQueue.start();
        imageCacheThread.start();
    }

    public void setDownloadPath(String path) {
        this.downloadPath = path;
    }

    public void addImageTask(String url,
                             HashMap<String, String> requestHeaders,
                             ImageView targetImgView,
                             boolean isLocal,
                             int desiredWidth,
                             int desiredHeight,
                             OnImageStateListener listener) {
        if(url == null) {
            return;
        }

        ImageTask imageTask = new ImageTask();
        imageTask.imageUri = url;
        imageTask.isLocal = isLocal;
        imageTask.savePathDir = downloadPath;
        imageTask.requireWidth = desiredWidth;
        imageTask.requireHeight = desiredHeight;

        targetImgView.setImageBitmap(bitmapHolder);

        final String lastUrl = (String) targetImgView.getTag(TAG_INT);
        targetImgView.setTag(TAG_INT, url);

        synchronized (ImageManager.this) {
            // remove targetView from previous notification list
            if(lastUrl != null) {
                List<WeakReference<ImageView>> lastImageViewsList = imageViewsMap.get(lastUrl);
                if(lastImageViewsList != null) {
                    Iterator<WeakReference<ImageView>> iterator = lastImageViewsList.iterator();
                    while (iterator.hasNext()) {
                        ImageView imageView = iterator.next().get();
                        if(targetImgView == imageView) {
                            iterator.remove();
                            break;
                        }
                    }
                }
            }
            // add targetView to new notification list
            List<WeakReference<ImageView>> imageViewsList = imageViewsMap.get(url);
            if(imageViewsList != null) {
                boolean isExist = false;
                for(WeakReference<ImageView> reference : imageViewsList) {
                    ImageView imageView = reference.get();
                    if(targetImgView == imageView) {
                        isExist = true;
                        break;
                    }
                }
                if(!isExist) {
                    imageViewsList.add(new WeakReference<ImageView>(targetImgView));
                }

            } else {
                imageViewsList = new ArrayList<>();
                imageViewsList.add(new WeakReference<ImageView>(targetImgView));
                imageViewsMap.put(url, imageViewsList);
            }
        }

        if(imageCacheThread != null) {
            imageCacheThread.addCacheImgTask(imageTask);
        }
    }

    private void deliveryToUI(ImageTask imageTask) {
        if(imageTask == null
                || imageTask.isInterrupt) {
            return;
        }
        deliveryList.add(imageTask);
        handler.removeCallbacks(deliveryRunnable);
        handler.postDelayed(deliveryRunnable, INTERVAL_TIME);
    }

    OnBitmapCacheListener onBitmapCacheListener = new OnBitmapCacheListener() {
        @Override
        public void onBitmapRetrieved(ImageTask imageTask) {
            if(imageTask != null) {
                // if bitmap is ready, send to UI handler
                Reference<Bitmap> weakReference = imageTask.bitmapWeakReference;
                if(weakReference != null && weakReference.get() != null) {
                    deliveryToUI(imageTask);
                } else {
                    // if load the local bitmap, add to decode queue directly
                    // otherwise, add to download queue
                    if(imageTask.isLocal) {
                        imageDecodeQueue.addDecodeImageTask(imageTask);
                    } else {
                        int status = ImageDBManager.getInstance(null).readImageTaskStatus(imageTask);
                        if(status <= 0) {
                            imageDownloadQueue.addDownloadImgTask(imageTask);
                        } else if (status == 2) {
                            File file = new File(imageTask.savePathDir+imageTask.md5);
                            if(file.isFile() && file.exists() && file.length() > 0) {
                                imageDecodeQueue.addDecodeImageTask(imageTask);
                            } else {
                                // the status in database shows the file is already downloaded
                                // but actually there's no such file existed.
                                // just delete and redownload
                                file.delete();
                                imageDownloadQueue.addDownloadImgTask(imageTask);
                            }
                        }
                    }
                }
            }
        }
    };

    OnDownloadFinishListener onDownloadFinishListener = new OnDownloadFinishListener() {
        @Override
        public void downloadFinish(EImageStateType stateType, ImageTask imageTask) {
            if(stateType == EImageStateType.EDownloaded && imageDecodeQueue != null) {
                imageDecodeQueue.addDecodeImageTask(imageTask);
            } else {
                synchronized (ImageManager.this) {
                    imageViewsMap.remove(imageTask.imageUri);
                }
            }
        }
    };

    OnDecodeFinishListener onDecodeFinishListener = new OnDecodeFinishListener() {
        @Override
        public void decodeFinish(EImageStateType stateType, ImageTask imageTask) {
            if(stateType == EImageStateType.EDecoded) {
                imageCacheThread.saveBitmapCache(imageTask);
                deliveryToUI(imageTask);
            } else {
                synchronized (ImageManager.this) {
                    imageViewsMap.remove(imageTask.imageUri);
                }
            }
        }
    };

}
