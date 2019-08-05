package com.example.zjyd;

import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Intent intent = getIntent();
        String machineID = intent.getStringExtra("ID");
        String machineName = "浙江易锻";
        int machineImageId = R.drawable.machine;
        Toolbar toolbar = findViewById(R.id.machine_toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        ImageView machineImageView = findViewById(R.id.machine_image_view);
        TextView machineContentText = findViewById(R.id.machine_content_text);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbarLayout.setTitle(machineName);
        Glide.with(this).load(machineImageId).into(machineImageView);
        machineContentText.setText(machineID);
    }

    /**
     * toolbar按钮的点击事件
     * @param item 所点击的按钮，通过getItemId()方法来匹配
     * @return 点击成功返回true，否则返回父类的方法
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
