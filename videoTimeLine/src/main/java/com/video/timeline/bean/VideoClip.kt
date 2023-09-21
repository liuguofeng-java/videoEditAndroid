package com.video.timeline.bean

import java.io.Serializable


/**
 * 视频美化视频片段
 * @author WangYingHao
 * @since 2019-08-03
 */
data class VideoClip(
    var id: String, //唯一标识
    var type: TrackEnum, //类型 1:视频,2:音频
    var originalFilePath: String,//文件路径
    var originalDurationMs: Long = 0,//原始文件时长
    var startAtMs: Long = 0, //视频有效起始时刻
    var endAtMs: Long = 0,//视频有效结束时刻
    var text: String?//如果是音频音频显示的文字
) : Serializable {
    val durationMs: Long //视频有效播放时长，受节选、速度、转场吃掉时间影响
        get() {
            return endAtMs - startAtMs
        }
}