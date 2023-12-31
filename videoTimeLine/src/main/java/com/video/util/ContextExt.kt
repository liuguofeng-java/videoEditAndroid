package com.video.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Vibrator
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager

/**
 * dp转px
 *
 * @param context
 * @param dp
 * @return
 */
fun Context.dp2px(dp: Float): Float {
    return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp, resources.displayMetrics
    )
}

/**
 * sp转px
 *
 * @param context
 * @param sp
 * @return
 */
fun Context.sp2px(sp: Float): Float {
    return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp, resources.displayMetrics
    )
}

fun Context.getScreenWidth(): Int {
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val outMetrics = DisplayMetrics()
    wm.defaultDisplay.getMetrics(outMetrics)
    return outMetrics.widthPixels
}

fun Context.color(@ColorRes res: Int): Int {
    return ContextCompat.getColor(this, res)
}

/**
 * 小震一下
 */
fun Context.vibratorOneShot() {
    vibrator(10) //为啥是10，我看其它地方也都是10，统一体验哈哈（- -.）
}

@SuppressLint("MissingPermission")
fun Context.vibrator(milliseconds: Long){
//    VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
    (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(milliseconds) //deprecated但是推荐的接口api level 26
}



val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )
val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

@Deprecated("注意:文字大小统一要求使用dp", ReplaceWith("dp"))
val Float.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics
    )


@Deprecated("注意:文字大小统一要求使用dp", ReplaceWith("dp"))
val Int.sp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()




