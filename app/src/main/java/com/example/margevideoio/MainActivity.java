package com.example.margevideoio;

import android.os.Bundle;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SurfaceView mSv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        new Thread(){
            @Override
            public void run() {
                super.run();
//                loadVideoIo();

            }
        }.start();
    }

    private void loadVideoIo() {

    }

    private void initView() {
        mSv = findViewById(R.id.sv);
    }
}