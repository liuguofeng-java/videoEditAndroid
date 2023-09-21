package com.video.timeline.widget

import android.graphics.RectF
import com.video.timeline.bean.VideoClip

interface FrameRecyclerView {

    fun rebindFrameInfo()
    fun getCurrentCursorVideoRect(rect: RectF)

    var hasBorder: Boolean
    val halfDurationSpace: Int
    var videoData: List<VideoClip>?

}