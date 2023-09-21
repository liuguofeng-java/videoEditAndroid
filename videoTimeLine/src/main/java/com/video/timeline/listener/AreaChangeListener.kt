package com.video.timeline.listener

import com.video.timeline.bean.VideoClip


/**
 * 区域更改事件
 * @author liuguofeng
 * @date 2023-7-31
 */
interface AreaChangeListener {

    // 选中区域
    fun onTouchDown(videoClip: VideoClip?)

    // 改变区域
    fun onChange(videoClip: VideoClip)
}