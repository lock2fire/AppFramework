package com.yuxiao.buz.baseframework;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.yuxiao.buz.baseframework.common.BitmapUtil;
import com.yuxiao.buz.baseframework.common.RootPathFinder;
import com.yuxiao.buz.baseframework.common.FileUtil;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Bitmap bitmap = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View btn1 = findViewById(R.id.add_abs_path);
        View btn2 = findViewById(R.id.add_child_path);
        View btn3 = findViewById(R.id.show);
        final ImageView iv = (ImageView) findViewById(R.id.iv);
//            bitmap = BitmapUtil.decodeBitmap(getAssets().open("2.jpg"), -1, -1);
        String path = RootPathFinder.getRootPath(getApplicationContext())+PathConstant.PIC_;
        bitmap = BitmapUtil.decodeBitmap(path+"4.jpg", -1, -1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = RootPathFinder.getRootPath(getApplicationContext())+PathConstant.PIC_;
                BitmapUtil.saveBitmap(bitmap, path, "love.jpg");
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = RootPathFinder.getRootPath(getApplicationContext())+PathConstant.PIC_;
                FileUtil.delFile(path, "love.jpg");
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                iv.setImageBitmap(bitmap);
                String path = RootPathFinder.getRootPath(getApplicationContext())+PathConstant.PIC_;
                Toast.makeText(getApplicationContext(), FileUtil.getFreeSize(path+"4.jpg")+"", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
