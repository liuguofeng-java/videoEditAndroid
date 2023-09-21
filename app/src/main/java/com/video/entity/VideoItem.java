package com.video.entity;

import java.io.Serializable;

/**
 * 片段item
 * @author liuguofeng
 * @data 2023-9-21
 */
public class VideoItem implements Serializable {

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 视频开始时间
     */
    private String startTimeText;

    /**
     * 结束事件
     */
    private Long endTime;

    /**
     * 视频结束时间
     */
    private String endTimeText;

    /**
     * 是否存在声音1:存在,0:不存在
     */
    private Integer isVoice;

    /**
     * 排序
     */
    private Integer sortNo;

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getStartTimeText() {
        return startTimeText;
    }

    public void setStartTimeText(String startTimeText) {
        this.startTimeText = startTimeText;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getEndTimeText() {
        return endTimeText;
    }

    public void setEndTimeText(String endTimeText) {
        this.endTimeText = endTimeText;
    }

    public Integer getIsVoice() {
        return isVoice;
    }

    public void setIsVoice(Integer isVoice) {
        this.isVoice = isVoice;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}
