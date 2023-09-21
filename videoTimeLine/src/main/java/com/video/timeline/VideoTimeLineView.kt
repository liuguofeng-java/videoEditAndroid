package com.video.timeline

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.video.timeline.bean.CompareVideoClip
import com.video.timeline.bean.TrackEnum
import com.video.timeline.bean.VideoClip
import com.video.timeline.listener.AreaChangeListener
import com.video.timeline.listener.OnFrameClickListener
import com.video.timeline.listener.SelectAreaMagnetOnChangeListener
import com.video.timeline.listener.VideoPlayerOperate
import com.video.timeline.widget.FrameRecyclerView
import com.video.timeline.widget.RulerView
import com.video.timeline.widget.SelectAreaView
import com.video.timeline.widget.TimeLineBaseValue
import com.video.timeline.widget.VideoFrameRecyclerView
import com.video.timeline.widget.AudioFrameRecyclerView
import com.video.timeline.widget.ZoomFrameLayout
import com.video.util.DensityUtil
import com.video.util.getScreenWidth
import com.video.videotimeline.R
import java.util.UUID

/**
 * 轨道view
 * @author liuguofeng
 * @data 2023-9-21
 */
class VideoTimeLineView : RelativeLayout {
    private lateinit var selectAreaView: SelectAreaView
    private lateinit var zoomFrameLayout: ZoomFrameLayout
    private lateinit var rulerView: RulerView
    lateinit var timeLineValue: TimeLineBaseValue
    private lateinit var lineFrame: View
    private val rvFrames = mutableListOf<FrameRecyclerView>()
    val videoLines: MutableList<MutableList<VideoClip>> = mutableListOf()

    // 区域更改时间
    var areaChangeListener: AreaChangeListener? = null

    // 轨道下标
    private var trackIndex: Int = 0

    constructor(context: Context) : super(context, null) {
        initAttrs(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
        initAttrs(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, def: Int) : super(context, attrs, def) {
        initAttrs(context, attrs, def)
    }

    private fun initAttrs(context: Context, attributeSet: AttributeSet?, defAttrStyle: Int) {
        timeLineValue = TimeLineBaseValue(context)
        val array =
            context.obtainStyledAttributes(
                attributeSet,
                R.styleable.VideoTimeLineView,
                defAttrStyle,
                0
            )
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.view_video_time_line, this, true)
        zoomFrameLayout = view.findViewById(R.id.zoomFrameLayout)
        selectAreaView = view.findViewById(R.id.selectAreaView)
        rulerView = view.findViewById(R.id.rulerView)
        lineFrame = view.findViewById(R.id.lineFrame)

        zoomFrameLayout.scaleEnable = true
        zoomFrameLayout.timeLineValue = timeLineValue
        zoomFrameLayout.dispatchTimeLineValue()
        zoomFrameLayout.dispatchScaleChange()
        array.recycle()
    }

    private val videoSelectAreaChangeListener by lazy {
        object : SelectAreaMagnetOnChangeListener(selectAreaView.context) {
            override val timeJumpOffset: Long
                get() = selectAreaView.eventHandle.timeJumpOffset

            override val timeLineValue = (this@VideoTimeLineView).timeLineValue

            var downStartAtMs: Long = 0L
            var downEndAtMs: Long = 0L
            var downSpeed: Float = 1f
            override fun onTouchDown() {
                isOperateAreaSelect = true
                val selectVideo = selectVideo ?: return

                //更新边缘，此处边缘不限
                startTimeEdge = 0
                endTimeEdge = Long.MAX_VALUE

                downStartAtMs = selectVideo.startAtMs
                downEndAtMs = selectVideo.endAtMs
            }

            override fun onTouchUp() {
                isOperateAreaSelect = false
                val selectVideo = selectVideo ?: return
                areaChangeListener?.onChange(selectVideo)
            }

            override fun onChange(
                startOffset: Long,
                endOffset: Long,
                fromUser: Boolean
            ): Boolean {
                if (filterOnChange(startOffset, endOffset)) {
                    return true
                }
                val selectVideo = selectVideo ?: return false
                if (startOffset != 0L) {
                    //    - 起始位置移动时，相对时间轴的开始位置其实是不变的，变的是当前选择视频的开始位置+长度 （此时因为总的时间轴变长，所以区域变化了）
                    val oldStartTime = selectVideo.startAtMs
                    selectVideo.startAtMs += (downSpeed * startOffset).toLong()
                    //起始位置 + 吸附产生的时间差
                    selectVideo.startAtMs += checkTimeJump(
                        selectAreaView.startTime,
                        startOffset < 0
                    ) - selectAreaView.startTime

                    if (selectVideo.startAtMs < 0) {
                        selectVideo.startAtMs = 0
                    }
                    if (selectVideo.startAtMs > selectVideo.endAtMs - timeLineValue.minClipTime) {
                        selectVideo.startAtMs = selectVideo.endAtMs - timeLineValue.minClipTime
                    }
                    selectAreaView.endTime =
                        selectAreaView.startTime + selectVideo.durationMs //这样是经过换算的
                    val realOffsetTime = selectVideo.startAtMs - oldStartTime
                    if (fromUser) { //光标位置反向移动，保持时间轴和手的相对位置
                        timeLineValue.time -= (realOffsetTime / downSpeed).toLong()
                        if (timeLineValue.time < 0) {
                            timeLineValue.time = 0
                        }
                    }
                    updateVideoClip()
                    return realOffsetTime != 0L
                } else if (endOffset != 0L) {
                    //   - 结束位置移动时，范围的起始位置也不变，结束位置会变。
                    val oldEndMs = selectVideo.endAtMs
                    selectVideo.endAtMs += (downSpeed * endOffset).toLong()
                    selectAreaView.endTime = selectAreaView.startTime + selectVideo.durationMs

                    selectVideo.endAtMs += checkTimeJump(
                        selectAreaView.endTime,
                        endOffset < 0
                    ) - selectAreaView.endTime
                    if (selectVideo.endAtMs < selectVideo.startAtMs + timeLineValue.minClipTime) {
                        selectVideo.endAtMs = selectVideo.startAtMs + timeLineValue.minClipTime
                    }
                    if (selectVideo.endAtMs > selectVideo.originalDurationMs) {
                        selectVideo.endAtMs = selectVideo.originalDurationMs
                    }
                    selectAreaView.endTime = selectAreaView.startTime + selectVideo.durationMs
                    val realOffsetTime = selectVideo.endAtMs - oldEndMs
                    if (!fromUser) {
                        //结束位置，如果是动画，光标需要跟着动画
                        timeLineValue.time += (realOffsetTime / downSpeed).toLong()
                        if (timeLineValue.time < 0) {
                            timeLineValue.time = 0
                        }
                    }
                    updateVideoClip()
                    return realOffsetTime != 0L
                }
                return false
            }
        }
    }

    /**
     * 更新全局的时间轴
     * @param fromUser 用户操作引起的，此时不更改缩放尺度
     */
    private fun updateTimeLineValue() {
        /**
        1、UI定一个默认初始长度（约一屏或一屏半），用户导入视频初始都伸缩为初始长度；初始精度根据初始长度和视频时长计算出来；
        2、若用户导入视频拉伸到最长时，总长度还短于初始长度，则原始视频最长能拉到多长就展示多长；
        3、最大精度：即拉伸到极限时，一帧时长暂定0.25秒；
         */
        timeLineValue.apply {
            val isFirst = duration == 0L
            duration = totalDurationMs
            if (time > duration) {
                time = duration
            }

            if (isFirst) {//首次
                resetStandPxInSecond()
            } else {
                fitScaleForScreen()
            }
            zoomFrameLayout.dispatchTimeLineValue()
            zoomFrameLayout.dispatchScaleChange()
        }
    }

    val totalDurationMs: Long //当前正在播放视频的总时长
        get() {
            if (videoLines.size == 0) return 0
            var result = 0L
            for (video in videoLines[0]) {
                result += video.durationMs
            }
            return result
        }

    /**
     * 更新视频的截取信息
     * update and dispatch
     * */
    private fun updateVideoClip() {
        updateTimeLineValue()
        for (rvFrame in rvFrames) {
            rvFrame.rebindFrameInfo()
        }
        rulerView.invalidate()
        selectAreaView.invalidate()
    }

    /**
     * 是否正在操作区域选择
     */
    private var isOperateAreaSelect = false

    private fun clearSelectVideoIfNeed() {
        if (selectVideo != null && !selectAreaView.timeInArea()
            && !isOperateAreaSelect //未操作区域选择时
        ) {
            selectVideo = null
        }
    }

    /** 选段 */
    var selectVideo: VideoClip? = null
        set(value) {
            field = value
            val rvFrame = rvFrames[trackIndex]
            val videos = videoLines[trackIndex]
            if (value == null) {
                //取消选中
                selectAreaView.visibility = View.GONE
            } else {
                //选中视频
                selectAreaView.startTime = 0
                selectAreaView.onChangeListener = videoSelectAreaChangeListener
                for ((index, item) in videos.withIndex()) {
                    if (item === value) {
                        selectAreaView.offsetStart = if (index > 0) {
                            rvFrame.halfDurationSpace
                        } else {
                            0
                        }
                        selectAreaView.offsetEnd = if (index < videos.size - 1) {
                            rvFrame.halfDurationSpace
                        } else {
                            0
                        }
                        break
                    }
                    selectAreaView.startTime += item.durationMs
                }
                selectAreaView.endTime = selectAreaView.startTime + value.durationMs
                selectAreaView.visibility = View.VISIBLE
            }
        }

    fun findVideoByX(type: TrackEnum, rvFrame: RecyclerView, x: Float): VideoClip? {
        if (type == TrackEnum.VIDEO_TRACK) {
            return (rvFrame as VideoFrameRecyclerView).findVideoByX(x)
        } else if (type == TrackEnum.AUDIO_TRACK) {
            return (rvFrame as AudioFrameRecyclerView).findVideoByX(x)
        }
        return null
    }


    /**
     * 添加视频
     * @param lineIndex 轨道下标
     * @param type 类型 1:视频,2:音频
     * @param path 视频路径
     * @param start 视频开始时间
     * @param end 视频结束时间
     * @param text 音频文本
     *
     */
    fun addVideo(
        lineIndex: Int,
        type: TrackEnum,
        duration: Long,
        path: String,
        start: Long,
        end: Long,
    ) {
        videoLines[lineIndex].add(
            VideoClip(
                UUID.randomUUID().toString(), type, path,
                duration, start, end, ""
            )
        )
    }

    /**
     * 添加视频
     * @param lineIndex 轨道下标
     * @param type 类型 1:视频,2:音频
     * @param path 视频路径
     * @param start 视频开始时间
     * @param end 视频结束时间
     * @param text 音频文本
     *
     */
    fun addVideo(
        lineIndex: Int,
        type: TrackEnum,
        duration: Long,
        path: String,
        start: Long,
        end: Long,
        content: String,
    ) {
        videoLines[lineIndex].add(
            VideoClip(
                UUID.randomUUID().toString(), type, path,
                duration, start, end, content
            )
        )
    }

    /**
     * 更新视频
     */
    fun updateVideos() {
        for (index in rvFrames.indices) {
            val rvFrame = rvFrames[index]
            rvFrame.videoData = videoLines[index]
            rvFrame.rebindFrameInfo()
        }
        for (index in videoLines.indices) {
            initClip(index)
        }
        updateTimeLineValue()
    }

    /**
     * 初始化对比片段
     */
    private fun initClip(trackIndex: Int) {
        var videoRealTime: Long = 0

        val list = mutableListOf<CompareVideoClip>()

        for (index in videoLines[trackIndex].indices) {
            val item = videoLines[trackIndex][index]
            var clipIndexTime: Long = 0
            for (clipTime in item.startAtMs - 1..item.endAtMs) {
                list.add(CompareVideoClip(index, videoRealTime, clipTime, clipIndexTime))
                videoRealTime++
                clipIndexTime++
            }
        }
    }

    /**
     * 滑动事件
     */
    fun timeChangeListener(timeChangeListener: VideoPlayerOperate) {
        zoomFrameLayout.timeChangeListener(timeChangeListener)
    }

    /**
     * 区域改变时间事件
     */
    fun areaChangeListener(areaChangeListener: AreaChangeListener) {
        this.areaChangeListener = areaChangeListener
    }


    /**
     * 更新进度
     * @param time 时间戳
     */
    fun updateTime(time: Long) {
        zoomFrameLayout.updateTime(time)
    }

    /**
     * 添加轨道
     * @param type 类型 1:视频,2:音频
     */
    fun addTrack(type: TrackEnum, isEdit: Boolean) {
        var rvFrame: RecyclerView? = null
        if (type == TrackEnum.VIDEO_TRACK) {
            rvFrame = VideoFrameRecyclerView(context, null, 0)
        } else if (type == TrackEnum.AUDIO_TRACK) {
            rvFrame = AudioFrameRecyclerView(context, null, 0)
        }
        zoomFrameLayout.addView(rvFrame as View)

        rvFrame.overScrollMode = View.OVER_SCROLL_NEVER
        rvFrame.clipToPadding = false

        val tIndex = rvFrames.size
        rvFrame.addOnItemTouchListener(object : OnFrameClickListener(rvFrame) {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                return true
            }

            override fun onLongClick(e: MotionEvent): Boolean {
                return false
            }

            override fun onClick(e: MotionEvent): Boolean {
                if (!isEdit) {
                    return false
                }
                //点击的位置
                findVideoByX(type, rvFrame, e.x)?.let {
                    if (findVideoByX(type, rvFrame, rvFrame.paddingLeft.toFloat()) == it) {
                        // 切换轨道
                        trackIndex = tIndex
                        val layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            DensityUtil.dip2px(context, 48F)
                        )
                        layoutParams.topMargin = (trackIndex + 1) * DensityUtil.dip2px(context, 20F + 52F)
                        selectAreaView.layoutParams = layoutParams

                        //已选中，切换状态
                        selectVideo = if (selectVideo == it) {
                            areaChangeListener?.onTouchDown(null)
                            null
                        } else {
                            areaChangeListener?.onTouchDown(it)
                            it
                        }
                    } else {
                        //移动用户点击的位置到中间
                        rvFrame.postDelayed(
                            {
                                if (selectVideo != null) {
                                    selectVideo = findVideoByX(type, rvFrame, e.x)
                                    areaChangeListener?.onTouchDown(it)
                                }
                                rvFrame.smoothScrollBy((e.x - rvFrame.paddingLeft).toInt(), 0)
                            },
                            100
                        )
                    }
                } ?: run {
                    selectVideo?.let { selectVideo = null }
                    return false
                }
                return true
            }
        })
        rvFrame.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    clearSelectVideoIfNeed()
                }
            }
        })

        rvFrames.add(rvFrame as FrameRecyclerView)
        selectAreaView.bringToFront()
        lineFrame.bringToFront()
        rulerView.bringToFront()
        videoLines.add(mutableListOf())
        //不是编辑轨道的话要显示边框
        if (!isEdit) rvFrames[tIndex].hasBorder = true

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rvFrame.layoutManager = layoutManager

        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            DensityUtil.dip2px(context, 48F)
        )
        layoutParams.topMargin = rvFrames.size * DensityUtil.dip2px(context, 20F + 52F)
        rvFrame.layoutParams = layoutParams
        val halfScreenWidth = rvFrame.context.getScreenWidth()
        rvFrame.setPadding(halfScreenWidth / 2, 0, halfScreenWidth / 2, 0)
    }
}