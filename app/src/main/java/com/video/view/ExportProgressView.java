package com.video.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;

import com.video.R;

import java.io.IOException;

/**
 * 视频导出
 *
 * @author liuguofeng
 * @date 2022/11/16 16:59
 */
public class ExportProgressView extends androidx.appcompat.widget.AppCompatImageView {

    private int mProgress;
    private final Paint mPaint;
    private final Paint mTextPaint;
    private final Path mPath;
    private final Path mCurrentPath;
    private static int PADDING = 10;
    private String videoPath;

    public ExportProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        PADDING *= (int) context.getResources().getDisplayMetrics().density;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setTextSize(64);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setStrokeWidth(3);
        mTextPaint.setColor(getResources().getColor(R.color.but_primary));

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getResources().getColor(R.color.but_primary));
        mPaint.setStrokeWidth(PADDING);
        mPath = new Path();
        mCurrentPath = new Path();

        setPadding(PADDING / 2, PADDING / 2, PADDING / 2, PADDING / 2);
    }

    public void setVideo(String videoPath) {
        this.videoPath = videoPath;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        mCurrentPath.reset();
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(videoPath);
        Bitmap bitmap = media.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        setImageBitmap(bitmap);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                media.close();
            }
        } catch (IOException e) {
            Log.d("TAG", "获取视频第一帕图片失败: " + e.getMessage());
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        super.onDraw(canvas);
        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.addRect(0, 0, width, height, Path.Direction.CCW);
        mPath.close();
        mPaint.setColor(getResources().getColor(R.color.text_color));
        canvas.drawPath(mPath, mPaint);

        mCurrentPath.reset();
        int mCurrentY;
        int mCurrentX = mCurrentY = 0;
        mCurrentPath.moveTo(mCurrentX, mCurrentY);
        int progress = mProgress;

        if (progress > 0) {
            mCurrentX = (int) (width * Math.min(1.f, progress / 25.f));
            mCurrentPath.lineTo(mCurrentX, 0);
        }
        progress -= 25;
        if (progress > 0) {
            mCurrentY = (int) (height * Math.min(1.f, progress / 25.f));
            mCurrentPath.lineTo(mCurrentX, mCurrentY);
        }
        progress -= 25;
        if (progress > 0) {
            mCurrentX -= (int) (width * Math.min(1.f, progress / 25.f));
            mCurrentPath.lineTo(mCurrentX, mCurrentY);
        }
        progress -= 25;
        if (progress > 0) {
            mCurrentY -= (int) (height * Math.min(1.f, progress / 25.f));
            mCurrentPath.lineTo(mCurrentX, mCurrentY);
        }
        mPaint.setColor(getResources().getColor(R.color.but_primary));
        canvas.drawPath(mCurrentPath, mPaint);

        canvas.drawText(mProgress + "%", width / 2, height / 2, mTextPaint);

    }
}