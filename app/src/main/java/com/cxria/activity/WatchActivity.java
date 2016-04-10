package com.cxria.activity;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.WindowManager;

import com.cxria.utils.MediaController;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.common.Util;
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
    private Pair<Integer, Integer> mScreenSize;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        dialog = ProgressDialog.show(this, "", "正在缓冲...");
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
        dialog.dismiss();
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height, int sarNum, int sarDen) {
        if (width > height) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            mScreenSize = Util.getResolution(this);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            mScreenSize = Util.getResolution(this);
        }

        if (width < mScreenSize.first) {
            height = mScreenSize.first * height / width;
            width = mScreenSize.first;
        }

        if (width * height < mScreenSize.first * mScreenSize.second) {
            width = mScreenSize.second * width / height;
            height = mScreenSize.second;
        }
    }
}
