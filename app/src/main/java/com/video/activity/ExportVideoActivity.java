package com.video.activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.video.core.BaseActivity;
import com.video.databinding.ActivityExportVideoBinding;
import com.video.entity.VideoItem;
import com.video.utils.DateUtils;
import com.video.utils.FileUtils;
import com.video.utils.StatusBarUtil;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;

public class ExportVideoActivity extends BaseActivity {
    private ActivityExportVideoBinding binding;
    // 视频片段
    private List<VideoItem> list;
    // 导出文件目录
    private File filePath;
    // 是否要执行
    private boolean isExec = true;
    // 是否执行结束
    private boolean isEnd = false;
    private final RxFFmpegInvoke rxFFmpegInvoke = RxFFmpegInvoke.getInstance();
    // 已剪辑视频路径
    private final List<String> videoPaths = new ArrayList<>();
    // 阻塞进程
    private CountDownLatch countDownLatch;

    // 导出文件名称
    String videoName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".mp4";

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityExportVideoBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        StatusBarUtil.setStatusBarColor(this);

        int statusBarHeight = StatusBarUtil.getStatusBarHeight(this);
        binding.getRoot().setPadding(0, statusBarHeight, 0, 0);

        list = (List<VideoItem>) getIntent().getSerializableExtra("videoItem");
        filePath = new File(this.getFilesDir(), "export_videos");
        FileUtils.deleteFolder(filePath.getPath());
        filePath.mkdir();
        binding.progressExport.setVideo(list.get(0).getPath());

        // 设置导航头
        initHead();
        // 导出
        new Thread(this::exportVideo).start();
    }

    /**
     * 导出视频
     */
    @SuppressLint("SetTextI18n")
    private void exportVideo() {
        while (isExec) {
            // 剪辑片段
            if (videoPaths.size() < list.size()) {
                Log.d("TAG", "exportVideo: " + videoPaths.size());
                String outPath = filePath.getPath() + File.separator + UUID.randomUUID() + ".mp4";
                VideoItem videoItem = list.get(videoPaths.size());
                String startTimeStr = DateUtils.timeToStr("HH:mm:ss.SSS", videoItem.getStartTime());
                String endTimeStr = DateUtils.timeToStr("HH:mm:ss.SSS", videoItem.getEndTime());
                // 是否存在声音
                String mute = "";
                if (videoItem.getIsVoice() == 0) {
                    mute = " -af volume=0";
                }
                String text = String.format("ffmpeg -ss %s -to %s -i %s -c:v libx264 -crf 30%s %s",
                        startTimeStr, endTimeStr, videoItem.getPath(), mute, outPath);
                execFFmpeg(text);
                videoPaths.add(outPath);
            } else if (videoPaths.size() == list.size() && !isEnd) {
                // 合并视频片段
                isEnd = true;
                StringBuilder str = new StringBuilder();
                videoPaths.forEach(item -> str.append("file '").append(item).append("'\n"));
                String txtPath = filePath.getPath() + File.separator + "index.txt";
                try {
                    FileUtils.saveTxt(txtPath, str.toString());
                } catch (IOException e) {
                    showToast("合并多个MP4失败");
                    return;
                }
                String text = String.format("ffmpeg -f concat -safe 0 -i %s -acodec aac -b:a 128K -f mp4 -movflags faststart -y %s",
                        txtPath, filePath.getPath() + File.separator + videoName);
                execFFmpeg(text);
            } else {
                // 转移视频到外部目录
                File outPath = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "videos");
                if (!outPath.exists()) outPath.mkdirs();
                String exportVideo = outPath + File.separator + videoName;
                try {
                    FileUtils.copyFile(new File(filePath.getPath() + File.separator + videoName), new File(exportVideo));
                } catch (IOException e) {
                    showToast("转移文件失败！" + e.getMessage());
                    Log.d("TAG", "转移文件失败！: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
                ContentResolver localContentResolver = this.getContentResolver();
                ContentValues localContentValues = getVideoContentValues(new File(exportVideo), System.currentTimeMillis());
                localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);
                showToast("保存到相册成功，路径为" + exportVideo);
                runOnUiThread(() -> {
                    binding.progressExport.setProgress(100);
                    binding.exportMsg.setText("保存到相册成功，路径为" + exportVideo);
                });
                return;
            }
            runOnUiThread(() -> {
                float progress = ((videoPaths.size() + 0.0f) / (list.size() + 1)) * 100;
                binding.progressExport.setProgress((int) progress);
            });
        }
    }

    public static ContentValues getVideoContentValues(File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", paramFile.getName());
        localContentValues.put("_display_name", paramFile.getName());
        localContentValues.put("mime_type", "video/3gp");
        localContentValues.put("datetaken", paramLong);
        localContentValues.put("date_modified", paramLong);
        localContentValues.put("date_added", paramLong);
        localContentValues.put("_data", paramFile.getAbsolutePath());
        localContentValues.put("_size", paramFile.length());
        return localContentValues;
    }

    /**
     * 执行ffmpeg
     *
     * @param text ffmpeg命令
     */
    private void execFFmpeg(String text) {
        String[] commands = text.split(" ");
        FFmpegSubscriber ffmpegSubscriber = new FFmpegSubscriber(this);
        rxFFmpegInvoke.setDebug(true);
        rxFFmpegInvoke.runCommandRxJava(commands)
                .subscribe(ffmpegSubscriber);
        countDownLatch = new CountDownLatch(1);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置导航头
     */
    private void initHead() {
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
            exit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认要退出导出吗?");
        String[] items = {"确定", "取消"};
        builder.setSingleChoiceItems(items, 0, (dialog, which) -> {
            isExec = false;
            if (rxFFmpegInvoke != null)
                rxFFmpegInvoke.exit();
            finish();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * ffmpeg 回调
     */
    static class FFmpegSubscriber extends RxFFmpegSubscriber {

        private final WeakReference<ExportVideoActivity> mWeakReference;

        public FFmpegSubscriber(ExportVideoActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onFinish() {
            final ExportVideoActivity activity = mWeakReference.get();
            activity.countDownLatch.countDown();
        }

        @Override
        public void onProgress(int progress, long progressTime) {
        }

        @Override
        public void onCancel() {
            mWeakReference.get().showToast("视频转文件失败！");
            mWeakReference.get().finish();
        }

        @Override
        public void onError(String message) {
            final ExportVideoActivity activity = mWeakReference.get();
            if (activity != null) {
                mWeakReference.get().showToast("视频转文件失败！");
                activity.finish();
            }
        }
    }

}
