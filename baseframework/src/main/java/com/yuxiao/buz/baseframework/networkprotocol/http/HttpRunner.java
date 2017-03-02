package com.yuxiao.buz.baseframework.networkprotocol.http;

import android.util.Log;

import com.yuxiao.buz.baseframework.networkprotocol.http.protocol.OnDownloadCompleteListener;
import com.yuxiao.buz.baseframework.networkprotocol.http.protocol.OnDownloadProgressListener;
import com.yuxiao.buz.baseframework.networkprotocol.http.protocol.OnExceptionListener;
import com.yuxiao.buz.baseframework.networkprotocol.http.protocol.OnReceiveResponseListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

public class HttpRunner {
    private HttpRunner(){super();}

    private static boolean mIsDebugMode;
    private static final String KHttpGet = "GET";
    private static final String KHttpPost = "POST";
    private static final String KHttpRangeFormat = "bytes=%d-";
    private static final String KHttpEncodingKey = "Accept-Encoding";
    private static final String KHttpCharsetKey = "Accept-Charset";
    private static final String KHttpConnectionKey = "connection";
    private static final String KHttpRangeKey = "Range";
    private static final String KHttpEncoding = "gzip,deflate";
    private static final String KGZIPEncoding = "gzip";
    private static final String KHttpCharset = "utf-8";
    private static final int KHttpConnectionTimeout = 15000;
    private static final int KHttpReadTimeout = 0;
    private static final int KHttpDataChunkSize = 1024 * 64;
    private static final long KIOStreamRWTimeInterval = 16;

    public enum EHttpState
    {
        EParaIllegal,
        EPathCreateFail,
        ECachePathExist,
        EDataTransferInterrupted,
        EDataTransferManualInterrupted,
        EHttpException,
        EHttpResponseOK,
        EHttpDataTransfering,
        EDownloadSuccess
    }

    private enum EURLType
    {
        EInvalid,
        EHTTP,
        EHTTPS
    }



    public interface OnUploadProgressListener
    {
        void onUploadProgress(
                String url,
                HashMap<String, String> httpHeaders,
                HashMap<String, String> parameters,
                boolean isRequestWithPost,
                Object extra,
                String uploadFilePath,
                long totalLength,
                long loadedLength);
    }


    public interface OnUploadCompleteListener
    {
        void onUploadCompleted(
                String url,
                HashMap<String, String> httpHeaders,
                HashMap<String, String> parameters,
                boolean isRequestWithPost,
                Object extra,
                String uploadFilePath);
    }


    public static class ConnectionInterruptSignal
    {
        public boolean isNeedInterrupt;
        public ConnectionInterruptSignal() {
            isNeedInterrupt = false;
        }
        public ConnectionInterruptSignal(Boolean b) {
            isNeedInterrupt = b;
        }
    }

    public static void doGetHttpDownloadRequest(final String urlStr,
                                                final HashMap<String, String> httpHeaders,
                                                final String savePath,
                                                final long startOffset,
                                                final Object extra,
                                                final ConnectionInterruptSignal interruptSignal,
                                                final OnReceiveResponseListener onResponseListener,
                                                final OnDownloadProgressListener onDownloadProgressListener,
                                                final OnDownloadCompleteListener onDownloadCompleteListener,
                                                final OnExceptionListener onExceptionListener) {
        if(interruptSignal != null &&
                interruptSignal.isNeedInterrupt)
        {
            if(onExceptionListener != null)
            {
                onExceptionListener.onHttpException(
                        urlStr,
                        httpHeaders,
                        null,
                        false,
                        extra,
                        EHttpState.EDataTransferManualInterrupted,
                        null);
            }

            return;
        }

        EURLType urlType = getURLType(urlStr);

        if(urlType == null ||
                urlType == EURLType.EInvalid)
        {
            if(onExceptionListener != null)
            {
                onExceptionListener.onHttpException(
                        urlStr,
                        httpHeaders,
                        null,
                        false,
                        extra,
                        EHttpState.EParaIllegal,
                        null);
            }

            return;
        }

        if(savePath == null)
        {
            if(onExceptionListener != null)
            {
                onExceptionListener.onHttpException(
                        urlStr,
                        httpHeaders,
                        null,
                        false,
                        extra,
                        EHttpState.EParaIllegal,
                        null);
            }

            return;
        }

        File judgeFile = new File(savePath);

        if(judgeFile.exists() &&
                judgeFile.isFile())
        {
            judgeFile.delete();
        }

        judgeFile = judgeFile.getParentFile();

        if(!judgeFile.exists() ||
                !judgeFile.isDirectory())
        {
            boolean result = judgeFile.mkdirs();

            if(!result)
            {
                if(onExceptionListener != null)
                {
                    onExceptionListener.onHttpException(
                            urlStr,
                            httpHeaders,
                            null,
                            false,
                            extra,
                            EHttpState.EPathCreateFail,
                            null);
                }

                return;
            }
        }

        try
        {
            URL url = new URL(urlStr);
            URLConnection urlConnection = url.openConnection();

            if(urlConnection == null)
            {
                if(onExceptionListener != null)
                {
                    onExceptionListener.onHttpException(
                            urlStr,
                            httpHeaders,
                            null,
                            false,
                            extra,
                            EHttpState.EHttpException,
                            null);
                }

                return;
            }

            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(KHttpConnectionTimeout);
            urlConnection.setReadTimeout(KHttpReadTimeout);
            urlConnection.setRequestProperty(KHttpCharsetKey, KHttpCharset);
            urlConnection.setRequestProperty(KHttpEncodingKey, KHttpEncoding);

            String startLoadRange = String.format(KHttpRangeFormat, startOffset);
            urlConnection.setRequestProperty(KHttpRangeKey, startLoadRange);

            HttpURLConnection httpURLConnection = (HttpURLConnection)urlConnection;
            httpURLConnection.setRequestMethod(KHttpGet);

            HttpsURLConnection httpsURLConnection = null;

            if(urlType == EURLType.EHTTPS)
            {
                httpsURLConnection = (HttpsURLConnection)urlConnection;

                HttpsAdapter httpsAdapter = HttpsAdapter.getInstance();
                httpsAdapter.adaptHttpsURLConnect(httpsURLConnection);
            }

            if(httpHeaders != null &&
                    httpHeaders.size() > 0)
            {
                Set parameterSet = httpHeaders.entrySet();
                Iterator iterator = parameterSet.iterator();

                if(iterator != null)
                {
                    while(iterator.hasNext())
                    {
                        Map.Entry entry = (Map.Entry)iterator.next();

                        if(entry == null)
                        {
                            continue;
                        }

                        String key = (String)entry.getKey();
                        String value = (String)entry.getValue();

                        if(key == null ||
                                value == null ||
                                key.trim().length() <= 0)
                        {
                            continue;
                        }

                        httpURLConnection.addRequestProperty(key, value);
                    }
                }
            }

            if(interruptSignal != null &&
                    interruptSignal.isNeedInterrupt)
            {
                if(onExceptionListener != null)
                {
                    onExceptionListener.onHttpException(
                            urlStr,
                            httpHeaders,
                            null,
                            false,
                            extra,
                            EHttpState.EDataTransferManualInterrupted,
                            null);
                }

                return;
            }

            httpURLConnection.connect();

            int responseCode = httpURLConnection.getResponseCode();

            String response = String.valueOf(responseCode);

            if(mIsDebugMode)
            {
                Log.i("HttpHandler", response);
            }

            if(responseCode / 200 != 1)
            {
                httpURLConnection.disconnect();

                if(onExceptionListener != null)
                {
                    onExceptionListener.onHttpException(
                            urlStr,
                            httpHeaders,
                            null,
                            false,
                            extra,
                            EHttpState.EHttpException,
                            response);
                }

                return;
            }
            else
            {
                if(onResponseListener != null)
                {
                    onResponseListener.onReceiveResponse(
                            urlStr,
                            httpHeaders,
                            null,
                            false,
                            extra,
                            response);
                }
            }

            InputStream inputStream = httpURLConnection.getInputStream();

            if(inputStream == null)
            {
                httpURLConnection.disconnect();

                if(onExceptionListener != null)
                {
                    onExceptionListener.onHttpException(
                            urlStr,
                            httpHeaders,
                            null,
                            false,
                            extra,
                            EHttpState.EHttpException,
                            null);
                }

                return;
            }

            String encoding = httpURLConnection.getContentEncoding();

            if(encoding != null &&
                    encoding.toLowerCase().contains(KGZIPEncoding))
            {
                inputStream = new GZIPInputStream(inputStream);
            }

            long contentSize = httpURLConnection.getContentLength();
            long readLength = 0;
            byte[] readBytes = new byte[KHttpDataChunkSize];

            FileOutputStream fOS = new FileOutputStream(savePath, true);

            while(true)
            {
                if(interruptSignal != null &&
                        interruptSignal.isNeedInterrupt)
                {
                    inputStream.close();
                    fOS.close();
                    httpURLConnection.disconnect();

                    if(onExceptionListener != null)
                    {
                        onExceptionListener.onHttpException(
                                urlStr,
                                httpHeaders,
                                null,
                                false,
                                extra,
                                EHttpState.EDataTransferManualInterrupted,
                                null);
                    }

                    return;
                }

                int currentReadLength = inputStream.read(readBytes);

                if(currentReadLength <= 0)
                {
                    break;
                }

                fOS.write(readBytes, 0, currentReadLength);
                fOS.flush();

                readLength += currentReadLength;

                if(onDownloadProgressListener != null)
                {
                    onDownloadProgressListener.onDownloadProgress(
                            urlStr,
                            httpHeaders,
                            null,
                            false,
                            extra,
                            null,
                            savePath,
                            contentSize,
                            readLength);
                }

                try
                {
                    Thread.currentThread().sleep(KIOStreamRWTimeInterval);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

            fOS.close();
            inputStream.close();
            httpURLConnection.disconnect();

            if(onDownloadCompleteListener != null)
            {
                onDownloadCompleteListener.onDownloadComplete(
                        urlStr,
                        httpHeaders,
                        null,
                        false,
                        extra,
                        savePath);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();

            if(onExceptionListener != null)
            {
                onExceptionListener.onHttpException(
                        urlStr,
                        httpHeaders,
                        null,
                        false,
                        extra,
                        EHttpState.EHttpException,
                        e.getLocalizedMessage());
            }
        }
    }

    private static EURLType getURLType(String url)
    {
        if(url == null ||
                url.length() <= 0)
        {
            return EURLType.EInvalid;
        }

        String lowerCasedURL = url.toLowerCase();

        boolean result = false;

        if(lowerCasedURL.startsWith("http://"))
        {
            return EURLType.EHTTP;
        }
        else if(lowerCasedURL.startsWith("https://"))
        {
            return EURLType.EHTTPS;
        }

        return EURLType.EInvalid;
    }

}
