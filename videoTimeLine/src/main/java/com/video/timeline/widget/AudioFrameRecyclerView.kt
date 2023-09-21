package com.video.timeline.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.video.timeline.adapter.AudioFrameAdapter
import com.video.timeline.bean.VideoClip
import com.video.timeline.bean.VideoFrameData
import com.video.timeline.listener.OnFrameClickListener
import com.video.util.dp2px

/**
 * 帧列表
 * 单声音：没有间隔，直接按视频长度换算
 * 多声音，首尾两个 + 一半间隔，中间的 + 完整间隔
 *
 * @author SamWang
 * @date 2019-07-29
 */
class AudioFrameRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr), TimeLineBaseValue.TimeLineBaseView,FrameRecyclerView {
    /** 视频数据 */
    override var videoData: List<VideoClip>? = null

    /** 帧数据 */
    private val listData = mutableListOf<VideoFrameData>()
    private val decorationWidth by lazy(LazyThreadSafetyMode.NONE) { context.dp2px(2f).toInt() }
    override val halfDurationSpace = decorationWidth / 2
    private val voiceFrameItemDecoration: FrameItemDecoration

    init {
        adapter = AudioFrameAdapter(listData)
        voiceFrameItemDecoration = FrameItemDecoration(context)
        addItemDecoration(voiceFrameItemDecoration)

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (scrollState == SCROLL_STATE_IDLE) {
                    //过滤updateTime 导致的滚动
                    return
                }

                (parent as? ZoomFrameLayout)?.let { zoomLayout ->
                    if (zoomLayout.isScrollIng()) {
                        zoomLayout.flingAnimation.cancel()
                    }

                    getCurrentCursorTime()?.let {
                        zoomLayout.updateTimeByScroll(it)
                    }
                }


            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val timeLineValue = timeLineValue ?: return
                val timeChangeListener = (parent as? ZoomFrameLayout)?.timeChangeListener ?: return

                when (newState) {
                    SCROLL_STATE_DRAGGING -> timeChangeListener.startTrackingTouch()
                    SCROLL_STATE_IDLE -> {
                        timeChangeListener.stopTrackingTouch(timeLineValue.time)
                        if (needUpdateTimeWhenScrollEnd) {
                            updateTime()
                        }
                    }
                }
            }
        })
    }

    override var hasBorder: Boolean
        set(value) {
            voiceFrameItemDecoration.hasBorder = value
            invalidate()
        }
        get() = voiceFrameItemDecoration.hasBorder

    override var timeLineValue: TimeLineBaseValue? = null

    override fun scaleChange() {
        rebindFrameInfo()
    }

    /**
     * 重新绑定视频的帧信息
     * 间隔：前后间隔都当作自己一帧
     * */
    override fun rebindFrameInfo() {
        listData.clear()
        val timeLineValue = timeLineValue ?: return
        val videoData = videoData ?: return
        if (videoData.isEmpty()) {
            adapter?.notifyDataSetChanged()
            return
        }
        for (item in videoData) {
            var element: VideoFrameData?
            val durationPx = timeLineValue.time2px(item.durationMs).toInt()
            element = VideoFrameData(item, 0, 0, durationPx, true, true, left)
            listData.add(element)

        }
        adapter?.notifyDataSetChanged()
        updateTime()
    }

    private var needUpdateTimeWhenScrollEnd = false //标记一些更新时间时正在滚动，等滚动完成后重新校正
    override fun updateTime() {
        if (listData.isEmpty()) {
            return
        }

        if (scrollState == SCROLL_STATE_IDLE) {
            val timeLineValue = timeLineValue ?: return
            var position = listData.size - 1 //精度问题会导致遍历完了还没找到合适的Item,此时进度条一定是在最后一个item的最右边
            var offsetX = listData[listData.size - 1].frameWidth
            var offsetTime = 0L //偏移时间
            var lastFrame: VideoFrameData? = null //上一帧
            for ((index, item) in listData.withIndex()) {
                if (item.isLastItem || offsetTime + item.time >= timeLineValue.time) {
                    //光标在这个视频的的相对时间
                    offsetTime = timeLineValue.time - offsetTime
                    offsetTime = if (lastFrame == null) {
                        position = index
                        offsetTime - item.time
                    } else {
                        offsetTime - lastFrame.time
                    }

                    offsetX = timeLineValue.time2px(offsetTime).toInt()

                    break
                } else {
                    position = index
                    lastFrame = item
                    continue
                }
            }
            //offset 负就向左，
            (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, -offsetX)
            needUpdateTimeWhenScrollEnd = false
        } else {
            needUpdateTimeWhenScrollEnd = true
        }
    }

    private var frameClickListener: OnFrameClickListener? = null

    override fun addOnItemTouchListener(listener: OnItemTouchListener) {
        super.addOnItemTouchListener(listener)
        (listener as? OnFrameClickListener)?.let {
            frameClickListener = it
        }
    }

    var scaleGestureDetector: ScaleGestureDetector? = null

    private fun disableLongPress() {
        frameClickListener?.gestureDetector?.setIsLongpressEnabled(false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.pointerCount > 1) {
            // todo 找更合适的地方禁用长按
            disableLongPress()
        }

        if (scrollState != SCROLL_STATE_IDLE) {
            disableLongPress()
            return super.onTouchEvent(e)
        }

        if (scaleGestureDetector == null) {
            (parent as? ZoomFrameLayout)?.scaleGestureListener?.let {
                scaleGestureDetector =
                    ScaleGestureDetector(this@AudioFrameRecyclerView.context, it)
            }
        }

        scaleGestureDetector?.let {
            if (scrollState == SCROLL_STATE_IDLE && e.pointerCount > 1) {
                val scaleEvent = it.onTouchEvent(e)
                if (it.isInProgress) {
                    return@onTouchEvent scaleEvent
                }
            }
        }

        if (e.pointerCount > 1) {
            return true
        }

        return super.onTouchEvent(e)

    }

    /** 通过X坐标找到对应的视频 */
    fun findVideoByX(x: Float): VideoClip? {
        val child = findChildViewByX(x) ?: return null
        val position = getChildAdapterPosition(child)
        return listData.getOrNull(position)?.videoData
    }

    /**
     * 通过x坐标找到对应的view
     */
    private fun findChildViewByX(x: Float): View? {
        val findChildViewUnder = findChildViewUnder(x, height / 2f)
        findChildViewUnder?.let {
            return it
        }

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val position = getChildAdapterPosition(child)
            if (position !in 0 until listData.size) {
                continue
            }
            val item = listData[position]
            val left = if (item.isFirstItem && position > 0) {
                child.left - halfDurationSpace
            } else {
                child.left
            }

            val right = if (item.isLastItem && position < listData.size - 1) {
                child.right + halfDurationSpace
            } else {
                child.right
            }

            if (left <= x && x <= right) {
                return child
            }
        }

        return null
    }

    /**
     * 当前游标指定的View
     * 用时间去找，比用坐标找更精确！可以精确到1ms,坐标只能精确到1px
     */
    private fun getCurrentCursorView(): View? {
        return findChildViewByX(paddingLeft.toFloat())
    }

    private val cursorX
        get() = paddingLeft

    private fun isAtEnd(): Boolean {
        return if (listData.isNotEmpty()) {
            val lastVH = findViewHolderForAdapterPosition(listData.size - 1) ?: return false
            lastVH.itemView.right <= cursorX
        } else {
            false
        }
    }

    /**
     * 当前游标指定的时间
     */
    private fun getCurrentCursorTime(): Long? {
        val child = getCurrentCursorView() ?: return null
        val videoData = videoData ?: return null
        val timeLineValue = timeLineValue ?: return null
        layoutManager?.canScrollHorizontally()
        if (isAtEnd()) {
            return timeLineValue.duration
        }

        val position = getChildAdapterPosition(child)
        if (position in 0 until listData.size) {
            val item = listData[position]
            val indexVideo = videoData.indexOfFirst { it === item.videoData }

            var time = 0L
            for (i in 0 until indexVideo) {
                time += videoData[i].durationMs
            }

            time += item.time
            var itemWidth = item.frameWidth

            if (indexVideo > 0 && item.isFirstItem) {
                itemWidth += halfDurationSpace
            }
            if (indexVideo < videoData.size - 1 && item.isLastItem) {
                itemWidth -= halfDurationSpace
            }

            val offsetX = paddingLeft - child.left
            val offsetTime = timeLineValue.px2time(offsetX.toFloat())
            time += offsetTime
            return time
        }
        return null

    }

    /**
     * 获取当前光标的指定的视频的范围
     *  当前item,
     * */
    override fun getCurrentCursorVideoRect(rect: RectF) {

        val child = getCurrentCursorView() ?: return
        val videoData = videoData ?: return
        val timeLineValue = timeLineValue ?: return

        val position = getChildAdapterPosition(child)
        if (position in 0 until listData.size) {
            val item = listData[position]
            val indexVideo = videoData.indexOfFirst { it === item.videoData }

            var offset = 0f //手动计算偏移值，防止 timeLineValue.time2px(item.time) 有误差
            for (i in position - 1 downTo 0) {
                val itemCountWidth = listData[i]
                if (itemCountWidth.videoData !== item.videoData) {
                    break
                }
                offset += itemCountWidth.frameWidth
            }

            rect.top = child.top.toFloat()
            rect.bottom = child.bottom.toFloat()

            rect.left = child.left - offset //第一帧的左边

            rect.right = rect.left + timeLineValue.time2px(item.videoData.durationMs)

            if (indexVideo > 0) {
                rect.right -= halfDurationSpace
            }
            if (indexVideo < videoData.size - 1) {
                rect.right -= halfDurationSpace
            }

        }
    }


}