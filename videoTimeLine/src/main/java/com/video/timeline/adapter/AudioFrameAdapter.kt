package com.video.timeline.adapter

import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.video.timeline.bean.VideoFrameData
import com.video.timeline.soundfile.AudioSoundFileData
import com.video.timeline.soundfile.SoundFile
import com.video.timeline.soundfile.WaveformView
import com.video.timeline.widget.RoundRectMask
import com.video.videotimeline.R


/**
 * 帧列表 adapter
 * @author liuguofeng
 * @date 2023-7-11 17:14
 */
class AudioFrameAdapter(data: MutableList<VideoFrameData>) :
    BaseQuickAdapter<VideoFrameData, BaseViewHolder>(
        R.layout.item_audio_frame, data
    ) {


    override fun convert(helper: BaseViewHolder, item: VideoFrameData) {
        val layoutParams = helper.itemView.layoutParams
        layoutParams.width = item.frameWidth

        val maskView = helper.getView<RoundRectMask>(R.id.mask)
        maskView.setCornerRadiusDp(4f)
        maskView.setCorners(true, true, true, true)
        val maskLayoutParams = maskView.layoutParams as FrameLayout.LayoutParams

        if (item.isFirstItem) {
            maskLayoutParams.gravity = Gravity.LEFT
        } else {
            maskLayoutParams.gravity = Gravity.RIGHT
        }
        val audioText = helper.getView<TextView>(R.id.audio_text)
        audioText.text = item.videoData.text

        // 波形
        val waveform = helper.getView<WaveformView>(R.id.waveform)
        val mLoadSoundFileThread: Thread = object : Thread() {
            override fun run() {
                try {
                    synchronized(AudioSoundFileData.soundFileData) {
                        var soundFile = AudioSoundFileData.soundFileData[item.videoData.id]
                        if (soundFile == null) {
                            soundFile = SoundFile.create(item.videoData.originalFilePath) { true }
                            AudioSoundFileData.soundFileData[item.videoData.id] = soundFile
                        }
                        waveform.setSoundFile(soundFile)
                        waveform.invalidate()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        mLoadSoundFileThread.start()

    }

}




