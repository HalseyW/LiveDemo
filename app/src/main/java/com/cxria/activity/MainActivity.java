package com.cxria.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cxria.utils.Config;
import com.pili.Hub;
import com.pili.PiliException;
import com.pili.Stream;
import com.qiniu.Credentials;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "halsey";
    private Button btn_start_live;
    private Button btn_watch_live;

    public static final String ACCESS_KEY = "Vybf6u2ufaGApwUiT1J_SuDDaXHLoiDY34_z9LhQ";
    public static final String SECRET_KEY = "b9j42VmiajhhvJEvsRCz59rc7COOFUZefe0j-5b9";

    private static final String url = "your app server address.";

    public static final String HUB_NAME = "cxlive";

    /**
     * 检查是否支持硬解码
     *
     * @return
     */
    private static boolean isSupportHWEncode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initButton();
    }

    /**
     * 开始直播跳转到解码Activity
     */
    private void live() {
        Intent intent = new Intent(MainActivity.this, HWEncodingActivity.class);
        startHWEncodingActivity(intent);
    }

    /**
     * 获得直播参数
     *
     * @param intent
     */
    private void startHWEncodingActivity(final Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String resByHttp = null;

                    Credentials credentials = new Credentials(ACCESS_KEY, SECRET_KEY);
                    Hub hub = new Hub(credentials, HUB_NAME);

                    Stream stream = hub.getStream("z1.cxlive.cxlive");

                    if (!Config.DEBUG_MODE) {
                        //resByHttp = requestStreamJson();
                        resByHttp = stream.toJsonString();

                        Log.i(TAG, "resByHttp:" + resByHttp);
                        if (resByHttp == null) {
                            showToast("Json流获取失败");
                            return;
                        }
                        intent.putExtra(Config.EXTRA_KEY_STREAM_JSON, resByHttp);
                    } else {
                        showToast("Stream Json Got Fail!");
                    }
                    startActivity(intent);
                } catch (PiliException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String requestStreamJson() {
        try {
            HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setConnectTimeout(5000);
            httpConn.setReadTimeout(10000);
            int responseCode = httpConn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int length = httpConn.getContentLength();
            if (length <= 0) {
                return null;
            }
            InputStream is = httpConn.getInputStream();
            byte[] data = new byte[length];
            int read = is.read(data);
            is.close();
            if (read <= 0) {
                return null;
            }
            return new String(data, 0, read);
        } catch (Exception e) {
            showToast("Network error!");
        }
        return null;
    }

    /**
     * 初始化硬解码按钮
     */
    private void initButton() {
        btn_start_live = (Button) findViewById(R.id.btn_start_live);
        btn_watch_live = (Button) findViewById(R.id.btn_watch_live);
        if (!isSupportHWEncode()) {
            btn_start_live.setVisibility(View.INVISIBLE);
        }
        btn_start_live.setOnClickListener(this);
        btn_watch_live.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_live:
                live();
                break;
            case R.id.btn_watch_live:
                startActivity(new Intent(this, WatchActivity.class));
                break;
        }
    }

    void showToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
