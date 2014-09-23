package com.bl.barcode;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.bravolucy.forecast.R;
import net.sourceforge.zbar.*;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;

import java.io.IOException;

/**
 * Created by jatinpuri on 2/9/14.
 */
public class ZbarPrac {
    private static final String TAG = "Zbar";
    private final CordovaInterface cordova;
    private SurfaceHolder mHolder;
    private final Context context;
    private CallbackContext sendBarcode;
    private boolean freeze = false;
    private Camera.PreviewCallback previewCallback;


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
    ScrollView beautyContent;


    private boolean previewing = true;

    static {
        System.loadLibrary("iconv");
    }

    public void setup(CallbackContext sendBarcode, final int y) {
        this.sendBarcode = sendBarcode;
        final LayoutInflater inflator = cordova.getActivity().getLayoutInflater();
        final LinearLayout inflate = (LinearLayout) inflator.inflate(R.layout.main2, null);

        final SurfaceView surfaceView = (SurfaceView) inflate.findViewById(R.id.surfaceViewBeautyCamera);
        mHolder = surfaceView.getHolder();

        WindowManager mW = (WindowManager) cordova.getActivity().getSystemService(Context.WINDOW_SERVICE);
        final int screenWidth = mW.getDefaultDisplay().getWidth();
        int screenHeight = mW.getDefaultDisplay().getHeight();

        final ViewGroup.LayoutParams slayout = surfaceView.getLayoutParams();
        slayout.width = screenWidth;
        slayout.height = screenHeight;
        surfaceView.requestLayout();


        final ScrollView beautyContent = (ScrollView) inflate.findViewById(R.id.scrollView);
        final ViewGroup.LayoutParams newParams = beautyContent.getLayoutParams();
        final int barcodeScannerHeight = (int) convertDpToPixel(125f, cordova.getActivity());
        newParams.height = barcodeScannerHeight;
        beautyContent.requestLayout();
        previewCallback = getPreviewCallback(0, 0, barcodeScannerHeight, screenWidth);
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCamera = getCameraInstance();
                mHolder.addCallback(new CameraPreview(cordova.getActivity(), mCamera, previewCallback, autoFocusCB, cordova.getActivity()));
            }
        });

        autoFocusHandler = new Handler();
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        scanText = (TextView) inflate.findViewById(R.id.scanText);

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        inflate.setPadding(0, y, 0, 0);
        Toast.makeText(cordova.getActivity(), "Y given is: " + y + ". Height: " + params.height, Toast.LENGTH_LONG);
        LOG.e(TAG, "Y given is: " + y + ". Height: " + params.height);
        this.beautyContent = beautyContent;
        previewing = true;
        cordova.getActivity().addContentView(inflate, params);
        //cordova.getActivity().getWindow().g
    }

    public boolean unfreeze() {
        if (!freeze || previewCallback == null) {
            LOG.e("Zbar", "Not frozen yet");
            return false;
        }
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCamera.startPreview();
                mCamera.setPreviewCallback(previewCallback);
            }
        });
        return true;

    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            LOG.e(TAG, e.getMessage());
        }
        return c;
    }

    public void releaseCamera() {
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.setPreviewCallback(null);
        mCamera.release();
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    private Camera.PreviewCallback getPreviewCallback(final int left, final int top, final int width, final int height) {
        return new Camera.PreviewCallback() {

            long lastScanned = 0;

            public void onPreviewFrame(byte[] data, Camera camera) {

                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPreviewSize();

                Image barcode = new Image(size.width, size.height, "Y800");
                barcode.setData(data);
                /**
                 * http://stackoverflow.com/questions/18918211/how-to-change-area-of-scan-zbar
                 * below parameters are taken from above link
                 */
                Log.e("Crop", "Cropping: left(" + left + ") top(" + top + ") width(" + width + ") height(" + height + ")");
                barcode.setCrop(Math.abs(left), top, width, height);
                int result = scanner.scanImage(barcode);

                if (result != 0) {
                    SymbolSet syms = scanner.getResults();
                    for (Symbol sym : syms) {
                        // scanText.setText("barcode result " + sym.getData());
                        String barcodeS = sym.getData();
                        LOG.e("ZbarPrac", "Barcode detected: " + barcodeS);
                        if (System.currentTimeMillis() - lastScanned <= 1500) {
                            continue;
                        }
                        PluginResult ans = new PluginResult(PluginResult.Status.OK, barcodeS);
                        ans.setKeepCallback(true);
                        sendBarcode.sendPluginResult(ans);
                        lastScanned = System.currentTimeMillis();
                        cordova.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCamera.setPreviewCallback(null);
                                mCamera.stopPreview();
                                freeze = true;
                            }
                        });
                        break;
                    }
                }
            }

        };
    }

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };


    public void stop() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                previewing = false;
                releaseCamera();
                final ViewGroup parent = (ViewGroup) beautyContent.getParent();
                parent.removeView(beautyContent);
            }
        });

    }

    /* This method converts dp unit to equivalent pixels, depending on device density.
    *
            * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
    * @param context Context to get resources and device specific display metrics
    * @return A float value to represent px equivalent to dp depending on device density
    */
    public float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /* This method converts dp unit to equivalent pixels, depending on device density.
    *
            * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
    * @param context Context to get resources and device specific display metrics
    * @return A float value to represent px equivalent to dp depending on device density
    */
    public int convertPixelToDp(float pixel, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = pixel * (160 / metrics.densityDpi);
        return (int) px;
    }


}
