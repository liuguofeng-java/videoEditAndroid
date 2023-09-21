package com.video.timeline.listener

import com.video.timeline.widget.TimeChangeListener

/**
 * 视频美化首页 视频播放控件的操作接口
 * @author SamWang
 * @date 2019-07-24
 */
interface VideoPlayerOperate : TimeChangeListener {
    fun startTrackingTouch()

    fun stopTrackingTouch(ms: Long)
}