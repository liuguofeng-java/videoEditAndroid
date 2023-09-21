package com.video.utils;

import android.media.MediaMetadataRetriever;

import java.util.Formatter;
import java.util.Locale;

public class VideoUtils {
    /**
     * 获取 视频 或 音频 时长
     *
     * @param path 视频 或 音频 文件路径
     * @return 时长 毫秒值
     */
    public static int getDuration(String path) {
        int duration = 0;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            if (path != null) {
                mmr.setDataSource(path);
            }
            String time = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            duration = Integer.parseInt(time);
        } catch (Exception ignored) {
        } finally {
            try {
                mmr.release();
            } catch (Exception ignored) {

            }
        }
        return duration;
    }

    /**
     *
     * @param timeMs
     * @return
     */
    public static String stringForTime(long timeMs) {
        Formatter formatter = new Formatter(new StringBuilder(), Locale.getDefault());
        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}
