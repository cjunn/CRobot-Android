package com.crobot.core.infra.tool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.OrientationEventListener;

import androidx.annotation.RequiresApi;

import com.crobot.core.infra.InfraError;

import java.util.concurrent.atomic.AtomicReference;

public class ScreenCaptureImpl implements ScreenCapture {

    public static final int ORIENTATION_AUTO = Configuration.ORIENTATION_UNDEFINED;
    private static final String LOG_TAG = "ScreenCapture";
    private final MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private volatile Looper mImageAcquireLooper;
    private volatile Image mUnderUsingImage;
    private volatile AtomicReference<Image> mCachedImage = new AtomicReference<>();
    private volatile Exception mException;
    private Intent mData;
    private Context mContext;
    private int mOrientation = -1;
    private int mDetectedOrientation;
    private OrientationEventListener mOrientationEventListener;

    private ScreenMetrics screenMetrics;

    public ScreenCaptureImpl(Context context, Intent data, int orientation, ScreenMetrics screenMetrics) {
        this.mContext = context;
        this.mData = data;
        this.screenMetrics = screenMetrics;
        this.mProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        this.mMediaProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) mData.clone());
        setOrientation(orientation);
        observeOrientation();
    }

    private void observeOrientation() {
        mOrientationEventListener = new OrientationEventListener(mContext) {
            @Override
            public void onOrientationChanged(int o) {
                int orientation = mContext.getResources().getConfiguration().orientation;
                if (mOrientation == ORIENTATION_AUTO && mDetectedOrientation != orientation) {
                    mDetectedOrientation = orientation;
                    try {
                        refreshVirtualDisplay(orientation);
                    } catch (Exception e) {
                        e.printStackTrace();
                        mException = e;
                    }
                }
            }
        };
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    public void setOrientation(int orientation) {
        if (mOrientation == orientation)
            return;
        mOrientation = orientation;
        mDetectedOrientation = mContext.getResources().getConfiguration().orientation;
        refreshVirtualDisplay(mOrientation == ORIENTATION_AUTO ? mDetectedOrientation : mOrientation);
    }


    private void refreshVirtualDisplay(int orientation) {
        if (mImageAcquireLooper != null) {
            mImageAcquireLooper.quit();
        }
        if (mImageReader != null) {
            mImageReader.close();
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
        mMediaProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) mData.clone());
        int screenHeight = screenMetrics.getHeight(orientation);
        int screenWidth = screenMetrics.getWidth(orientation);
        int densityDpi = screenMetrics.getDensityDpi();
        initVirtualDisplay(screenWidth, screenHeight, densityDpi);
        startAcquireImageLoop();
    }

    @SuppressLint("WrongConstant")
    private void initVirtualDisplay(int width, int height, int screenDensity) {
        mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 3);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(LOG_TAG,
                width, height, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    private void startAcquireImageLoop() {
        new Thread(() -> {
            Log.d(LOG_TAG, "AcquireImageLoop: start");
            Looper.prepare();
            mImageAcquireLooper = Looper.myLooper();
            setImageListener(new Handler());
            Looper.loop();
            Log.d(LOG_TAG, "AcquireImageLoop: stop");
        }).start();
    }

    private void setImageListener(Handler handler) {
        mImageReader.setOnImageAvailableListener(reader -> {
            try {
                Image oldCacheImage = mCachedImage.getAndSet(null);
                if (oldCacheImage != null) {
                    oldCacheImage.close();
                }
                mCachedImage.set(reader.acquireLatestImage());
            } catch (Exception e) {
                mException = e;
            }

        }, handler);
    }


    @Override
    public Image capture() {
        Exception e = mException;
        if (e != null) {
            mException = null;
            throw new InfraError(e);
        }
        Thread thread = Thread.currentThread();
        while (!thread.isInterrupted()) {
            Image cachedImage = mCachedImage.getAndSet(null);
            if (cachedImage != null) {
                if (mUnderUsingImage != null) {
                    mUnderUsingImage.close();
                }
                mUnderUsingImage = cachedImage;
                return cachedImage;
            }
        }
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void release() {
        if (mImageAcquireLooper != null) {
            mImageAcquireLooper.quit();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mImageReader != null) {
            mImageReader.close();
        }
        if (mUnderUsingImage != null) {
            mUnderUsingImage.close();
        }
        Image cachedImage = mCachedImage.getAndSet(null);
        if (cachedImage != null) {
            cachedImage.close();
        }
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            release();
        } finally {
            super.finalize();
        }
    }
}
