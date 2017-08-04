package com.example.android.mediaMuxter;

import android.annotation.TargetApi;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by pc on 2017/7/28.
 */

public class MediaUtils {

    MediaExtractor mediaExtractor;

    public MediaUtils() {
        mediaExtractor = new MediaExtractor();
        File rawFile = new File(SD, "input.mp4");
        try {
            mediaExtractor.setDataSource(rawFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    String SD = Environment.getExternalStorageDirectory().getAbsolutePath();

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void splitvideo() {
        File outVideo = new File(SD, "decodevideo.mp4");
        File outAudio = new File(SD, "decodevideo.mp4");

        int audioId = -1;
        int videoId = -1;
        try {
            FileOutputStream videoOutputStream = new FileOutputStream(outVideo);
            FileOutputStream audioOutputStream = new FileOutputStream(outAudio);

            int trackcount = mediaExtractor.getTrackCount();
            if (trackcount <= 0) {
                return;
            }
            for (int i = 0; i < trackcount; i++) {
                MediaFormat format = mediaExtractor.getTrackFormat(i);
                String mine = format.getString(MediaFormat.KEY_MIME);
                //视频信道
                if (mine.startsWith("video/")) {
                    videoId = i;
                }
                //音频信道
                if (mine.startsWith("audio/")) {
                    audioId = i;
                }
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(1000 * 1024);
            mediaExtractor.selectTrack(videoId);
            int datasize = 0;
            while (true) {
                datasize = mediaExtractor.readSampleData(byteBuffer, 0);
                if (datasize < 0) {
                    break;
                }
                byte[] buffer = new byte[datasize];
                byteBuffer.get(buffer);
                videoOutputStream.write(buffer);
                byteBuffer.clear();
                mediaExtractor.advance();
            }
            mediaExtractor.selectTrack(audioId);
            while (true) {
                datasize = mediaExtractor.readSampleData(byteBuffer, 0);
                if (datasize < 0) {
                    break;
                }
                byte[] buffer = new byte[datasize];
                byteBuffer.get(buffer);
                audioOutputStream.write(buffer);
                byteBuffer.clear();
                mediaExtractor.advance();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void readVideoOnly() {
        try {
            int videoId =-1;
            int trackcount = mediaExtractor.getTrackCount();
            if (trackcount <= 0) {
                return;
            }
            MediaFormat format = null;
            for (int i = 0; i < trackcount; i++) {
                 format = mediaExtractor.getTrackFormat(i);
                String mine = format.getString(MediaFormat.KEY_MIME);
                //视频信道
                if (mine.startsWith("video/")) {
                    videoId = i;
                    break;
                }
            }
            if(null==format){
                return;
            }
            mediaExtractor.selectTrack(videoId);
            MediaMuxer mediaMuxer = new MediaMuxer(SD + "/decodevideo.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//            try {
//                String orientat =format.getString(MediaFormat.KEY_ROTATION);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//            try {
//                int orientat =format.getInteger(MediaFormat.KEY_ROTATION);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//            try {
//                mediaMuxer.setOrientationHint(format.getInteger(MediaFormat.KEY_ROTATION));
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//            try {
//                mediaMuxer.setOrientationHint(format.getInteger(MediaFormat.KEY_ROTATION));
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//            try {
//                mediaMuxer.setOrientationHint(Integer.parseInt(format.getString(MediaFormat.KEY_ROTATION)));
//            }catch (Exception e){
//                e.printStackTrace();
//            }
            int trackIndex = mediaMuxer.addTrack(format);
            ByteBuffer inputBuffer = ByteBuffer.allocate(1024*1024);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            mediaMuxer.start();
            boolean isAudioSample = false;
            boolean finshed = false;
            while (!finshed){
                inputBuffer.clear();
                finshed = getInputBuffer(inputBuffer,isAudioSample,bufferInfo);
                if(!finshed){
                    mediaMuxer.writeSampleData(trackIndex,inputBuffer,bufferInfo);
                }
            }
            mediaMuxer.stop();
            mediaMuxer.release();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void readAudioOnly() {
        try {
            int audioId =-1;
            int trackcount = mediaExtractor.getTrackCount();
            if (trackcount <= 0) {
                return;
            }
            MediaFormat format = null;
            for (int i = 0; i < trackcount; i++) {
                format = mediaExtractor.getTrackFormat(i);
                String mine = format.getString(MediaFormat.KEY_MIME);
                //视频信道
                if (mine.startsWith("audio/")) {
                    audioId = i;
                    break;
                }
            }
            if(null==format){
                return;
            }
            mediaExtractor.selectTrack(audioId);
            MediaMuxer mediaMuxer = new MediaMuxer(SD + "/decodevideo.aac", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int trackIndex = mediaMuxer.addTrack(format);
            ByteBuffer inputBuffer = ByteBuffer.allocate(1024*1024);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            mediaMuxer.start();
            boolean isAudioSample = false;
            boolean finshed = false;
            while (!finshed){
                inputBuffer.clear();
                finshed = getInputBuffer(inputBuffer,isAudioSample,bufferInfo);
                if(!finshed){
                    mediaMuxer.writeSampleData(trackIndex,inputBuffer,bufferInfo);
                }
            }
            mediaMuxer.stop();
            mediaMuxer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean getInputBuffer(ByteBuffer inputBuffer, boolean isAudioSample, MediaCodec.BufferInfo bufferInfo){
       int datasize = mediaExtractor.readSampleData(inputBuffer, 0);
        bufferInfo.presentationTimeUs =mediaExtractor.getSampleTime();
        if(bufferInfo.presentationTimeUs ==-1){
            return true;
        }
        bufferInfo.flags=mediaExtractor.getSampleFlags();
        bufferInfo.offset=0;
        bufferInfo.size = datasize;
        mediaExtractor.advance();
        return false;
    }



    public void combineSomeVideos(ArrayList<String> paths){
       ArrayList<MediaExtractor> extractors = new ArrayList<>();
        try {
            for(int i=0;i<paths.size();i++){
                extractors.add(extractorVideo(paths.get(i)));
            }
            for(int i=0;i<extractors.size();i++){


            }


        }catch (Exception e){
            e.printStackTrace();
        }




    }

    private MediaExtractor extractorVideo(String path) throws Exception{

        MediaExtractor videoExtractor = new MediaExtractor();
        videoExtractor.setDataSource(SD + "/decodevideo.mp4");
        return videoExtractor;
    }



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void combineVideo() {
        try {
            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(SD + "/decodevideo.mp4");
            MediaFormat videoFormat = null;
            int videoTrackIndex = -1;
            int videoTrackCount = videoExtractor.getTrackCount();
            for (int i = 0; i < videoTrackCount; i++) {
                videoFormat = videoExtractor.getTrackFormat(i);
                String mimeType = videoFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("video/")) {
                    videoTrackIndex = i;
                    break;
                }
            }

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(SD + "/decodevideo.aac");
            MediaFormat audioFormat = null;
            int audioTrackIndex = -1;
            int audioTrackCount = audioExtractor.getTrackCount();
            for (int i = 0; i < audioTrackCount; i++) {
                audioFormat = audioExtractor.getTrackFormat(i);
                String mimeType = audioFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("audio/")) {
                    audioTrackIndex = i;
                    break;
                }
            }


            videoExtractor.selectTrack(videoTrackIndex);
            audioExtractor.selectTrack(audioTrackIndex);

            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

            MediaMuxer mediaMuxer = new MediaMuxer(SD + "/output.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mediaMuxer.setOrientationHint(videoFormat.getInteger(MediaFormat.KEY_ROTATION));
            int writeVideoTrackIndex = mediaMuxer.addTrack(videoFormat);
            int writeAudioTrackIndex = mediaMuxer.addTrack(audioFormat);
            mediaMuxer.start();
            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
//            long sampleTime = 0;
//            {
//                videoExtractor.readSampleData(byteBuffer, 0);
//                if (videoExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
//                    videoExtractor.advance();
//                }
//                videoExtractor.readSampleData(byteBuffer, 0);
//                long secondTime = videoExtractor.getSampleTime();
//                videoExtractor.advance();
//                long thirdTime = videoExtractor.getSampleTime();
//                sampleTime = Math.abs(thirdTime - secondTime);
//            }
//            videoExtractor.unselectTrack(videoTrackIndex);
//            videoExtractor.selectTrack(videoTrackIndex);

            while (true) {
                int readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0);
                if (readVideoSampleSize < 0) {
                    break;
                }
                videoBufferInfo.size = readVideoSampleSize;
                videoBufferInfo.presentationTimeUs= videoExtractor.getSampleTime();
                videoBufferInfo.offset = 0;
                videoBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo);
                videoExtractor.advance();
            }

            while (true) {
                int readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0);
                if (readAudioSampleSize < 0) {
                    break;
                }
                audioBufferInfo.size = readAudioSampleSize;
                audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                audioBufferInfo.offset = 0;
                audioBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);
                audioExtractor.advance();
            }

            mediaMuxer.stop();
            mediaMuxer.release();
            videoExtractor.release();
            audioExtractor.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
