package com.example.zjyd;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.baidu.mapapi.SDKInitializer;
import com.example.zjyd.util.LogUtil;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            SDKInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            LogUtil.e(TAG, e.toString());
        }

        setContentView(R.layout.activity_map);
    }
}
