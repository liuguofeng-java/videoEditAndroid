package com.video.entity;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * 片段item
 *
 * @author liuguofeng
 * @data 2023-9-21
 */
public class VideoItem implements Serializable {

    /**
     * 视频路径
     */
    private String path;

    /**
     * 开始时间
     */
    private Long startTime;


    /**
     * 结束事件
     */
    private Long endTime;


    /**
     * 是否存在声音1:存在,0:不存在
     */
    private Integer isVoice;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Integer getIsVoice() {
        return isVoice;
    }

    public void setIsVoice(Integer isVoice) {
        this.isVoice = isVoice;
    }
}
