package com.example.zjyd.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.zjyd.R;

import java.util.Objects;

public class InfoFragment extends Fragment {

    private static final String TAG = "InfoFragment";
    View view;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    ImageView machineImageView;
    TextView machineContentText;
    String machineName = "浙江易锻";
    int machineImageId = R.drawable.machine;
    String machineID = "This is machineID";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_info, container, false);
        toolbar = view.findViewById(R.id.machine_toolbar);
        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar);
        machineImageView = view.findViewById(R.id.machine_image_view);
        machineContentText = view.findViewById(R.id.machine_content_text);
        collapsingToolbarLayout.setTitle(machineName);
        Glide.with(this).load(machineImageId).into(machineImageView);
        machineContentText.setText(machineID);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
