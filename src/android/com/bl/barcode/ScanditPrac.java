package com.bl.barcode;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.mirasense.scanditsdk.ScanditSDKBarcodePicker;
import com.mirasense.scanditsdk.interfaces.ScanditSDK;
import com.mirasense.scanditsdk.interfaces.ScanditSDKListener;
import com.mirasense.scanditsdk.interfaces.ScanditSDKOverlay;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.LOG;

/**
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

    public void start(int width, int height, int x, int y) {
        mPicker = new ScanditSDKBarcodePicker(context, "BBmApO+iEeOUGrokEE0/aMNgbCD+2kUCS1K`fhesj8P4");
        mPicker.getOverlayView().addListener(this);
        setSettingsForPicker(context, mPicker);
        this.mholder = getHolder();
        mholder.addCallback(this);

//        //rParams
//        LinearLayout linearLayout = new LinearLayout(cordova.getActivity());
//        linearLayout.setPadding(x, y, 0, 0);
//        linearLayout.addView(mPicker);
//        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        cordova.getActivity().addContentView(linearLayout, rParams);
        WindowManager mW = (WindowManager) cordova.getActivity().getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = mW.getDefaultDisplay().getWidth();
        int screenHeight = mW.getDefaultDisplay().getHeight();
        Toast.makeText(context, "Hey-----" + width + " " + height + " " + x + " " + y + " sw " + screenWidth + " " + screenHeight, Toast.LENGTH_LONG).show();

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
        linearLayout.addView(mPicker, LinearLayout.LayoutParams.MATCH_PARENT, ad_height);

        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (screenWidth), ad_height);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.bottomMargin = 20;
        cordova.getActivity().getWindow().addContentView(linearLayout, params);
        this.linearLayout = linearLayout;
        mPicker.setScanningHotSpot(0.5f, y / (height * 1f));
        mPicker.getOverlayView().setInfoBannerY(0.4f);
        mPicker.startScanning();
    }

    private LinearLayout linearLayout;


    public void stop() {
        LOG.e("TAG", "stop called");
        try {
            if (linearLayout != null) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPicker.stopScanning();
                        final ViewGroup parent = (ViewGroup) linearLayout.getParent();
                        parent.removeView(linearLayout);
                        Toast.makeText(context, "Surface Destroyed", Toast.LENGTH_LONG).show();
                        linearLayout = null;
                    }
                });
            }else{
                LOG.i("TAG","Layout is null to remove it");
            }
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
        Log.e(TAG, "Surface Destroyed");
        stop();

    }

    public static void setSettingsForPicker(Context context, ScanditSDK picker) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // if (prefs.getBoolean("ignore_preview_aspect_ratio", false)) {
        picker.ignorePreviewAspectRatio();
//        }
        picker.setEan13AndUpc12Enabled(prefs.getBoolean("ean13_and_upc12_enabled", true));
        picker.setEan8Enabled(prefs.getBoolean("ean8_enabled", true));
        picker.setUpceEnabled(prefs.getBoolean("upce_enabled", true));
        picker.setCode39Enabled(prefs.getBoolean("code39_enabled", true));
        picker.setCode93Enabled(prefs.getBoolean("code93_enabled", false));
        picker.setCode128Enabled(prefs.getBoolean("code128_enabled", true));
        picker.setItfEnabled(prefs.getBoolean("itf_enabled", true));
        picker.setMsiPlesseyEnabled(prefs.getBoolean("msi_plessey_enabled", false));
        picker.setGS1DataBarEnabled(prefs.getBoolean("databar_enabled", false));
        picker.setGS1DataBarExpandedEnabled(prefs.getBoolean("databar_expanded_enabled", false));
        picker.setCodabarEnabled(prefs.getBoolean("codabar_enabled", false));

        int msiPlesseyChecksum = Integer.valueOf(prefs.getString("msi_plessey_checksum", "1"));
        int actualChecksum = ScanditSDK.CHECKSUM_MOD_10;
        if (msiPlesseyChecksum == 0) {
            actualChecksum = ScanditSDK.CHECKSUM_NONE;
        } else if (msiPlesseyChecksum == 2) {
            actualChecksum = ScanditSDK.CHECKSUM_MOD_11;
        } else if (msiPlesseyChecksum == 3) {
            actualChecksum = ScanditSDK.CHECKSUM_MOD_1010;
        } else if (msiPlesseyChecksum == 4) {
            actualChecksum = ScanditSDK.CHECKSUM_MOD_1110;
        }
        picker.setMsiPlesseyChecksumType(actualChecksum);

        picker.setQrEnabled(prefs.getBoolean("qr_enabled", true));
        picker.setDataMatrixEnabled(prefs.getBoolean("data_matrix_enabled", true));
        if (prefs.getBoolean("data_matrix_enabled", true)) {
            picker.setMicroDataMatrixEnabled(prefs.getBoolean("micro_data_matrix_enabled", false));
            picker.setInverseRecognitionEnabled(prefs.getBoolean("inverse_recognition", false));
        }
        picker.setPdf417Enabled(prefs.getBoolean("pdf417_enabled", false));

        picker.setScanningHotSpot(prefs.getInt("hot_spot_x", 50) / 100.0f,
                prefs.getInt("hot_spot_y", 50) / 100.0f);
        picker.restrictActiveScanningArea(prefs.getBoolean("restrict_scanning_area", false));
        picker.setScanningHotSpotHeight(prefs.getInt("hot_spot_height", 25) / 100.0f);
        picker.getOverlayView().drawViewfinder(prefs.getBoolean("draw_viewfinder", true));
        picker.getOverlayView().setViewfinderDimension(
                prefs.getInt("viewfinder_width", 70) / 100.0f,
                prefs.getInt("viewfinder_height", 30) / 100.0f,
                prefs.getInt("viewfinder_landscape_width", 40) / 100.0f,
                prefs.getInt("viewfinder_landscape_height", 30) / 100.0f);

        picker.getOverlayView().setBeepEnabled(prefs.getBoolean("beep_enabled", true));
        picker.getOverlayView().setVibrateEnabled(prefs.getBoolean("vibrate_enabled", false));

        picker.getOverlayView().showSearchBar(prefs.getBoolean("search_bar", false));
        picker.getOverlayView().setSearchBarPlaceholderText(prefs.getString(
                "search_bar_placeholder", "Scan barcode or enter it here"));

        picker.getOverlayView().setTorchEnabled(prefs.getBoolean("torch_enabled", true));
        picker.getOverlayView().setTorchButtonPosition(
                prefs.getInt("torch_button_x", 5) / 100.0f, prefs.getInt("torch_button_y", 1) / 100.0f, 67, 33);

        int cameraSwitch = Integer.valueOf(prefs.getString("camera_switch_visibility", "0"));
        int cameraSwitchVisibility = ScanditSDKOverlay.CAMERA_SWITCH_NEVER;
        if (cameraSwitch == 1) {
            cameraSwitchVisibility = ScanditSDKOverlay.CAMERA_SWITCH_ON_TABLET;
        } else if (cameraSwitch == 2) {
            cameraSwitchVisibility = ScanditSDKOverlay.CAMERA_SWITCH_ALWAYS;
        }
        picker.getOverlayView().setCameraSwitchVisibility(cameraSwitchVisibility);
        picker.getOverlayView().setCameraSwitchButtonPosition(
                prefs.getInt("camera_switch_button_x", 5) / 100.0f, prefs.getInt("camera_switch_button_y", 1) / 100.0f, 67, 33);
    }


}

