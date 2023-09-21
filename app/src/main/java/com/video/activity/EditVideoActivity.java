package com.video.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.video.R;
import com.video.adapter.TabButsAdapter;
import com.video.core.BaseActivity;
import com.video.databinding.ActivityEditVideoBinding;
import com.video.entity.VideoItem;
import com.video.entity.TabButsItem;
import com.video.timeline.bean.TrackEnum;
import com.video.timeline.bean.VideoClip;
import com.video.timeline.listener.AreaChangeListener;
import com.video.timeline.listener.VideoPlayerOperate;
import com.video.utils.AnimationUtils;
import com.video.utils.StatusBarUtil;
import com.video.utils.VideoUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EditVideoActivity extends BaseActivity implements View.OnClickListener, TabButsAdapter.OnItemClickListener {

    private ActivityEditVideoBinding binding;
    // 视频进度
    private final List<VideoItem> videoItems = new ArrayList<>();
    // 底部按钮Adapter
    private final TabButsAdapter adapter = new TabButsAdapter();
    // 播放器
    private ExoPlayer mVideo;
    // 声音大小
    private final float volume = 1.0f;
    //定时器
    private final Timer timer = new Timer();
    // 选择的片段
    private int checkedIndex = 0;
    // 底部按钮数据
    private List<TabButsItem> tabButsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditVideoBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        // 初始化
        init();

        // 初始化播放器
        initPlayer();

        // 初始化事件
        initEvent();

        StatusBarUtil.setStatusBarColor(this);
        int statusBarHeight = StatusBarUtil.getStatusBarHeight(this);
        binding.root.setPadding(0, statusBarHeight, 0, 0);
    }

    /**
     * 初始化
     */
    private void init() {
        // 初始化底部操作按钮
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.tabButs.setLayoutManager(layoutManager);
        adapter.setOnItemClickListener(this);
        binding.tabButs.setAdapter(adapter);
    }

    /**
     * 底部按钮
     */
    @SuppressLint("NotifyDataSetChanged")
    private void tagButs() {
        tabButsList = new ArrayList<>();
        tabButsList.add(new TabButsItem("删除", R.drawable.vector_tab_delete));
        VideoItem videoItem = videoItems.get(checkedIndex);
        if (videoItem.getIsVoice() == 1) {
            tabButsList.add(new TabButsItem("声音", R.drawable.vector_tab_sound));
        } else {
            tabButsList.add(new TabButsItem("静音", R.drawable.vector_tab_mute));
        }
        tabButsList.add(new TabButsItem("分割", R.drawable.vector_tab_spilt));
        tabButsList.add(new TabButsItem("文字", R.drawable.vector_tab_text));
        tabButsList.add(new TabButsItem("贴纸", R.drawable.vector_tab_paster));
        tabButsList.add(new TabButsItem("画中画", R.drawable.vector_tab_pip));
        tabButsList.add(new TabButsItem("特效", R.drawable.vector_tab_effects));
        adapter.notifyDataSetChanged();
        adapter.add(tabButsList);
    }

    /**
     * 隐藏底部按钮
     */
    private void hideTagButs() {
        binding.timeLineView.setSelectVideo(null);
        binding.tabButs.setVisibility(View.GONE);
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        //设置视频播放器
        mVideo = new ExoPlayer.Builder(this).build();
        binding.videoPlayer.setPlayer(mVideo);
//        for (CopyVideoItem item : videoItems) {
//            MediaItem mediaItem = new MediaItem.Builder()
//                    .setUri(videoPath)
//                    .setClippingConfiguration(new MediaItem.ClippingConfiguration.Builder()
//                            .setStartPositionMs(item.getStartTime())
//                            .setEndPositionMs(item.getEndTime())
//                            .build())
//                    .build();
//            mVideo.addMediaItem(mediaItem);
//        }
//        volume = mVideo.getVolume();
        mVideo.setVolume(0);
        mVideo.prepare();

        //初始化进度条
        //视频进度条
        binding.timeLineView.addTrack(TrackEnum.VIDEO_TRACK, true);
//        int duration = VideoUtils.getDuration(videoPath);
        for (int i = 0; i < videoItems.size(); i++) {
            VideoItem item = videoItems.get(i);
//            binding.timeLineView.addVideo(0, TrackEnum.VIDEO_TRACK, duration, videoPath,
//                    item.getStartTime(), item.getEndTime());
        }

        binding.timeLineView.updateVideos();
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        // 隐藏操作按钮
        binding.tabButs.setVisibility(View.GONE);
        // 暂停播放按钮
        binding.playPauseBut.setOnClickListener(this);
        // 导出按钮
        binding.exportVideo.setOnClickListener(this);
        // 保存进度按钮
        binding.save.setOnClickListener(this);
        // 添加视频按钮
        binding.addVideo.setOnClickListener(this);
        // 监听进度条滑动
        binding.timeLineView.timeChangeListener(new VideoPlayerOperate() {
            @Override
            public void startTrackingTouch() {
                hideTagButs();
                mVideo.pause();
            }

            @Override
            public void stopTrackingTouch(long ms) {
                updateVideoProgress(ms);
                List<List<VideoClip>> videoLines = binding.timeLineView.getVideoLines();
                for (int i = 0; i < videoLines.size(); i++) {
                    List<VideoClip> videoClips = videoLines.get(i);
                    // 计算播放完成的视频长度
                    long completeLen = 0;
                    // 上一个的视频累加长度
                    long lastLen = 0;
                    int index = 0;
                    for (int j = 0; j < videoClips.size(); j++) {
                        VideoClip videoClip = videoClips.get(j);
                        completeLen += videoClip.getDurationMs();
                        if (completeLen >= ms) {
                            index = j;
                            break;
                        }
                        lastLen += videoClip.getDurationMs();
                    }
                    mVideo.seekTo(index, ms - lastLen);
                }
            }

            @Override
            public void updateTimeByScroll(long time) {
            }
        });

        // 区域改变时间
        binding.timeLineView.areaChangeListener(new AreaChangeListener() {
            @Override
            public void onTouchDown(VideoClip videoClip) {
                if (videoClip == null) {
                    binding.tabButs.setVisibility(View.GONE);
                    binding.tabButs.startAnimation(AnimationUtils.getHiddenAlphaAnimation());
                } else {
                    if (mVideo.isPlaying()) return;
                    binding.tabButs.setVisibility(View.VISIBLE);
                    binding.tabButs.startAnimation(AnimationUtils.getShowAlphaAnimation());
                    int index = 0;
                    List<VideoClip> videoClips = binding.timeLineView.getVideoLines().get(0);
                    for (int i = 0; i < videoClips.size(); i++) {
                        if (videoClips.get(i).getId().equals(videoClip.getId())) {
                            index = i;
                            break;
                        }
                    }
                    checkedIndex = index;
                    tagButs();
                }
            }

            @Override
            public void onChange(@NonNull VideoClip videoClip) {
                List<VideoClip> videoClips = binding.timeLineView.getVideoLines().get(0);
                int index = 0;
                for (int i = 0; i < videoClips.size(); i++) {
                    if (videoClips.get(i).getId().equals(videoClip.getId())) {
                        index = i;
                        break;
                    }
                }
                mVideo.removeMediaItem(index);
                MediaItem mediaItem = new MediaItem.Builder()
                        .setUri(videoClip.getOriginalFilePath())
                        .setClippingConfiguration(new MediaItem.ClippingConfiguration.Builder()
                                .setStartPositionMs(videoClip.getStartAtMs())
                                .setEndPositionMs(videoClip.getEndAtMs())
                                .build())
                        .build();
                mVideo.addMediaItem(index, mediaItem);
                VideoItem copyVideoItem = videoItems.get(index);
                copyVideoItem.setStartTime(videoClip.getStartAtMs());
                copyVideoItem.setEndTime(videoClip.getEndAtMs());

                // 找到播放位置
                long timeValue = binding.timeLineView.timeLineValue.getTime();
                int centreIndex = 0;
                long overTime = 0;
                for (int i = 0; i < videoClips.size(); i++) {
                    VideoClip clip = videoClips.get(i);
                    if (clip.getId().equals(videoClip.getId())) {
                        centreIndex = i;
                        break;
                    }
                    overTime += clip.getDurationMs();
                }
                mVideo.seekTo(centreIndex, timeValue - overTime);
            }
        });

        // 播放更新进度
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (mVideo.isPlaying()) {
                        binding.playPauseBut.setBackgroundResource(R.drawable.vector_pause);
                        // 获取视频进度
                        List<VideoClip> videoClips = binding.timeLineView.getVideoLines().get(0);
                        // 当前播放片段的下标
                        int vCurrentIndex = mVideo.getCurrentMediaItemIndex();
                        // 当前播放片段位置
                        long vItemCurrentPosition = mVideo.getCurrentPosition();
                        // 计算播放完成的视频长度
                        long vCompleteLen = 0;
                        for (int j = 0; j < vCurrentIndex; j++) {
                            VideoClip videoClip = videoClips.get(j);
                            vCompleteLen += videoClip.getDurationMs();
                        }
                        long vCurrentPosition = vItemCurrentPosition + vCompleteLen;
                        binding.timeLineView.updateTime(vCurrentPosition);
                        updateVideoProgress(vCurrentPosition);
                        VideoItem copyVideoItem = videoItems.get(vCurrentIndex);
                        mVideo.setVolume(copyVideoItem.getIsVoice() == 0 ? 0 : volume);
                        hideTagButs();
                    } else {
                        binding.playPauseBut.setBackgroundResource(R.drawable.vector_play);
                    }
                });
            }
        }, 10, 10);
    }

    /**
     * 更新进度
     *
     * @param currentTime 当前进度时间
     */
    @SuppressLint("SetTextI18n")
    private void updateVideoProgress(long currentTime) {
        String currentStr = VideoUtils.stringForTime(currentTime);
        String durationStr = VideoUtils.stringForTime(binding.timeLineView.getTotalDurationMs());
        binding.currentPosition.setText(currentStr + "/" + durationStr);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideo != null) {
            mVideo.release();
        }
        if (timer != null) {
            timer.cancel();
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.play_pause_but) {
            // 播放与暂停
            if (mVideo.isPlaying()) {
                mVideo.pause();
            } else {
                mVideo.prepare();
                mVideo.play();
            }
        } else if (id == R.id.export_video) {
            // 导出
            mVideo.pause();
            Intent intent = new Intent();
//            intent.setClass(this, ExportVideoActivity.class);
            intent.putExtra("videoClips", (Serializable) videoItems);
//            intent.putExtra(Constant.SRC_VIDEO_KEY, getIntent().getStringExtra(Constant.SRC_VIDEO_KEY));
            startActivity(intent);
        } else if (id == R.id.save) {
        } else if (id == R.id.add_video) {
            // 添加视频
            Intent intent = new Intent();
            intent.setClass(EditVideoActivity.this, SelectVideoActivity.class);
            startActivityForResult(intent, 100);
        }
    }

    /**
     * 点击底部操作按钮时
     *
     * @param view     当前控件
     * @param position 当前位置
     */
    @Override
    public void onItemClick(View view, int position) {
        TabButsItem item = tabButsList.get(position);
        int id = item.getImageId();
        if (id == R.drawable.vector_tab_delete) {
            // 删除片段
            List<VideoClip> videoClips = binding.timeLineView.getVideoLines().get(0);
            if (videoClips.size() <= 1) {
                showToast("至少有一个片段!");
                return;
            }
            videoItems.remove(checkedIndex);
            mVideo.removeMediaItem(checkedIndex);
            videoClips.remove(checkedIndex);
            binding.timeLineView.updateVideos();
            hideTagButs();
            long currentMs = 0;
            if (videoClips.size() >= checkedIndex) {
                for (int i = 0; i < checkedIndex; i++) {
                    VideoClip videoClip = videoClips.get(i);
                    currentMs += videoClip.getDurationMs();
                }
            }
            binding.timeLineView.updateTime(currentMs);
            showToast("删除成功!");
        } else if (id == R.drawable.vector_tab_mute || id == R.drawable.vector_tab_sound) {
            // 设置-静音和声音
            VideoItem videoItem = videoItems.get(checkedIndex);
            videoItem.setIsVoice(id == R.drawable.vector_tab_mute ? 1 : 0);
            tagButs();
        } else {
            showToast("待开发!");
        }
    }
}
