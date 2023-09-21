package com.video.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.video.R;
import com.video.adapter.SelectVideoAdapter;
import com.video.core.BaseActivity;
import com.video.databinding.ActivitySelectVideoBinding;
import com.video.entity.VideoInfo;
import com.video.utils.StatusBarUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SelectVideoActivity extends BaseActivity {

    private ActivitySelectVideoBinding binding;

    // 选择的视频信息
    private final ArrayList<String> selectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectVideoBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        // 初始化头部
        initHead();

        // 初始化视频信息
        initVideoInfo();

        binding.submit.setOnClickListener(view -> {
            if (selectList.size() == 0) {
                showToast("至少选择一个视频！");
                return;
            }

            Intent intent = new Intent();
            intent.putStringArrayListExtra("selectList", selectList);
            setResult(100, intent);
            finish();
        });
    }

    /**
     * 初始化视频信息
     */
    @SuppressLint("SetTextI18n")
    private void initVideoInfo() {
        // 获取视频信息
        List<VideoInfo> list = getVideoFromSDCard(this);
        for (VideoInfo videoInfo : list) {
            videoInfo.setBitmap(getVideoThumbnail(videoInfo.getPath()));
        }

        SelectVideoAdapter adapter = new SelectVideoAdapter();
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        binding.listView.setLayoutManager(layoutManager);

        adapter.add(list);
        binding.listView.setAdapter(adapter);

        // 选择事件
        adapter.setOnItemClickListener((position, isCheck) -> {
            VideoInfo videoInfo = list.get(position);
            if (isCheck) {
                selectList.add(videoInfo.getPath());
            } else {
                selectList.remove(videoInfo.getPath());
            }
            binding.text.setText("已选择" + selectList.size() + "个");
        });
    }

    /**
     * 设置导航头
     */
    private void initHead() {
        StatusBarUtil.setStatusBarColor(this);
        int statusBarHeight = StatusBarUtil.getStatusBarHeight(this);
        binding.root.setPadding(0, statusBarHeight, 0, 0);

        setSupportActionBar(binding.tbRegisterBack);
        //显示返回按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 监听标题栏按钮点击事件.
     *
     * @param item 按钮
     * @return 结果
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //返回按钮点击事件
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 获取视频缩略图
     *
     * @param filePath 视频路径
     */
    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap b = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            b = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
//                throw new RuntimeException(e);
            }
        }
        return b;
    }

    /**
     * 从本地得到所有的视频地址
     */
    private List<VideoInfo> getVideoFromSDCard(Context context) {
        List<VideoInfo> list = new ArrayList<>();
        String[] projection = new String[]{MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION};
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
            list.add(new VideoInfo() {{
                setPath(path);
                setDurationMs(duration);
            }});
        }
        cursor.close();
        return list;
    }
}