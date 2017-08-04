package com.example.android.common.media;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by pc on 2017/7/25.
 */

public class VideoRecorder implements SurfaceHolder.Callback {
    private Activity activity;
    private Camera mCamera = null;//相机
    private Camera.Size mSize = null;//相机的尺寸
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;//默认后置摄像头
    private static final SparseIntArray orientations = new SparseIntArray();//手机旋转对应的调整角度
    private MediaRecorder mMediaRecorder;
    private SurfaceHolder mSurfaceHolder;
    private boolean isCameraInit;
    private int width, height;
    private Handler mHandler = new Handler();
    private int orientation;

    static {
        orientations.append(Surface.ROTATION_0, 90);
        orientations.append(Surface.ROTATION_90, 0);
        orientations.append(Surface.ROTATION_180, 270);
        orientations.append(Surface.ROTATION_270, 180);
    }

    private File mOutputFile;
    private SurfaceView glSurfaceView;
    private boolean isRecording;
    private RecorderListener listener;
    private int cameraPosition = 1;
    private boolean audioEnable = true;

    public VideoRecorder(Activity activity, SurfaceView glSurfaceView) {
        this.activity = activity;
        this.glSurfaceView = glSurfaceView;
        width = activity.getResources().getDisplayMetrics().widthPixels;
        height = activity.getResources().getDisplayMetrics().heightPixels;
//        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
//
//        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
//        audioManager.setStreamMute(AudioManager.STREAM_MUSIC,true);
//        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
//        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, 0, 0);
//        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
//        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);

    }


    public void initView() {
        SurfaceHolder holder = glSurfaceView.getHolder();// 取得holder
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.setKeepScreenOn(true);
        holder.addCallback(this); // holder加入回调接口

    }

    /**
     * 初始化相机
     */
    public void initCamera() {
        initCamera(mCameraFacing);
    }

    /**
     * 初始化相机
     */
    public void initCamera(int mCameraFacing) {
        if (Camera.getNumberOfCameras() == 2) {
            mCamera = Camera.open(mCameraFacing);
        } else {
            mCamera = Camera.open();
        }
        Camera.Parameters parameters = mCamera.getParameters();
        mSize = CameraHelper.getOptimalVideoSize(parameters.getSupportedVideoSizes(), parameters.getSupportedPreviewSizes(), glSurfaceView.getWidth(), glSurfaceView.getHeight());
        List<String> focusModesList = parameters.getSupportedFocusModes();

        //增加对聚焦模式的判断
        if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        } else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int videoFrameWidth = 0, videoFrameHeight;
        orientation = orientations.get(rotation);
        if(orientation==90||orientation ==270) {
            videoFrameWidth = height;
            videoFrameHeight = width;
        }else {
        videoFrameWidth = width;
        videoFrameHeight = height;
        }
        parameters.setPreviewSize(videoFrameWidth, videoFrameHeight);
        mCamera.setParameters(parameters);

        mCamera.setDisplayOrientation(orientation);
        isCameraInit = true;
//                 if(null!=listener){
//                     mHandler.post(new Runnable() {
//                         @Override
//                         public void run() {
//                             listener.onInitCamera();
//                         }});


    }


    private boolean prepareVideoRecorder() {
        if (!isCameraInit) {
            initCamera();
        }
        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();
        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
//        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mMediaRecorder.setOrientationHint(orientation);
        // Step 2: Set sources
        // 设置音频采集方式
        if (audioEnable) {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        }
        //设置视频的采集方式
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);


        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        profile.videoCodec =MediaRecorder.VideoEncoder.H264; //设置video的编码格式
        profile.videoBitRate = (int) (1*1024 * 1024); //设置录制的视频编码比特率

        if (orientation == 90 || orientation == 270) {
            profile.videoFrameHeight = width;
            profile.videoFrameWidth = height;
        } else {
            profile.videoFrameHeight = height;
            profile.videoFrameWidth =width ;
        }

        if(audioEnable) {
            profile.audioCodec = MediaRecorder.AudioEncoder.AAC; //设置audio的编码格式
            profile.audioBitRate = 48000;
            profile.audioSampleRate=44100;
            profile.audioChannels=1;
        }
        profile.fileFormat =MediaRecorder.OutputFormat.MPEG_4;  //设置文件的输出格式

        //设置要捕获的视频的宽度和高度

        mMediaRecorder.setProfile(profile);
        //设置记录会话的最大持续时间（毫秒）
//        mMediaRecorder.setMaxDuration(30 * 1000);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
//        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
//        mMediaRecorder.setProfile(profile);
        // Step 4: Set output file
        mOutputFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO);
        if (mOutputFile == null) {
            return false;
        }
        mMediaRecorder.setOutputFile(mOutputFile.getPath());
        // END_INCLUDE (configure_media_recorder)
        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
//            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
//            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }

        return true;
    }


    public void startRecord() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (prepareVideoRecorder()) {
                    // Camera is available and unlocked, MediaRecorder is prepared,
                    // now you can start recording
                    mMediaRecorder.start();
                    isRecording = true;
                    if (null != listener) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onstarted();
                            }
                        });
                    }
                } else {
                    // prepare didn't work, release the camera
                    releaseMediaRecorder();
                }
            }
        }.start();


    }

    public boolean isRecording() {
        return isRecording;
    }

    public void switchCamera() {
        //切换前后摄像头
//        int cameraCount = 0;
//        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

        if (Camera.getNumberOfCameras() == 2) {
            if (cameraPosition == 1) {
                releaseCamera();
                initCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                try {
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cameraPosition = 0;

            } else {
                releaseCamera();
                initCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                try {
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cameraPosition = 1;
            }
        }
    }

    public void setAudioEnable(boolean audioEnable) {

        this.audioEnable = audioEnable;

    }


    public void stopRecord() {
        // stop recording and release camera
        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    if (null != mMediaRecorder) {
                        mMediaRecorder.stop();  // stop the recording
                    }
                } catch (RuntimeException e) {
                    // RuntimeException is thrown when stop() is called immediately after start().
                    // In this case the output file is not properly constructed ans should be deleted.
//            Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                    //noinspection ResultOfMethodCallIgnored
                    mOutputFile.delete();
                }
                releaseMediaRecorder(); // release the MediaRecorder object
                if (null != mCamera) {
                    mCamera.lock();// take camera access back from MediaRecorder
                }
                // inform the user that recording has stopped
                releaseCamera();
                isRecording = false;
                if (null != listener) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onstoped();
                        }
                    });
                }
            }
        }.start();

    }


    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
            mMediaRecorder = null;
        }
    }


    private void releaseCamera() {
        if (mCamera != null) {
            // release the camera for other applications
            mCamera.stopPreview();
//            mCamera.setPreviewCallback(null);
//            mCamera.unlock();
            isCameraInit = false;
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        if (null != mSurfaceHolder) {
            mSurfaceHolder.setFixedSize(glSurfaceView.getHeight(), glSurfaceView.getWidth());//最高只能设置640x480

        }
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder = holder;
        if (null != mSurfaceHolder) {
            mSurfaceHolder.setFixedSize(glSurfaceView.getHeight(), glSurfaceView.getWidth());//最高只能设置640x480

        }
        if (mCamera == null) {
            return;
        }
        try {
            //设置显示
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            releaseCamera();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (isRecording && mCamera != null) {
            mCamera.lock();
        }
        mSurfaceHolder = null;
        releaseMediaRecorder();
        releaseCamera();
    }


    public interface RecorderListener {
        public void onInitCamera();

        public void onstarted();

        public void onstoped();

    }

    public void setRecorderListener(RecorderListener listener) {

        this.listener = listener;
    }

}
