package com.yuxiao.buz.baseframework.networkprotocol.http;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsAdapter
{
    private static HttpsAdapter mInstance;

    private SSLContext mSSLContext;

    private HostnameVerifierExt mHostnameVerifierExt;

    private X509Certificate[] mAcceptedIssuers;

    private HttpsAdapter()
    {
        super();
    }

    public static  HttpsAdapter getInstance()
    {
        if(mInstance == null)
        {
            mInstance = new HttpsAdapter();
        }

        return mInstance;
    }

    public void adaptHttpsURLConnect(HttpsURLConnection connection)
    {
        if(connection == null)
        {
            return;
        }

        try
        {
            if(mSSLContext == null)
            {
                mSSLContext = SSLContext.getInstance("TLS");

                TrustManagerExt trustManager = new TrustManagerExt();

                TrustManager[] trustManagers = new TrustManager[]{trustManager};

                SecureRandom secureRandom = new SecureRandom();

                mHostnameVerifierExt = new HostnameVerifierExt();

                mSSLContext.init(
                        null,
                        trustManagers,
                        secureRandom);
            }

            if(mHostnameVerifierExt != null)
            {
                HttpsURLConnection.setDefaultHostnameVerifier(mHostnameVerifierExt);
            }

            if(mSSLContext != null)
            {
                HttpsURLConnection.setDefaultSSLSocketFactory(mSSLContext.getSocketFactory());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private class HostnameVerifierExt implements HostnameVerifier
    {
        @Override
        public boolean verify(String hostname, SSLSession session)
        {
            return true;
        }
    }

    private class TrustManagerExt implements X509TrustManager
    {
        @Override
        public void checkClientTrusted(
                X509Certificate[] chain,
                String authType) throws CertificateException
        {
        }

        @Override
        public void checkServerTrusted(
                X509Certificate[] chain,
                String authType) throws CertificateException
        {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return mAcceptedIssuers;
        }
    }
}
