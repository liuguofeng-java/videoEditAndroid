package com.video.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.video.core.BaseActivity;
import com.video.databinding.ActivityMainBinding;
import com.video.utils.StatusBarUtil;

import java.io.File;
import java.util.List;

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
            videoEditBut();
        }
    }


    /**
     * 点击视频编辑按钮
     */
    private void videoEditBut() {
        // 申请权限
        XXPermissions.with(this)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        startActivity(MainActivity.this, EditVideoActivity.class);
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        if (doNotAskAgain) {
                            showToast("被永久拒绝授权，请手动授予读取外部存储权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(MainActivity.this, permissions);
                        } else {
                            showToast("获取读取外部存储权限失败");
                        }
                    }
                });
    }
}