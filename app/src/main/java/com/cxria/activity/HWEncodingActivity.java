package com.cxria.activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.cxria.ui.CameraPreviewFrameView;
import com.cxria.utils.Config;
import com.pili.pldroid.streaming.CameraStreamingManager;
import com.pili.pldroid.streaming.CameraStreamingSetting;
import com.pili.pldroid.streaming.StreamingProfile;
import com.pili.pldroid.streaming.widget.AspectFrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class HWEncodingActivity extends AppCompatActivity implements CameraStreamingManager.StreamingStateListener, View.OnClickListener {
    private JSONObject mJSONObject;
    private CameraStreamingManager mCameraStreamingManager;

    private TextView tv_status;
    private Button btn_switch_cam;
    private Button btn_live_or_not;
    private StreamingProfile.Stream stream;
    private StreamingProfile profile;
    private CameraStreamingSetting setting;

    private AspectFrameLayout afl;
    private CameraPreviewFrameView cameraPreviewFrameView;
    /**
     * 保持屏幕常亮
     */
    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hwencoding);
        initViews();
        setListeners();

        getLiveAddress();
        setLiveStatus();
    }

    private void setListeners() {
        btn_switch_cam.setOnClickListener(this);
        btn_live_or_not.setOnClickListener(this);
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        powerManager = (PowerManager) this.getSystemService(this.POWER_SERVICE);
        wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");

        tv_status = (TextView) findViewById(R.id.tv_status);
        btn_switch_cam = (Button) findViewById(R.id.btn_switch_cam);
        btn_live_or_not = (Button) findViewById(R.id.btn_live_or_not);

        afl = (AspectFrameLayout) findViewById(R.id.cameraPreview_afl);
        cameraPreviewFrameView = (CameraPreviewFrameView) findViewById(R.id.cameraPreview_surfaceView);
    }

    /**
     * 设置直播信息
     */
    public void setLiveStatus() {
        stream = new StreamingProfile.Stream(mJSONObject);
        profile = new StreamingProfile();
        profile.setVideoQuality(StreamingProfile.VIDEO_QUALITY_HIGH3)
                .setAudioQuality(StreamingProfile.AUDIO_QUALITY_HIGH2)
                .setStream(stream);

        setting = new CameraStreamingSetting();
        setting.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT)
                .setContinuousFocusModeEnabled(true)
                .setCameraPrvSizeLevel(CameraStreamingSetting.PREVIEW_SIZE_LEVEL.LARGE)
                .setCameraPrvSizeRatio(CameraStreamingSetting.PREVIEW_SIZE_RATIO.RATIO_16_9);

        mCameraStreamingManager = new CameraStreamingManager(this, afl, cameraPreviewFrameView, CameraStreamingManager.EncodingType.HW_VIDEO_WITH_HW_AUDIO_CODEC);
        mCameraStreamingManager.prepare(setting, profile);
        mCameraStreamingManager.setStreamingStateListener(this);
    }

    /**
     * 获取直播流信息
     */
    private void getLiveAddress() {
        try {
            String streamJsonStrFromServer = getIntent().getStringExtra(Config.EXTRA_KEY_STREAM_JSON);
            mJSONObject = new JSONObject(streamJsonStrFromServer);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch_cam:
                mCameraStreamingManager.switchCamera();
                break;
            case R.id.btn_live_or_not:
                if (btn_live_or_not.getText().equals("正在直播...")) {
                    btn_live_or_not.setText("开始直播");
                    stopStreaming();
                } else {
                    btn_live_or_not.setText("正在直播");
                    startStreaming();
                }
                break;
        }
    }

    @Override
    public void onStateChanged(int state, Object o) {
        switch (state) {
            case CameraStreamingManager.STATE.PREPARING:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_status.setText("准备中...");
                    }
                });
                break;
            case CameraStreamingManager.STATE.READY:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_status.setText("点击开始直播");
                        btn_live_or_not.setText("开始直播");
                    }
                });
                break;
            case CameraStreamingManager.STATE.CONNECTING:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_status.setText("连接中...");
                    }
                });
                break;
            case CameraStreamingManager.STATE.STREAMING:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_status.setText("正在直播...");
                        btn_live_or_not.setText("正在直播...");
                    }
                });
                break;
            case CameraStreamingManager.STATE.SHUTDOWN:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_status.setText("点击开始直播");
                    }
                });
                break;
            case CameraStreamingManager.STATE.IOERROR:
                break;
            case CameraStreamingManager.STATE.UNKNOWN:
                break;
            case CameraStreamingManager.STATE.SENDING_BUFFER_EMPTY:
                break;
            case CameraStreamingManager.STATE.SENDING_BUFFER_FULL:
                break;
            case CameraStreamingManager.STATE.AUDIO_RECORDING_FAIL:
                break;
            case CameraStreamingManager.STATE.OPEN_CAMERA_FAIL:
                break;
            case CameraStreamingManager.STATE.DISCONNECTED:
                break;
        }
    }

    private void startStreaming() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCameraStreamingManager.startStreaming();
            }
        }).start();
    }

    private void stopStreaming() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCameraStreamingManager.stopStreaming();
            }
        }).start();
    }

    @Override
    public boolean onStateHandled(int i, Object o) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraStreamingManager.resume();
        wakeLock.acquire();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraStreamingManager.pause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        wakeLock.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraStreamingManager.destroy();
        mCameraStreamingManager.stopStreaming();
    }
}
