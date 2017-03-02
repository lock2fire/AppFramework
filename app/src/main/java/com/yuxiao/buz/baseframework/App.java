package com.yuxiao.buz.baseframework;

import android.app.Application;

import com.yuxiao.buz.baseframework.common.RootPathFinder;
import com.yuxiao.buz.baseframework.imageloader.Cavalli;

/**
 * @author yu.xiao
 * @version 1.0
 * @description
 * @createDate 2017年02月16日
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RootPathFinder.init(this, PathConstant.PATHS);
        Cavalli.setSavePath(RootPathFinder.getRootPath(this)+PathConstant.PIC_);
        Cavalli.start(this);
    }
}
