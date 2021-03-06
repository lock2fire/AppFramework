package com.yuxiao.buz.test;

import android.app.Application;

import com.yuxiao.buz.baseframework.common.RootPathFinder;
import com.yuxiao.buz.baseframework.imageloader.Cavalli;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RootPathFinder.init(this, PathConstant.PATHS);
        Cavalli.setSavePath(RootPathFinder.getOwnerPath(this)+PathConstant.PIC_);
        Cavalli.start(this);
    }
}
