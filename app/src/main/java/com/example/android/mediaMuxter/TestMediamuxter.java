package com.example.android.mediaMuxter;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.view.View;

import com.example.android.mediarecorder.R;

/**
 * Created by pc on 2017/7/28.
 */

public class TestMediamuxter extends Activity implements View.OnClickListener{
    private MediaUtils mediaUtils;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splitemedia);
        findViewById(R.id.but1).setOnClickListener(this);
        findViewById(R.id.but2).setOnClickListener(this);
        findViewById(R.id.but3).setOnClickListener(this);
        findViewById(R.id.but4).setOnClickListener(this);
        mediaUtils = new MediaUtils();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.but1:

                mediaUtils.splitvideo();
                break;
            case R.id.but2:
                mediaUtils.readAudioOnly();

                break;
            case R.id.but3:
                mediaUtils.readVideoOnly();
                break;
            case R.id.but4:
                mediaUtils.combineVideo();
                break;
        }
    }



}
