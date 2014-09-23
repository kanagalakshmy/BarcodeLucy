/*
 * Barebones implementation of displaying camera preview.
 * 
 * Created by lisah0 on 2012-02-24
 */
package com.bl.barcode;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * A basic Camera preview class
 */
public class CameraPreview implements SurfaceHolder.Callback {

    private Camera mCamera;
    private PreviewCallback previewCallback;
    private AutoFocusCallback autoFocusCallback;
    private Activity activity;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public CameraPreview(Context context, Camera camera,
                         PreviewCallback previewCb,
                         AutoFocusCallback autoFocusCb, Activity activity) {


//        final Parameters parameters = mCamera.getParameters();
//        for(Camera.Size s : parameters.getSupportedPictureSizes()){
//            Log.w("zbar-count"," "+s.width+", "+s.height);
//        }
//        WindowManager mW = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
//        int screenWidth = mW.getDefaultDisplay().getWidth();
//        int screenHeight = mW.getDefaultDisplay().getHeight();
//
//        mCamera.setParameters(parameters);
//
//        layout(0,0,screenWidth, 100);
        //setPadding(100,100,100,100);
        //parameters.set
        previewCallback = previewCb;
        autoFocusCallback = autoFocusCb;
        this.mCamera = camera;


        /* 
         * Set camera to continuous focus if supported, otherwise use
         * software auto-focus. Only works for API level >=9.
         */
        /*
        Camera.Parameters parameters = camera.getParameters();
        for (String f : parameters.getSupportedFocusModes()) {
            if (f == Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) {
                mCamera.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                autoFocusCallback = null;
                break;
            }
        }
        */

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        this.activity = activity;
    }


    public void surfaceCreated(SurfaceHolder holder) {

//        mHolder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        Log.e("DBG", "Surface Created");
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            //mCamera =  getCameraInstance();
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

        } catch (IOException e) {
            Log.e("DBG", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Camera preview released in activity
    }

    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("DBG", "Surface Changed");

                // stop preview before making changes
                try {
                    mCamera.stopPreview();
                } catch (Exception e) {
                    // ignore: tried to stop a non-existent preview
                }

                try {
                    // Hard code camera surface rotation 90 degs to match Activity view in portrait
                    mCamera.setDisplayOrientation(90);
                    mCamera.setPreviewDisplay(holder);
                    mCamera.setPreviewCallback(previewCallback);
                    mCamera.startPreview();
                    mCamera.autoFocus(autoFocusCallback);
                } catch (Exception e) {
                    Log.d("DBG", "Error starting camera preview: " + e.getMessage());
                }
            }
        });
    }

}
