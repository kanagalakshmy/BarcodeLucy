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



        previewCallback = previewCb;
        autoFocusCallback = autoFocusCb;
        this.mCamera = camera;

        this.activity = activity;
    }


    public void surfaceCreated(SurfaceHolder holder) {


        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        Log.e("DBG", "Surface Created");

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
                    Log.e("DBG", "Error stopping camera preview: " + e.getMessage());
                }

                try {
                    // Hard code camera surface rotation 90 degs to match Activity view in portrait
                    mCamera.setDisplayOrientation(90);
                    mCamera.setPreviewDisplay(holder);
                    mCamera.setPreviewCallback(previewCallback);
                    mCamera.startPreview();
                    mCamera.autoFocus(autoFocusCallback);
                } catch (Exception e) {
                    Log.e("DBG", "Error starting camera preview: " + e.getMessage());
                }
            }
        });
    }

}
