package com.yuxiao.buz.baseframework.imageloader.thread;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yuxiao.buz.baseframework.common.BitmapUtil;
import com.yuxiao.buz.baseframework.imageloader.delegate.EImageStateType;
import com.yuxiao.buz.baseframework.imageloader.entity.ImageTask;
import com.yuxiao.buz.baseframework.imageloader.delegate.OnDecodeFinishListener;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
public class ImageDecodeQueue {

    OnDecodeFinishListener onDecodeFinishListener;

    int threadNum = 1;

    List<ImageDecoderThread> imageDecoderThreadList = new ArrayList<>();
    BlockingDeque<ImageTask> imageTaskBlockingDeque = new LinkedBlockingDeque<>();
    List<ImageTask> helperList = new CopyOnWriteArrayList<>();

    public ImageDecodeQueue(int threadNum) {
        this.threadNum = threadNum;
    }

    public void addDecodeImageTask(ImageTask imageTask) {
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

    public void setOnDecodeFinishListener(OnDecodeFinishListener listener) {
        this.onDecodeFinishListener = listener;
        for (ImageDecoderThread imageDecoderThread : imageDecoderThreadList) {
            imageDecoderThread.setOnDecodeFinishListener(listener);
        }
    }

    public void start() {
        for(int i = 0; i < threadNum; i++) {
            ImageDecoderThread imageDecoderThread = new ImageDecoderThread(imageTaskBlockingDeque, helperList);
            imageDecoderThreadList.add(imageDecoderThread);
            imageDecoderThread.setOnDecodeFinishListener(onDecodeFinishListener);
            imageDecoderThread.start();
        }
    }

    private static class ImageDecoderThread extends Thread {
        BlockingDeque<ImageTask> blockingDeque;
        OnDecodeFinishListener onDecodeFinishListener;
        List<ImageTask> helperList;

        ImageDecoderThread(BlockingDeque<ImageTask> blockingDeque, List<ImageTask> helperList) {
            this.blockingDeque = blockingDeque;
            this.helperList = helperList;
        }

        public void setOnDecodeFinishListener(OnDecodeFinishListener listener) {
            this.onDecodeFinishListener = listener;
        }

        @Override
        public void run() {

            while (!this.isInterrupted()) {

                try {
                    ImageTask imageTask = blockingDeque.takeLast();

                    if(imageTask == null) {
                        continue;
                    }

                    String path = imageTask.isLocal ? imageTask.imageUri : imageTask.savePathDir+imageTask.md5;
//                    Point imageSize = getImageViewSize(imageTask.imageViewReference.get());
//                    int width = imageTask.requireWidth == 0 ? imageSize.x : imageTask.requireWidth;
//                    int height = imageTask.requireHeight == 0 ? imageSize.y : imageTask.requireHeight;
                    Bitmap bitmap = BitmapUtil.decodeBitmap(path, imageTask.requireWidth, imageTask.requireHeight);

                    if(onDecodeFinishListener != null) {
                        if(bitmap == null) {
                            onDecodeFinishListener.decodeFinish(EImageStateType.EDecodeFail, imageTask);
                        } else {
                            imageTask.bitmapWeakReference = new WeakReference<Bitmap>(bitmap);
                            onDecodeFinishListener.decodeFinish(EImageStateType.EDecoded, imageTask);
                        }
                    }

                    helperList.remove(imageTask);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static Point getImageViewSize(ImageView imageView)
    {
        Point imageSize = new Point();

        final DisplayMetrics displayMetrics = imageView.getContext()
                .getResources().getDisplayMetrics();

        if(imageView == null) {
            imageSize.x = displayMetrics.widthPixels;
            imageSize.y = displayMetrics.heightPixels;
            return imageSize;
        }

        final ViewGroup.LayoutParams params = imageView.getLayoutParams();

        int width = params.width == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView
                .getWidth(); // Get actual image width
        if (width <= 0)
            width = params.width; // Get layout width parameter
        if (width <= 0)
            width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check
        // maxWidth
        // parameter
        if (width <= 0)
            width = displayMetrics.widthPixels;
        int height = params.height == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView
                .getHeight(); // Get actual image height
        if (height <= 0)
            height = params.height; // Get layout height parameter
        if (height <= 0)
            height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check
        // maxHeight
        // parameter
        if (height <= 0)
            height = displayMetrics.heightPixels;
        imageSize.x = width;
        imageSize.y = height;
        return imageSize;
    }

    private static int getImageViewFieldValue(Object object, String fieldName)
    {
        int value = 0;
        try
        {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE)
            {
                value = fieldValue;
                Log.e("TAG", value + "");
            }
        } catch (Exception e)
        {
        }
        return value;
    }


}
