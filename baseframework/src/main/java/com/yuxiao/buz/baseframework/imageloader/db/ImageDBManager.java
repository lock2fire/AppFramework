/*
*online image state database
 */
package com.yuxiao.buz.baseframework.imageloader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yuxiao.buz.baseframework.imageloader.entity.ImageTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by yu.xiao on 2016/5/25.
 */
public class ImageDBManager
{
    private static ImageDBManager mInstance;

    private ImageDBHelper mDBHelper;

    private SQLiteDatabase mDataBase;

    private final String KComma = ",";
    private final String KCommaReplace = "[comma]";
    private final String KColon = ":";
    private final String KColonReplace = "[colon]";

    /**
     * @description constructor
     * @author yu.xiao
     * @createDate 2016-06-20
     * @param context
     * @return
     */
    private ImageDBManager(Context context)
    {
        if(context != null)
        {

            mDBHelper = new ImageDBHelper(context);

            try
            {
                mDataBase = mDBHelper.getWritableDatabase();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * @description get instance
     * @author yu.xiao
     * @createDate 2016-06-20
     * @param context
     * @return ImageDBManager
     */
    public static ImageDBManager getInstance(Context context)
    {
        if(mInstance == null ||
                context != null)
        {
            mInstance = new ImageDBManager(context);
        }

        return mInstance;
    }

    /**
     * @description add image
     * @author yu.xiao
     * @createDate 2016-06-20
     * @param imageTask
     * @return true succeeded, false failed
     */
    public boolean addImage(ImageTask imageTask)
    {
        if(mDBHelper == null ||
                imageTask == null)
        {
            return false;
        }

        if(mDataBase == null)
        {
            mDataBase = mDBHelper.getWritableDatabase();
        }

        if(mDataBase == null)
        {
            return false;
        }

        try {

            ContentValues contentValues = new ContentValues();

            if(imageTask.md5 != null)
            {
                contentValues.put(ImageDBHelper.KImageID, imageTask.md5);
            }

            if(imageTask.imageUri != null)
            {
                contentValues.put(ImageDBHelper.KOnlineResURL, imageTask.imageUri);
            }

            contentValues.put(ImageDBHelper.KCacheStatus, 0);
            contentValues.put(ImageDBHelper.KResWidth, imageTask.requireWidth);
            contentValues.put(ImageDBHelper.ResHeight, imageTask.requireHeight);

            mDataBase.insert(
                    ImageDBHelper.KTableImage,
                    null,
                    contentValues);

        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean isImageTaskExist(ImageTask imageTask) {
        if(mDBHelper == null ||
                imageTask == null)
        {
            return false;
        }

        if(mDataBase == null)
        {
            mDataBase = mDBHelper.getWritableDatabase();
        }

        if(mDataBase == null)
        {
            return false;
        }

        try {

            String sqlStr =
                    "SELECT * " +
                            " FROM " +
                            ImageDBHelper.KTableImage +
                            " WHERE " +
                            ImageDBHelper.KImageID + " = " + "\"" + imageTask.md5 + "\"";

            Cursor cursor =
                    mDataBase.rawQuery(
                            sqlStr,
                            null);

            if(cursor != null )
            {
                cursor.close();
                if(cursor.getCount() > 0 &&
                        cursor.moveToFirst()) {
                    return true;
                }
            }

        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }

        return false;
    }


    /**
     * @description request image data
     * @author yu.xiao
     * @createDate 2016-06-20
     * @param imageTask
     * @return ImageData
     */
    public int readImageTaskStatus(ImageTask imageTask)
    {
        if(mDBHelper == null ||
                imageTask == null ||
                imageTask.md5 == null)
        {
            return 0;
        }

        if(mDataBase == null)
        {
            mDataBase = mDBHelper.getWritableDatabase();
        }

        if(mDataBase == null)
        {
            return 0;
        }

        int status = 0;

        Cursor cursor = null;

        try {
            String sqlStr =
                    "SELECT * " +
                            " FROM " +
                            ImageDBHelper.KTableImage +
                            " WHERE " +
                            ImageDBHelper.KImageID + " = " + "\"" + imageTask.md5 + "\"";

            cursor =
                    mDataBase.rawQuery(
                            sqlStr,
                            null);

            if(cursor == null ||
                    cursor.getCount() <= 0 ||
                    !cursor.moveToFirst())
            {
                if(cursor != null)
                {
                    cursor.close();
                }

                return status; // 不存在
            }

            status = cursor.getInt(cursor.getColumnIndex(ImageDBHelper.KCacheStatus));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }

        return status;
    }

    /**
     * @description update image data
     * @author yu.xiao
     * @createDate 2016-06-20
     * @param imageTask
     * @return true succeeded, false failed
     */
    public boolean updateImageStatus(ImageTask imageTask, int status)
    {
        if(mDBHelper == null ||
                imageTask == null ||
                imageTask.md5 == null)
        {
            return false;
        }

        if(mDataBase == null)
        {
            mDataBase = mDBHelper.getWritableDatabase();
        }

        if(mDataBase == null)
        {
            return false;
        }

        Cursor cursor = null;

        try {
            //check existence
            String sqlStr =
                    "SELECT * " +
                            " FROM " +
                            ImageDBHelper.KTableImage +
                            " WHERE " +
                            ImageDBHelper.KImageID + " = " + "\"" + imageTask.md5 + "\"";

            cursor =
                    mDataBase.rawQuery(
                            sqlStr,
                            null);

            if(cursor == null ||
                    cursor.getCount() <= 0)
            {
                if(cursor != null)
                {
                    cursor.close();
                }

                return false;
            }

            sqlStr =
                    "UPDATE " +
                            ImageDBHelper.KTableImage +
                            " SET " +
                            ImageDBHelper.KCacheStatus + " = " + status +
                            " WHERE " +
                            ImageDBHelper.KImageID + " = " + "\"" + imageTask.md5 + "\"";

            mDataBase.execSQL(sqlStr);

        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }

        return true;
    }

    /**
     * @description remove image data
     * @author yu.xiao
     * @createDate 2016-06-20
     * @param imageTask
     * @return true succeeded, false failed
     */
    public boolean removeImage(ImageTask imageTask)
    {
        if(mDBHelper == null ||
                imageTask == null ||
                imageTask.md5 == null)
        {
            return false;
        }

        if(mDataBase == null)
        {
            mDataBase = mDBHelper.getWritableDatabase();
        }

        if(mDataBase == null)
        {
            return false;
        }

        try {
            //
            String sqlStr =
                    "DELETE " +
                            " FROM " +
                            ImageDBHelper.KTableImage +
                            " WHERE " +
                            ImageDBHelper.KImageID + " = " + "\"" + imageTask.md5 + "\"";

            mDataBase.execSQL(sqlStr);

        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * @description remove all image datas
     * @author yu.xiao
     * @createDate 2016-06-20
     * @return
     */
    public void removeAllImage()
    {
        if(mDBHelper == null)
        {
            return;
        }

        if(mDataBase == null)
        {
            mDataBase = mDBHelper.getWritableDatabase();
        }

        if(mDataBase == null)
        {
            return;
        }

        try {
            //delete columns from base table
            String sqlStr = "DELETE  FROM " + ImageDBHelper.KTableImage;

            mDataBase.execSQL(sqlStr);

        } catch (Exception e) {
//            e.printStackTrace();
        }

    }

    /**
     * @description encode string map tp string
     * @author yu.xiao
     * @createDate 2016-06-20
     * @param sourceStrMap
     * @return String
     */
    private String encodeStrMap(HashMap<String, String> sourceStrMap)
    {
        if(sourceStrMap == null ||
                sourceStrMap.size() <= 0)
        {
            return null;
        }

        StringBuffer encodedStrBuffer = new StringBuffer();

        Iterator iterator = sourceStrMap.entrySet().iterator();

        while(iterator.hasNext())
        {
            Map.Entry entry = (Map.Entry)iterator.next();

            String key = (String)entry.getKey();
            String value = (String)entry.getValue();

            if(key == null ||
                    value == null ||
                    key.length() <= 0 ||
                    value.length() <= 0)
            {
                continue;
            }

            value = new String(value);

            try
            {
                value = value.replace(KComma, KCommaReplace);
                value = value.replace(KColon, KColonReplace);
            }
            catch(Exception e)
            {
                //
            }

            String strItemStr = key + ":" + value;

            encodedStrBuffer.append(strItemStr);

            if(iterator.hasNext())
            {
                encodedStrBuffer.append(",");
            }
        }

        return encodedStrBuffer.toString();
    }

    /**
     * @description decode string to string map
     * @author yu.xiao
     * @createDate 2016-06-20
     * @param sourceStrMapStr
     * @return HashMap<String, String>
     */
    private HashMap<String, String> decodeStrMapStr(String sourceStrMapStr)
    {
        if(sourceStrMapStr == null ||
                sourceStrMapStr.length() <= 0)
        {
            return null;
        }

        String[] strArray = sourceStrMapStr.split(",");

        HashMap<String, String> targetStrMap = null;

        if(strArray == null ||
                strArray.length <= 0)
        {
            String[] strKeyValue = sourceStrMapStr.split(":");

            if(strKeyValue == null ||
                    strKeyValue.length <= 1)
            {
                return null;
            }

            String key = strKeyValue[0];
            String value = strKeyValue[1];

            if(key == null ||
                    value == null ||
                    key.trim().length() <= 0 ||
                    value.length() <= 0)
            {
                return null;
            }

            if(targetStrMap == null)
            {
                targetStrMap = new HashMap<String, String>();
            }

            targetStrMap.put(key, value);

            return targetStrMap;
        }

        //
        for(int i = 0; i < strArray.length; i++)
        {
            String strItemStr = strArray[i];

            if(strItemStr == null ||
                    strItemStr.length() <= 0)
            {
                continue;
            }

            String[] strKeyValue = strItemStr.split(":");

            if(strKeyValue == null ||
                    strKeyValue.length <= 1)
            {
                return null;
            }

            String key = strKeyValue[0];
            String value = strKeyValue[1];

            if(key == null ||
                    value == null ||
                    key.trim().length() <= 0 ||
                    value.length() <= 0)
            {
                continue;
            }

            value = value.replace(KCommaReplace, KComma);
            value = value.replace(KColonReplace, KColon);

            if(targetStrMap == null)
            {
                targetStrMap = new HashMap<String, String>();
            }

            targetStrMap.put(key, value);
        }

        return targetStrMap;
    }

    /**
     * @description close database
     * @author yu.xiao
     * @createDate 2016-06-20
     * @return String
     */
    public void closeDB()
    {
        if(mDataBase == null)
        {
            return;
        }

        mDataBase.close();

        mDataBase = null;
    }

    private static class ImageDBHelper extends SQLiteOpenHelper
    {
        private Context mContext;
        //
        private static final int KDBVersion = 1;


        //DB
        private static final String KDBNameStr = "image.db";

        //Table
        public static final String KTableImage = "imagetable";

        //Columns
        public static final String KImageID = "imageid";
        public static final String KImageDataType = "imagedatatype";
        public static final String KOnlineResURL = "onlineresurl";
        public static final String KOnlineResLocalPath = "onlinereslocalpath";
        public static final String KOnlineResRequestHeaders = "onlineresrequestheaders";
        public static final String KResWidth = "onlinereswith";
        public static final String ResHeight = "onlineresheight";
        public static final String KIsOnlineResCached = "isonlinerescached";
        public static final String KCacheStatus = "cachestatus"; // -1下载失败，0 未下载，1下载中，2已下载

//        /**
//         * @description decode string to string map
//         * @author yu.xiao
//         * @createDate 2016-06-20
//         * @param context
//         * @param name
//         * @param factory
//         * @param version
//         * @param errorHandler
//         * @return
//         */
//	    public ImageDBHelper(
//	    		Context context,
//	    		String name,
//	    		CursorFactory factory,
//	            int version,
//	            DatabaseErrorHandler errorHandler)
//	    {
//	        super(context, name, factory, version, errorHandler);
//
//	        if(context != null)
//	        {
//	        	mContext = context;
//	        }
//	    }

        /**
         * @description constructor
         * @author yu.xiao
         * @createDate 2016-06-20
         * @param context
         * @param name
         * @param factory
         * @param version
         * @return
         */
        public ImageDBHelper(
                Context context,
                String name,
                SQLiteDatabase.CursorFactory factory,
                int version)
        {
            super(context, name, factory, version);

            if(context != null)
            {
                mContext = context.getApplicationContext();
            }
        }

        /**
         * @description constructor
         * @author yu.xiao
         * @createDate 2016-06-20
         * @param context
         * @return
         */
        public ImageDBHelper(Context context)
        {
            super(context, KDBNameStr, null, KDBVersion);

            if(context != null)
            {
                mContext = context.getApplicationContext();
            }
        }

        @Override
        /**
         * @description override function
         * @author yu.xiao
         * @createDate 2016-06-20
         * @param db
         * @return
         */
        public void onCreate(SQLiteDatabase db)
        {
            checkDBTable(db);
        }


        /**
         * @description check and create database table
         * @author yu.xiao
         * @createDate 2016-06-20
         * @param db
         * @return
         */
        private void checkDBTable(SQLiteDatabase db)
        {
            if(db == null)
            {
                return;
            }

            Cursor cursor = null;

            try {
                cursor =
                        db.rawQuery(
                                "SELECT name FROM sqlite_master WHERE type='table';",
                                null);

                boolean isImageTableExisted = false;

                if(cursor != null)
                {
                    while(cursor.moveToNext())
                    {
                        String tableNameStr = cursor.getString(0);

                        if(KTableImage.compareTo(tableNameStr) == 0)
                        {
                            isImageTableExisted = true;
                        }

                        if(isImageTableExisted)
                        {
                            break;
                        }
                    }
                }

                if(!isImageTableExisted)
                {
                    StringBuffer tableCreateSB = new StringBuffer();

                    tableCreateSB.append("CREATE TABLE " + KTableImage + " (");
                    tableCreateSB.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,");
                    tableCreateSB.append("[" + KImageID + "] CHAR(17),");
                    tableCreateSB.append("[" + KImageDataType + "] INTEGER,");
                    tableCreateSB.append("[" + KOnlineResURL + "] TEXT,");
                    tableCreateSB.append("[" + KOnlineResLocalPath + "] TEXT,");
                    tableCreateSB.append("[" + KOnlineResRequestHeaders + "] TEXT,");
                    tableCreateSB.append("[" + KResWidth + "] INTEGER,");
                    tableCreateSB.append("[" + ResHeight + "] INTEGER,");
                    tableCreateSB.append("[" + KCacheStatus + "] INTEGER,");
                    tableCreateSB.append("[" + KIsOnlineResCached + "] INTEGER);");
                    db.execSQL(tableCreateSB.toString());
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(cursor != null) {
                    cursor.close();
                }
            }
        }

        @Override
        /**
         * @description open override function
         * @author yu.xiao
         * @createDate 2016-06-20
         * @param db
         * @return
         */
        public void onOpen(SQLiteDatabase db)
        {
        }

        @Override
        /**
         * @description upgrade override function
         * @author yu.xiao
         * @createDate 2016-06-20
         * @param db
         * @param oldVersion
         * @param newVersion
         * @return
         */
        public void onUpgrade(
                SQLiteDatabase db,
                int oldVersion,
                int newVersion)
        {
            if(oldVersion != newVersion) {
                try {
                    String dropImageSql ="DROP TABLE IF EXISTS " + KTableImage;
                    db.execSQL(dropImageSql);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
