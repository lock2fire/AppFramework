package com.yuxiao.buz.baseframework.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.R.attr.bitmap;
import static android.R.attr.cacheColorHint;

public class BitmapUtil {

    // --------------- save bitmap to file -----------
    public static boolean saveBitmap(@NonNull Bitmap bitmap, String path, String name) {
        if(bitmap == null) {
            return false;
        }
        if(!FileUtil.mkDir(path)) {
            return false;
        }

        // inject the disk size control
        File file = FileUtil.createFileWithSizeLimited(path, name);
        if(file == null) {
            return false;
        }

        if(file.exists()) {
            FileUtil.delFile(file.getAbsolutePath());
        }

        if(file != null) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                if(bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)) {
                    fos.flush();
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    // --------------- decode resource to bitmap ----------
    public static Bitmap decodeBitmap(String absPath, int maxWidth, int maxHeight){
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(absPath, options);
            options.inJustDecodeBounds = false;
            int maxSize = maxWidth == -1 && maxHeight == -1 ? -1 : maxWidth*maxHeight;
            options.inSampleSize = computeSampleSize(options, Math.min(maxWidth, maxHeight), maxSize);
            Bitmap bitmap = BitmapFactory.decodeFile(absPath, options);
            return bitmap;
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap decodeBitmap(InputStream is, int maxWidth, int maxHeight ) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            options.inJustDecodeBounds = false;
            int maxSize = maxWidth == -1 && maxHeight == -1 ? -1 : maxWidth*maxHeight;
            options.inSampleSize = computeSampleSize(options, Math.min(maxWidth, maxHeight), maxSize);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            return bitmap;
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        // make sure the roundedSize is 2 raised to the nth power like 1, 2, 4, 8, 16, 32
        if(initialSize <= 8) {
            for(roundedSize = 1; roundedSize < initialSize; roundedSize <<= 1) {
                ;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = (double)options.outWidth;
        double h = (double)options.outHeight;
        int lowerBound = maxNumOfPixels == -1?1:(int)Math.ceil(Math.sqrt(w * h / (double)maxNumOfPixels));
        int upperBound = minSideLength == -1?128:(int)Math.min(Math.floor(w / (double)minSideLength), Math.floor(h / (double)minSideLength));
        return upperBound < lowerBound?lowerBound:(maxNumOfPixels == -1 && minSideLength == -1?1:(minSideLength == -1?lowerBound:upperBound));
    }

    // --------------- Scale bitmap -------------------
    public static Bitmap scaleBitmap(@NonNull Bitmap bitmap, int newWith, int newHeight) throws Exception{
        if(bitmap == null) {
            throw new NullPointerException("bitmap can not be null.");
        }
        return Bitmap.createScaledBitmap(bitmap,
                newWith,
                newHeight,
                true);
    }

    public static Bitmap scaleBitmap(@NonNull Bitmap bitmap, float widthScaleRatio, float heightScaleRatio) throws Exception{
        if(bitmap == null) {
            throw new NullPointerException("bitmap can not be null.");
        }
        return scaleBitmap(bitmap,
                (int) (bitmap.getWidth()*widthScaleRatio),
                (int) (bitmap.getHeight()*heightScaleRatio));
    }

    public static Bitmap scaleBitmapByWidth(@NonNull Bitmap bitmap, int newWidth) throws Exception {
        if(bitmap == null) {
            throw new NullPointerException("bitmap can not be null.");
        }
        float ratio = (float) newWidth/(float) bitmap.getWidth();
        Bitmap scaledBitmap = scaleBitmap(bitmap, ratio, ratio);
        return scaledBitmap;
    }

    public static Bitmap scaleBitmapByHeight(@NonNull Bitmap bitmap, int newHeight) throws Exception {
        if(bitmap == null) {
            throw new NullPointerException("bitmap can not be null.");
        }
        float ratio = (float) newHeight/(float) bitmap.getHeight();
        Bitmap scaledBitmap = scaleBitmap(bitmap, ratio, ratio);
        return scaledBitmap;
    }

}
