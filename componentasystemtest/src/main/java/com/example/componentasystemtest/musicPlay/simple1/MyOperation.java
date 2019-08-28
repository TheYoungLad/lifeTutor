package com.example.componentasystemtest.musicPlay.simple1;

public interface MyOperation {
    public void play();//播放

    public void pause();//暂停

    public void moveon();//继续

    public void rePlay();//重新开始

    public void seekToPosition(int position);//跳到相应位置

    //    public long getDuration();//获取视频长度
    public long getCurrentPosition();//获取当前播放位置

}
