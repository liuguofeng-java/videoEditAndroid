package com.video.entity;

import android.graphics.Bitmap;

/**
 * 视频信息
 * @author liuguofeng
 * @data 2023-9-21
 */
public class VideoInfo {
    // 视频路径
    private String path;

    // 视频长度
    private long durationMs;

    // 视频缩略图
    private Bitmap bitmap;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
