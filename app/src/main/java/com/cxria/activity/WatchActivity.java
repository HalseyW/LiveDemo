package com.cxria.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cxria.utils.MediaController;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.widget.VideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class WatchActivity extends AppCompatActivity implements
        IjkMediaPlayer.OnCompletionListener,
        IjkMediaPlayer.OnInfoListener,
        IjkMediaPlayer.OnErrorListener,
        IjkMediaPlayer.OnVideoSizeChangedListener,
        IjkMediaPlayer.OnPreparedListener {
    private MediaController mMediaController;
    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        mMediaController = new MediaController(this, false, false);
        mVideoView = (VideoView) findViewById(R.id.video_view);

        mVideoView.setMediaController(mMediaController);

        AVOptions options = new AVOptions();
        options.setInteger(AVOptions.KEY_MEDIACODEC, 0);
        options.setInteger(AVOptions.KEY_BUFFER_TIME, 1000); // the unit of buffer time is ms
        options.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 10 * 1000); // the unit of timeout is ms
        options.setString(AVOptions.KEY_FFLAGS, AVOptions.VALUE_FFLAGS_NOBUFFER); // "nobuffer"
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, 1);

        mVideoView.setAVOptions(options);

        mVideoView.setVideoPath("rtmp://pili-live-rtmp.qn.cxria.com/cxlive/cxlive");

        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnVideoSizeChangedListener(this);

        mVideoView.requestFocus();
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        mVideoView.start();
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

    }
}
