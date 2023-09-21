package com.video.entity;


/**
 * 底部导航
 *
 * @author liuguofeng
 * @date 2023/9/21 10:03
 **/
public class TabButsItem {
    private String text;
    private int imageId;

    public TabButsItem(String text, int imageId) {
        this.text = text;
        this.imageId = imageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
