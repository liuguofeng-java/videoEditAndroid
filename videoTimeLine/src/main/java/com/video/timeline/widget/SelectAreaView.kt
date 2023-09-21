package com.video.timeline.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import androidx.core.content.ContextCompat
import com.video.timeline.widget.SelectAreaView.OnChangeListener.Companion.MODE_END
import com.video.timeline.widget.SelectAreaView.OnChangeListener.Companion.MODE_START
import com.video.timeline.widget.SelectAreaView.OnChangeListener.Companion.MODE_WHOLE
import com.video.videotimeline.R
import com.sam.video.util.SelectAreaEventHandle
import com.video.util.color
import com.video.util.dp2px
import kotlin.math.abs
import kotlin.math.ceil


/**
 * 区域选择view
 * 场景：
 * 1. 视频编辑，选择视频的范围
 * 2. 文字贴纸选择生效的时间范围: 文字贴纸的移动都直接作用于时间范围
 *
 *
 * 使用：
 * 切图资源高度为72dp，view高度也最好设置为72
 *
 * 在边缘停留会自动滚：
 *
 * @author SamWang
 * @date 2019-07-31
 */
class SelectAreaView @JvmOverloads constructor(
    context: Context, paramAttributeSet: AttributeSet? = null,
    paramInt: Int = 0
) : View(context, paramAttributeSet, paramInt),
    TimeLineBaseValue.TimeLineBaseView {

    /** 选择的时间范围 */
    var startTime = 0L
    var endTime = 0L

    /** 考虑视频间的间隔偏移 */
    var offsetStart = 0
    var offsetEnd = 0

    private val cursorWidth = context.dp2px(20f)
    private val selectAreaHeight = context.dp2px(48f)

    private val textMarginRight = context.dp2px(2f) //选区时间的右间距

    private val paintBg = Paint(Paint.ANTI_ALIAS_FLAG)

    //size: 20*48dp
    private val leftDrawable = context.resources.getDrawable(R.drawable.video_select)
    private val rightDrawable = context.resources.getDrawable(R.drawable.video_select)

    val eventHandle = SelectAreaEventHandle(context)
    var onChangeListener: OnChangeListener? = null
        set(value) {
            field = value
            eventHandle.onChangeListener = value
        }

    private val textY2Bottom: Float by lazy { //  文字Y坐标到底部的距离 ,字体的基线
        context.dp2px(24f) - abs(paintBg.ascent() + paintBg.descent()) / 2
    }

    init {
        paintBg.style = Paint.Style.FILL
        paintBg.textSize = context.dp2px(10f)
        paintBg.textAlign = Paint.Align.RIGHT
    }

    /** 基础数据 */
    override var timeLineValue: TimeLineBaseValue? = null
        set(value) {
            field = value
            eventHandle.timeLineValue = value
            invalidate()
        }

    /**
     * 是否在时间范围
     */
    fun timeInArea(): Boolean {
        val timeLineValue = timeLineValue ?: return false
        return timeLineValue.time in startTime..endTime
    }

    private var startTimeX = 0f
    private var endTimeX = 0f

    private fun getTime(): String {
        return String.format("%.1fs", (endTime - startTime) / 1000f)
    }

    /**
     * 全部移动模式
     */
    var wholeMoveMode = false

    override fun bringToFront() {
        super.bringToFront();
        invalidate()
    }

    //边框
    private val rect = RectF()
    private val colorStart = ContextCompat.getColor(context, R.color.video_edit_track_color)
    private val colorMiddle = ContextCompat.getColor(context, R.color.video_edit_track_color)
    private val colorEnd = ContextCompat.getColor(context, R.color.video_edit_track_color)
    private val shaderColors = intArrayOf(colorStart, colorMiddle, colorEnd)
    private val shaderPositions = floatArrayOf(0f, 0.39f, 1.0f)
    private val strokeWidth = context.dp2px(4f)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.video_edit_track_color)
        style = Paint.Style.STROKE
        strokeWidth = this@SelectAreaView.strokeWidth
    }



    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val timeLineValue = timeLineValue ?: return
        canvas.save()
        canvas.clipRect(paddingLeft, 0, width, height)
        val timeX = width / 2 //当前时间，中间那个标记线的位置

        val timeInPx = timeLineValue.time2px(timeLineValue.time)
        val alpha = if (wholeMoveMode) {
            128
        } else {
            255
        }
        leftDrawable.alpha = alpha
        rightDrawable.alpha = alpha

        val startTimeInPx = timeLineValue.time2px(startTime)
        startTimeX = ceil(timeX + offsetStart + startTimeInPx - timeInPx) //上取整损失一些精度，避免开始的游标和中间线对不上
        val endTimeInPx = timeLineValue.time2px(endTime)
        endTimeX = timeX - offsetEnd + endTimeInPx - timeInPx

        //中
        rect.left = startTimeX
        rect.top = (height - selectAreaHeight) / 2
        rect.right = endTimeX
        rect.bottom = (height + selectAreaHeight) / 2
        paint.shader = LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            shaderColors, shaderPositions, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, 0F, 0F, paint)

        //左
        leftDrawable.setBounds((startTimeX - cursorWidth).toInt(), 0, startTimeX.toInt(), height)
        leftDrawable.draw(canvas)

        //时间
        paintBg.color = ContextCompat.getColor(context, R.color.video_edit_track_color)
        val time = getTime()
        val textWidth = paintBg.measureText(time)
        if (textMarginRight + textWidth < endTimeX - startTimeX) {
            canvas.drawText(time, endTimeX - textMarginRight, height - textY2Bottom, paintBg)
        }

        // 右
        rightDrawable.setBounds(endTimeX.toInt(), 0, (endTimeX + cursorWidth).toInt(), height)
        rightDrawable.draw(canvas)

        canvas.restore()
    }


    private val gestureDetector: GestureDetector by lazy(LazyThreadSafetyMode.NONE) {
        GestureDetector(context, gestureListener).also {
            it.setIsLongpressEnabled(false) //自己识别长按
        }
    }

    private fun isEventIn(event: MotionEvent, @OnChangeListener.TouchMode mode: Int): Boolean {
        return when (mode) {
            MODE_START -> event.x in startTimeX - cursorWidth..startTimeX
            MODE_END -> event.x in endTimeX..endTimeX + cursorWidth
            MODE_WHOLE -> event.x in startTimeX - cursorWidth..endTimeX + cursorWidth
            else -> false
        }
    }

    /**
     * 点击监听
     */
    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(event: MotionEvent): Boolean {
            eventHandle.touchStartCursor = isEventIn(event, MODE_START)
            eventHandle.touchEndCursor =
                (!eventHandle.touchStartCursor) && isEventIn(event, MODE_END)
            eventHandle.removeLongPressEvent()

            val consume = if (eventHandle.startEndBothMoveEnable) {
                (event.x > startTimeX && event.x < endTimeX).also {
                    if (it) {
                        eventHandle.sendLongPressAtTime(event.downTime)
                    }
                }
            } else {
                false
            } || eventHandle.touchStartCursor || eventHandle.touchEndCursor

            onChangeListener?.apply {
                if (eventHandle.touchStartCursor) {
                    mode = MODE_START
                } else if (eventHandle.touchEndCursor) {
                    mode = MODE_END
                }
            }

            if (consume) {
                eventHandle.onChangeListener?.onTouchDown()
            }
            return consume
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {

            // 事件向上递传
            val eventDispatchUp = if (eventHandle.consumeEvent) {
                //较正结束点和对应点的真正偏移
                onChangeListener?.mode?.let { mode ->
                    if (!isEventIn(e2, mode) // 手指如果没在光标范围内 ,(e1是 down，没必要判断）
                        && isCloseToCursor(e2.x, distanceX, mode)// 则远离小耳朵一定要生效，逼近的可以吃掉
                    ) {
                        eventHandle.removeLongPressEvent()
                        eventHandle.autoHorizontalScrollAnimator?.cancel()
                        return true
                    }
                }

                !eventHandle.onScroll(e1, e2, distanceX, distanceY)
            } else {
                true
            }

            //滚动事件传递给上层
            if (eventDispatchUp) {
                eventHandle.removeLongPressEvent()
                // 需求事件不传递
                if (!eventHandle.consumeEvent) {
                    (parent as? ZoomFrameLayout)?.gestureListener?.onScroll(
                        e1,
                        e2,
                        distanceX,
                        distanceY
                    )
                }
            }

            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            eventHandle.removeLongPressEvent()
            if (!eventHandle.consumeEvent) {
                (parent as? ZoomFrameLayout)?.gestureListener?.onFling(e1, e2, velocityX, velocityY)
            }
            return super.onFling(e1, e2, velocityX, velocityY)

        }
    }

    /**
     * 光标是否更接近了
     */
    private fun isCloseToCursor(
        x: Float,
        distanceX: Float, // lastX - x
        @OnChangeListener.TouchMode mode: Int
    ): Boolean {
        val cursorX = when (mode) {
            MODE_START -> startTimeX
            MODE_END -> endTimeX
            else -> (startTimeX + endTimeX) / 2
        }

        return abs(x - cursorX) < abs(x + distanceX - cursorX)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = gestureDetector.onTouchEvent(event)
        eventHandle.onTouchEvent(event)
        return result
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        eventHandle.autoHorizontalScrollAnimator?.cancel()
    }

    override fun scaleChange() {
        eventHandle.timeJumpOffset = timeLineValue?.px2time(eventHandle.timeJumpOffsetPx) ?: 0
        invalidate()
    }

    override fun updateTime() {
        invalidate()
    }

    /**
     * 选择区域变化监听
     */
    interface OnChangeListener {

        @TouchMode
        var mode: Int

        /**
         * 按下选择区域
         */
        fun onTouchDown()

        /**
         * 开始时间变动的毫秒，结束时间变动的毫秒
         * @param startOffset 起始位置移动
         * @param endOffset 结束位置移动
         * @param fromUser true 用户滑动,false 动画自动滑动
         *
         * @return 返回这个事件是否生效
         */
        fun onChange(startOffset: Long, endOffset: Long, fromUser: Boolean): Boolean


        /**
         * 抬起选择区域
         */
        fun onTouchUp()

        /**
         * 整个区域移动
         */
        fun onMove(offset: Long, fromUser: Boolean): Boolean {
            return false
        }

        companion object {
            const val MODE_NONE = -1
            const val MODE_START = 1
            const val MODE_END = 2
            const val MODE_WHOLE = 3
        }

        @IntDef(MODE_NONE, MODE_START, MODE_END, MODE_WHOLE)
        @Retention(AnnotationRetention.SOURCE)
        annotation class TouchMode

    }

}