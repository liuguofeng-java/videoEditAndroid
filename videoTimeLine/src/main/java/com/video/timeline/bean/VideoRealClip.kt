package com.video.timeline.bean


/**
 * 视频真实片段
 * @author liuguofeng
 * @since 2023-5-26
 */
data class CompareVideoClip(
    var index: Int,//片段下表
    var realTime: Long = 0,//真实当前进度
    var clipTime: Long = 0,//片段当前进度
    var clipIndexTime: Long = 0,//片段当前进度(从0开始)
){


}