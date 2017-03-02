package com.yuxiao.buz.baseframework;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

public class MyFragmentActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        final Fragment fragmentA = new FragmentA();
        final Fragment fragmentB = new FragmentB();
        Button change = (Button) findViewById(R.id.change);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentA).commitAllowingStateLoss();
//                getSupportFragmentManager().beginTransaction().show(fragmentB).commitAllowingStateLoss();
                getSupportFragmentManager().beginTransaction().add(R.id.container, fragmentA).addToBackStack(null).commitAllowingStateLoss();
            }
        });
        Button addB = (Button) findViewById(R.id.addB);
        addB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getSupportFragmentManager().beginTransaction().addToBackStack()
//                getSupportFragmentManager().beginTransaction().add(R.id.container, fragmentB).commitAllowingStateLoss();
//                getSupportFragmentManager().beginTransaction().hide(fragmentB).commitAllowingStateLoss();
            }
        });
//        getSupportFragmentManager().beginTransaction().add(R.id.container, fragmentA).commitAllowingStateLoss();
        getSupportFragmentManager().beginTransaction().add(R.id.container, fragmentA).add(R.id.container, fragmentB).addToBackStack(null).commitAllowingStateLoss();
//        getSupportFragmentManager().beginTransaction().add(R.id.container, fragmentA).replace(R.id.container, fragmentB).addToBackStack(null).commitAllowingStateLoss();
    }
}
