package com.bl.barcode;

import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.mirasense.scanditsdk.ScanditSDKBarcodePicker;
import com.mirasense.scanditsdk.interfaces.ScanditSDK;
import com.mirasense.scanditsdk.interfaces.ScanditSDKListener;
import org.apache.cordova.CordovaInterface;

/**
 *
 * Created by jatinpuri on 20/8/14.
 */
public class ScanditPrac extends SurfaceView implements SurfaceHolder.Callback, ScanditSDKListener {

    private final CordovaInterface cordova;
    private ScanditSDKBarcodePicker mPicker;
    private SurfaceHolder mholder;
    private final Context context;

    public ScanditPrac(Context context, CordovaInterface cordova) {
        super(context);
        this.context = context;
        this.cordova = cordova;
    }

    public void start(int width, int height, int x, int y){
        mPicker = new ScanditSDKBarcodePicker(context, "BBmApO+iEeOUGrokEE0/aMNgbCD+2kUCS1K`fhesj8P4");
        mPicker.getOverlayView().addListener(this);
        this.mholder = getHolder();
        mholder.addCallback(this);

//        //rParams
//        LinearLayout linearLayout = new LinearLayout(cordova.getActivity());
//        linearLayout.setPadding(x, y, 0, 0);
//        linearLayout.addView(mPicker);
//        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        cordova.getActivity().addContentView(linearLayout, rParams);
        WindowManager mW = (WindowManager)cordova.getActivity().getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = mW.getDefaultDisplay().getWidth();
        int screenHeight = mW.getDefaultDisplay().getHeight();
        Toast.makeText(context,"Hey-----"+width+" "+height+" "+x+" "+y+" sw "+screenWidth+" "+screenHeight,Toast.LENGTH_LONG).show();

        int ad_width = screenWidth;
        int ad_height = height + y;
        // Resize display ad if it's too big.
//        if (screenWidth < ad_width){
//            int desired_width = screenWidth;
//            int scale;
//            Double val = Double.valueOf(desired_width) / Double.valueOf(ad_width);
//            val = val * 100d;
//            scale = val.intValue();
//            // Resize display ad to desired width and keep aspect ratio.
//            ViewGroup.LayoutParams layout = new ViewGroup.LayoutParams(desired_width, (desired_width*ad_height)/ad_width);
//            mPicker.setLayoutParams(layout);
//        }

        LinearLayout linearLayout = new LinearLayout(cordova.getActivity());
        linearLayout.setGravity(Gravity.FILL);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setPadding(x, y, 0, 0);
        linearLayout.addView(mPicker);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth, ad_height);
        params.gravity = Gravity.FILL;
        cordova.getActivity().getWindow().addContentView(linearLayout, params);
        mPicker.startScanning();
    }

    public void stop(){
        try {
            mPicker.stopScanning();
            Toast.makeText(context, "Surface Destroyed", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.d(TAG, "Error stopping camera preview: " + e.getMessage());
        }
    }


    private final String TAG = "Scandit";

    @Override
    public void didCancel() {
    }

    @Override
    public void didScanBarcode(String s, String s1) {
        Toast.makeText(context, "Scanned Barcode: " + s + " " + s1, Toast.LENGTH_LONG).show();
        Log.i(TAG, "Did scan barcode : " + s + " " + s1);
    }

    @Override
    public void didManualSearch(String s) {
        Toast.makeText(context, "Did manual search: " + s, Toast.LENGTH_LONG).show();
        Log.i(TAG, "This callback is called when you use the Scandit SDK search bar. : " + s);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "Surface Created");
        mPicker.startScanning();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "Surface Changed");
        if (mholder.getSurface() == null) {
            return;
        }
        try {
            mPicker.stopScanning();
        } catch (Exception e) {
            Log.d(TAG, "Error stopping camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG,"Surface Destroyed");
        stop();
    }
}
