package com.yuxiao.buz.baseframework;

/**
 * @author yu.xiao
 * @version 1.0
 * @description
 * @createDate 2017年02月16日
 */
interface PathConstant {
    static String PIC_ = "pic/";
    static String DATA_ = "data/";
    static String VIDEO_ = "video/";
    static String VIDEO_SUB1 = VIDEO_+"sub1/";
    static String VIDEO_SUB1_SUBB1 = VIDEO_SUB1+"subb1";
    final static String[] PATHS = {PIC_, DATA_, VIDEO_, VIDEO_SUB1, VIDEO_SUB1_SUBB1};
}
