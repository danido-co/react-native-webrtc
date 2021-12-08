package com.oney.WebRTCModule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;

import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.VideoCapturer;

public class DummyVideoCaptureController extends AbstractVideoCaptureController {
    /**
     * The {@link Log} tag with which {@code ScreenCaptureController} is to log.
     */
    private static final String TAG = ScreenCaptureController.class.getSimpleName();

    public DummyVideoCaptureController() {
        super(128, 128, 25);
    }

    @Override
    protected VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer = new DummyVideoCapturer();
        return videoCapturer;
    }
}
