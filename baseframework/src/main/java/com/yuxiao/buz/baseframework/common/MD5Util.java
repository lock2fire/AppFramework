package com.yuxiao.buz.baseframework.common;

import java.security.MessageDigest;

public final class MD5Util {
    private static final char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private MD5Util() {
    }

    public static String byteArrayToHexString(byte[] md) {
        int j = md.length;
        char[] str = new char[j * 2];
        int k = 0;

        for(int i = 0; i < j; ++i) {
            byte byte0 = md[i];
            str[k++] = hexDigits[byte0 >>> 4 & 15];
            str[k++] = hexDigits[byte0 & 15];
        }

        return new String(str);
    }

    public static String MD5(String s) {
        try {
            byte[] e = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(e);
            byte[] md = mdInst.digest();
            String str = byteArrayToHexString(md);
            return str;
        } catch (Exception var5) {
            var5.printStackTrace();
            return "";
        }
    }
}
