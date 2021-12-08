package com.oney.WebRTCModule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.webrtc.*;

public class DummyVideoCapturer implements VideoCapturer {

    private SurfaceTextureHelper surTexture;
    private Context appContext;
    private org.webrtc.CapturerObserver capturerObs;
    private Thread captureThread;

    @Override
    public void initialize(SurfaceTextureHelper surfaceTextureHelper, Context applicationContext, org.webrtc.CapturerObserver capturerObserver) {
        surTexture = surfaceTextureHelper;
        appContext = applicationContext;
        capturerObs = capturerObserver;
    }

    @Override
    public void startCapture(int width, int height, int fps) {
        captureThread = new Thread(() -> {
            try {
                long start = System.nanoTime();
                capturerObs.onCapturerStarted(true);

                while (true) {
                    JavaI420Buffer buffer = JavaI420Buffer.allocate(width, height);

                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawRGB(255, 0, 0);

                    bitmapToI420(bitmap, buffer);

                    long frameTime = System.nanoTime() - start;
                    VideoFrame videoFrame = new VideoFrame(buffer, 0, frameTime);
                    capturerObs.onFrameCaptured(videoFrame);

                    Thread.sleep(1000 / fps);
                }
            } catch(InterruptedException ex) {
                ex.printStackTrace();
            }
        });
        captureThread.start();
    }

    @Override
    public void stopCapture() {
        captureThread.interrupt();
    }

    @Override
    public void changeCaptureFormat(int width, int height, int fps) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean isScreencast() {
        return false;
    }

    private static void bitmapToI420(Bitmap src, JavaI420Buffer dest) {
        int width = src.getWidth();
        int height = src.getHeight();

        if(width != dest.getWidth() || height != dest.getHeight())
            return;

        int strideY = dest.getStrideY();
        int strideU = dest.getStrideU();
        int strideV = dest.getStrideV();
        ByteBuffer dataY = dest.getDataY();
        ByteBuffer dataU = dest.getDataU();
        ByteBuffer dataV = dest.getDataV();

        for(int line = 0; line < height; line++) {
            if(line % 2 == 0) {
                for (int x = 0; x < width; x += 2) {
                    int px = src.getPixel(x, line);
                    byte r = (byte) ((px >> 16) & 0xff);
                    byte g = (byte) ((px >> 8) & 0xff);
                    byte b = (byte) (px & 0xff);

                    dataY.put(line * strideY + x, (byte) (((66 * r + 129 * g + 25 * b) >> 8) + 16));
                    dataU.put(line / 2 * strideU + x / 2, (byte) (((-38 * r + -74 * g + 112 * b) >> 8) + 128));
                    dataV.put(line / 2 * strideV + x / 2, (byte) (((112 * r + -94 * g + -18 * b) >> 8) + 128));

                    px = src.getPixel(x + 1, line);
                    r = (byte) ((px >> 16) & 0xff);
                    g = (byte) ((px >> 8) & 0xff);
                    b = (byte) (px & 0xff);

                    dataY.put(line * strideY + x, (byte) (((66 * r + 129 * g + 25 * b) >> 8) + 16));
                }
            } else {
                for (int x = 0; x < width; x += 1) {
                    int px = src.getPixel(x, line);
                    byte r = (byte) ((px >> 16) & 0xff);
                    byte g = (byte) ((px >> 8) & 0xff);
                    byte b = (byte) (px & 0xff);

                    dataY.put(line * strideY + x, (byte) (((66 * r + 129 * g + 25 * b) >> 8) + 16));
                }
            }
        }
    }

}