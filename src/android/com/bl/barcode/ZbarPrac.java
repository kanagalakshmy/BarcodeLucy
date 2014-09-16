package com.bl.barcode;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.example.prac.R;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.LOG;

import java.io.IOException;

/**
 *
 * Created by jatinpuri on 2/9/14.
 */
public class ZbarPrac {
    private static final String TAG = "Zbar";
    private final CordovaInterface cordova;
    private SurfaceHolder mHolder;
    private final Context context;
    private CallbackContext sendBarcode;


    public ZbarPrac(Context context, CordovaInterface cordova) {
        //super(context);
        this.context = context;
        this.cordova = cordova;
    }

    private Camera mCamera;

    private Handler autoFocusHandler;

    TextView scanText;
    Button scanButton;

    ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    static {
        System.loadLibrary("iconv");
    }

    public void setup(CallbackContext sendBarcode) {
        this.sendBarcode = sendBarcode;

        final LayoutInflater inflator = cordova.getActivity().getLayoutInflater();
        final LinearLayout inflate = (LinearLayout) inflator.inflate(R.layout.main2, null);

        final SurfaceView surfaceView = (SurfaceView) inflate.findViewById(R.id.surfaceViewBeautyCamera);
        final SurfaceHolder holder = surfaceView.getHolder();

        WindowManager mW = (WindowManager) cordova.getActivity().getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = mW.getDefaultDisplay().getWidth();
        int screenHeight = mW.getDefaultDisplay().getHeight();

        final ViewGroup.LayoutParams slayout = surfaceView.getLayoutParams();
        slayout.width = screenWidth;
        slayout.height = screenHeight;
        surfaceView.requestLayout();


        final ScrollView beautyContent = (ScrollView) inflate.findViewById(R.id.scrollView);
        final ViewGroup.LayoutParams newParams = beautyContent.getLayoutParams();
        newParams.height = 180;
        beautyContent.requestLayout();

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    Camera camera = getCameraInstance();
                    if(camera == null){
                        releaseCamera();
                        camera = getCameraInstance();
                    }
                    camera.setPreviewDisplay(holder);
                    camera.setDisplayOrientation(90);
                    camera.startPreview();
                } catch (IOException e) {
                    Log.i("Lucy", e.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//                try {
//                    mCamera.stopPreview();
//                } catch (Exception e){
//                    Log.d("DBG", "Error setting camera destroyed: " + e.getMessage());
//                }
                releaseCamera();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseCamera();
            }
        });


       // inflate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 220));

//        final ScrollView beautyContent = (ScrollView) inflate.findViewById(R.id.scrollView);
//        final ViewGroup.LayoutParams newParams = beautyContent.getLayoutParams();
//        newParams.height = 180;
//        beautyContent.requestLayout();

        cordova.getActivity().addContentView(inflate, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

    }


    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
            LOG.e(TAG,e.getMessage());
        }
        return c;
    }

    public void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                Log.d("DBG", "Error setting camera destroyed: " + e.getMessage());
            }
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);
            int result = scanner.scanImage(barcode);

            if (result != 0) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    scanText.setText("barcode result " + sym.getData());
                    barcodeScanned = true;
                }
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };






}

//
//    public void setup() {
//
////        cordova.getActivity().setContentView(R.layout.scroll);
//
//        final LayoutInflater inflator = cordova.getActivity().getLayoutInflater();
//        final LinearLayout inflate = (LinearLayout) inflator.inflate(R.layout.scroll, null);
//
//
//        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//
//        /* Instance barcode scanner */
//
//
//        final SurfaceView surfaceView = (SurfaceView) inflate.findViewById(R.id.surfaceViewBeautyCamera);
//        final SurfaceHolder holder = surfaceView.getHolder();
//
//        WindowManager mW = (WindowManager) cordova.getActivity().getSystemService(Context.WINDOW_SERVICE);
//        int screenWidth = mW.getDefaultDisplay().getWidth();
//        int screenHeight = mW.getDefaultDisplay().getHeight();
//
//        final ViewGroup.LayoutParams slayout = surfaceView.getLayoutParams();
//        slayout.width = screenWidth;
//        slayout.height = screenHeight;
//        surfaceView.requestLayout();
//
//        final ScrollView beautyContent = (ScrollView) inflate.findViewById(R.id.scrollView);
//        final ViewGroup.LayoutParams newParams = beautyContent.getLayoutParams();
//        newParams.height = 180;
//        beautyContent.requestLayout();
//
//        SurfaceHolder mHolder = surfaceView.getHolder();
//        mHolder.addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                Toast.makeText(context, "Surface cReaeted", Toast.LENGTH_LONG).show();
//                try {
//                    if(mCamera == null){
//                        mCamera = getCameraInstance();
//                    }
//                    mCamera.setPreviewDisplay(holder);
//                    mCamera.setDisplayOrientation(90);
//                    mCamera.startPreview();
//                } catch (IOException e) {
//                    Log.d("DBG", "Error setting camera preview: " + e.getMessage());
//                }
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
////                try {
////                    mCamera.stopPreview();
////                } catch (Exception e){
////                    Log.d("DBG", "Error setting camera changed: " + e.getMessage());
////                }
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//                try {
//                    mCamera.stopPreview();
//                } catch (Exception e){
//                    Log.d("DBG", "Error setting camera destroyed: " + e.getMessage());
//                }
//                releaseCamera();
//            }
//        });
//
//        scanText = (TextView)inflate.findViewById(R.id.scanText);
//        cordova.getActivity().addContentView(inflate,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//
//
//        autoFocusHandler = new Handler();
//        mCamera = getCameraInstance();
//        if(mCamera == null){
//            throw new RuntimeException("Camera not available");
//        }
//        scanner = new ImageScanner();
//        scanner.setConfig(0, Config.X_DENSITY, 3);
//        scanner.setConfig(0, Config.Y_DENSITY, 3);
//
////
////        scanButton = (Button)findViewById(R.id.ScanButton);
////
////        scanButton.setOnClickListener(new OnClickListener() {
////            public void onClick(View v) {
////                if (barcodeScanned) {
////                    barcodeScanned = false;
////                    scanText.setText("Scanning...");
////                    mCamera.setPreviewCallback(previewCb);
////                    mCamera.startPreview();
////                    previewing = true;
////                    mCamera.autoFocus(autoFocusCB);
////                }
////            }
////        });
//    }
