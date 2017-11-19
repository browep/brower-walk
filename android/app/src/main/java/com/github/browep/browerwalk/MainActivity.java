package com.github.browep.browerwalk;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getCanonicalName();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private List<Thread> threads = new LinkedList<>();
    private List<Float> times = new LinkedList<>();
    private TextView threadCountTextView;
    private View stopMiningButton;
    private TextView hashRateTextView;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new MinerHandler();

        View startMiningBtn = findViewById(R.id.start_mining_btn);
        startMiningBtn.setOnClickListener(this);
        stopMiningButton = findViewById(R.id.stop_mining_btn);
        stopMiningButton.setOnClickListener(this);

        threadCountTextView = findViewById(R.id.thread_count_tv);
        hashRateTextView = findViewById(R.id.hash_rate_tv);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateThreadCount();
    }

    public native void startMiner();

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_mining_btn) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "starting miner");
                    while (!Thread.interrupted()) {
                        long startTime = System.currentTimeMillis();
                        startMiner();
                        long totalTime = System.currentTimeMillis() - startTime;
                        Message msg = Message.obtain();
                        msg.obj = BigDecimal.valueOf(totalTime).divide(BigDecimal.valueOf(1000)).floatValue();
                        handler.sendMessage(msg);
                    }
                }
            });
            thread.start();
            threads.add(thread);
        } else if (v.getId() == R.id.stop_mining_btn) {
            if (!threads.isEmpty()) {
                Thread thread = threads.remove(0);
                thread.interrupt();
            }
        }

        updateThreadCount();
    }

    private void updateThreadCount() {
        threadCountTextView.setText(MessageFormat.format(getString(R.string.workers_count), threads.size()));
        stopMiningButton.setEnabled(!threads.isEmpty());
        times.clear();
    }

    private class MinerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            times.add((Float) msg.obj);

            float sum = 0;

            for (Float time : times) {
                sum += time;
            }

            sum /= times.size();

            if (!threads.isEmpty()) {
                BigDecimal secondsPerHash = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(threads.size()), new MathContext(3, RoundingMode.HALF_UP));
                BigDecimal hashesPerMin = BigDecimal.ONE.divide(secondsPerHash, 3, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(60));
                hashRateTextView.setText(MessageFormat.format("{0} seconds per hash\n{1} H/m", secondsPerHash.toPlainString(), hashesPerMin.toPlainString()));
            } else {
                hashRateTextView.setText("-.-");
            }
        }
    }
}
