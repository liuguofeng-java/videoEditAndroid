package com.video.activity;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.video.core.BaseActivity;
import com.video.databinding.ActivityMainBinding;
import com.video.utils.StatusBarUtil;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        StatusBarUtil.setStatusBarColor(this);

        binding.videoEdit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == binding.videoEdit.getId()) {
            startActivity(this, EditVideoActivity.class);
        }
    }
}