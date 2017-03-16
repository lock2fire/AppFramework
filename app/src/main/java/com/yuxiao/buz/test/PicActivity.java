package com.yuxiao.buz.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.yuxiao.buz.test.R;
import com.yuxiao.buz.baseframework.imageloader.Cavalli;

import java.util.ArrayList;
import java.util.HashMap;

public class PicActivity extends FragmentActivity {

    static final String[] PICS = {
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882073&di=03d96ecb9f5be1eade4b768164f67447&imgtype=0&src=http%3A%2F%2Ftupian.enterdesk.com%2F2013%2Fmxy%2F12%2F11%2F4%2F3.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882235&di=29be370d3844eb566e72b81733492b30&imgtype=0&src=http%3A%2F%2Fimg05.tooopen.com%2Fimages%2F20140430%2Fsy_60177588239.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882233&di=9780bf83ff7ec03faa8d833f8818fb77&imgtype=0&src=http%3A%2F%2Fimg.tuku.cn%2Ffile_big%2F201502%2F0e93d8ab02314174a933b5f00438d357.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882233&di=b48af5e1caa0f6deb610076c8346d912&imgtype=0&src=http%3A%2F%2Fpic1.5442.com%3A82%2F2015%2F0409%2F01%2F15.jpg%2521960.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882232&di=d49af47b8184eb3903b1c1591e5786da&imgtype=0&src=http%3A%2F%2Fattach.bbs.miui.com%2Fforum%2F201702%2F24%2F173348egsu8qfurbq77rst.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882231&di=4de0bb9ed576fc7415f3da18b51422bd&imgtype=0&src=http%3A%2F%2Fpic.58pic.com%2F58pic%2F17%2F14%2F25%2F43Y58PICfJB_1024.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882231&di=11dbb9a43bae8f8a0666ef83f2169c39&imgtype=0&src=http%3A%2F%2Fpic.58pic.com%2F58pic%2F16%2F69%2F38%2F42v58PICzEP_1024.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882230&di=6a05968eb245766a7678da3c25660760&imgtype=0&src=http%3A%2F%2Ftupian.enterdesk.com%2F2013%2Fmxy%2F07%2F0715%2F1%2F3.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882230&di=25152e9b5d07d7cac35bab005ffca18c&imgtype=0&src=http%3A%2F%2Fdesk.fd.zol-img.com.cn%2Ft_s960x600c5%2Fg5%2FM00%2F00%2F0A%2FChMkJ1cpupiIW7yaABC-KDRTyM8AARBAQNvQmYAEL5A375.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882230&di=d1c56af86b82c64e86bb641262f265cb&imgtype=0&src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2Fc%2F57849d650ef11.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882229&di=908b9850ac52402ff352cf6d73371fcf&imgtype=0&src=http%3A%2F%2Fimg3.duitang.com%2Fuploads%2Fitem%2F201510%2F10%2F20151010211325_ZdA4R.jpeg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882229&di=71512750220843d9af7afc9958fa2f63&imgtype=0&src=http%3A%2F%2Fpic1.5442.com%2F2015%2F0710%2F12%2F02.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882228&di=97cc424f48af1c8274c2458c94533c40&imgtype=0&src=http%3A%2F%2Fwww.bz55.com%2Fuploads%2Fallimg%2F150604%2F139-150604162F5.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488368882228&di=0faec98c7fdbee2f81391ca23568575f&imgtype=0&src=http%3A%2F%2Fattach.bbs.miui.com%2Fforum%2F201702%2F28%2F192001z0otcrnt0k04zk4z.jpg"
    };

    ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);
        listView = (ListView) findViewById(R.id.listview);
        ArrayList<HashMap<String, Object>> pics = new ArrayList<>();
        for (int i = 0; i < PICS.length; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("picurl", PICS[i]);
            pics.add(map);
        }

        SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                Cavalli.getOnlineImg((String)data, (ImageView)view, 500, 500);
                return true;
            }
        };

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, pics, R.layout.item_pic,
                new String[]{"picurl"},
                new int[]{R.id.iv});

        simpleAdapter.setViewBinder(viewBinder);

        listView.setAdapter(simpleAdapter);
    }
}
