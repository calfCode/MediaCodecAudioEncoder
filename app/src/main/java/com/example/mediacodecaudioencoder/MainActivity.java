package com.example.mediacodecaudioencoder;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements AudioEncoder.OutputAACDelegate {
    private static final String TAG = "BRUCE";
    private FileOutputStream fileOutputStream;
    private FileInputStream fileInputStream;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String assertDir = "music";
        AssectsUtil.loadAssetsDirfile(getApplicationContext(),assertDir);
        String appFilePath = AssectsUtil.getAppDir(getApplicationContext(),assertDir)+ File.separator;
//        Log.d(TAG,"appFilePath="+appFilePath);
        final String musicPath = appFilePath+"test_ffmpeg.pcm";
        Log.d(TAG,"musicPath="+musicPath);
        String outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+File.separator+"731.aac";
        Log.d(TAG,"outPath="+outPath);
        try {
            fileOutputStream = new FileOutputStream(outPath);
        }catch (Exception e){
            Log.d(TAG,"fileOutputStream  e="+e.getLocalizedMessage());
        }
        final AudioEncoder.OutputAACDelegate outputAACDelegate = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTimeMills = System.currentTimeMillis();
                AudioEncoder audioEncoder = null;
                Log.i(TAG, "START ENCODE");
                try {

                    audioEncoder = new AudioEncoder(outputAACDelegate, 44100, 2, 128 * 1024);
                    fileInputStream =  new FileInputStream(musicPath);
                    int bufferSize = 1024 * 256;
                    byte[] buffer = new byte[bufferSize];
                    int encodeBufferSize = 1024 * 10;
                    byte[] encodeBuffer = new byte[encodeBufferSize];
                    int len = -1;
                    while ((len=fileInputStream.read(buffer))>0){
                        Log.d(TAG,"read len="+len);
                        int offset=0;
                        while (offset<len){
                            int encodeBufferLenth = Math.min(len-offset,encodeBufferSize);
                            System.arraycopy(buffer,offset,encodeBuffer,0,encodeBufferLenth);
                            audioEncoder.fireAudio(encodeBuffer,encodeBufferLenth);
                            offset+=encodeBufferLenth;
                        }
                    }
                }catch (Exception e){
                    Log.d(TAG,"e="+e.getLocalizedMessage());
                }finally {
                    if (null != fileInputStream) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(null != fileOutputStream) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                audioEncoder.stop();
                int wasteTimeMills = (int)(System.currentTimeMillis() - startTimeMills);
                Log.i(TAG, "wasteTimeMills is : " + wasteTimeMills);

            }
        }).start();
    }

    @Override
    public void outputAACPacket(byte[] data) {
        try {
            fileOutputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}