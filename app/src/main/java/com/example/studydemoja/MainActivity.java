package com.example.studydemoja;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView send_msg_to_main_tv;
    private TextView send_msg_to_child_tv;
    private TextView showTv;

    private volatile int count;

    android.os.Handler aHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            //这里接受并处理消息
            switch (msg.what) {
                case 1:
                    Log.d(TAG, "消息到达：" + Thread.currentThread().getName());
                    showTv.setText(String.valueOf(msg.obj));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        send_msg_to_main_tv = findViewById(R.id.send_msg_to_main_tv);
        send_msg_to_child_tv = findViewById(R.id.send_msg_to_child_tv);
        showTv = findViewById(R.id.show_tv);

        //1、子线程向UI线程发送消息
        send_msg_to_main_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AThread aThread = new AThread();
                aThread.start();
            }
        });

        //1、子线程向子线程发送消息
        send_msg_to_child_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BThread bThread = new BThread();
                bThread.start();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message message = bThread.getcHandler().obtainMessage();
                        message.what = 1;
                        message.obj = count++;
                        Log.d(TAG, "消息来源：" + Thread.currentThread().getName());
                        bThread.getcHandler().sendMessage(message);
                    }
                }).start();
            }
        });

    }

    //子线程A
    class AThread extends Thread {

        @Override
        public void run() {
            super.run();
            Message message = Message.obtain();
            message.what = 1;
            message.obj = count++;
            Log.d(TAG, "消息来源：" + Thread.currentThread().getName());
            aHandler.sendMessage(message);
        }
    }

    //子线程B 处理消息
    class BThread extends Thread {

        public Handler getcHandler() {
            return cHandler;
        }

        Handler cHandler;

        @Override
        public void run() {
            super.run();
            Looper.prepare();
            cHandler = new Handler() {
                @Override
                public void handleMessage(final Message msg) {
                    //这里接受并处理消息
                    switch (msg.what) {
                        case 1:
                            Log.d(TAG, "消息到达：" + Thread.currentThread().getName());
                            break;
                        default:
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }

}
