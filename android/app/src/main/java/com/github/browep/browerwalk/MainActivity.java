package com.github.browep.browerwalk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements MinerCallback, View.OnClickListener {

    private static final String TAG = MainActivity.class.getCanonicalName();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.start_mining_btn).setOnClickListener(this);
    }

    public native String startMiner(MinerCallback minerCallback);

    @Override
    public void onHash(String hash) {
        Log.d(TAG, "hash: " + hash);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_mining_btn) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "starting miner");
                    startMiner(MainActivity.this);
                }
            }).start();
        }
    }
}
