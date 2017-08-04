package com.example.android.mediarecorder;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.android.common.media.VideoRecorder;

/**
 * Created by pc on 2017/7/25.
 */

public class VideoRecorderActivity extends Activity implements View.OnClickListener,VideoRecorder.RecorderListener{
    private Button control,showvideos,nextStep;
    private SurfaceView surfaceview;
    private VideoRecorder recorder;
    private ImageView close,camera,voice;
    private Handler handler = new Handler();
    private int num;
    Runnable timerRunnable=new  Runnable() {
        @Override
        public void run() {
            num+=1;
            if(num==30){
                handler.removeCallbacks(this);
                num =0;
                if(null!=recorder){
                    recorder.stopRecord();
                }
            }
            handler.postDelayed(this,1000);
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videorecorder);
        findViewByIds();
         recorder = new VideoRecorder(this,surfaceview);
        recorder.initView();
        recorder.initCamera();
        recorder.setRecorderListener(this);
    }

    private void findViewByIds(){
        control = (Button) findViewById(R.id.control);
        showvideos = (Button) findViewById(R.id.showVideos);
        nextStep = (Button) findViewById(R.id.nextstep);
        surfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        close = (ImageView) findViewById(R.id.close);
        camera = (ImageView) findViewById(R.id.switchcamera);
        voice = (ImageView) findViewById(R.id.voice);
        control.setOnClickListener(this);
        showvideos.setOnClickListener(this);
        nextStep.setOnClickListener(this);
        close.setOnClickListener(this);
        camera.setOnClickListener(this);
        voice.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {
        if(v==control){
            if(recorder.isRecording()){
                recorder.stopRecord();
                num = 0;
                handler.removeCallbacks(timerRunnable);
            }else {
                num = 0;
                recorder.startRecord();

            }
        }else if(v ==showvideos){
            recorder.stopRecord();
        }else if(v == close){
            finish();
        }else if(v == camera){
            if(null!=recorder) {
                recorder.switchCamera();
            }
        }else if(v == voice){


        }


    }

    @Override
    public void onInitCamera() {

    }

    @Override
    public void onstarted() {
       handler.postDelayed(timerRunnable,1000);
    }

    @Override
    public void onstoped() {

    }
}
