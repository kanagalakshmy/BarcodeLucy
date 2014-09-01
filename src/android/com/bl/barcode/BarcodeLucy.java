package com.bl.barcode;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.Toast;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class provides access to notifications on the device.
 * <p/>
 * Be aware that this implementation gets called on
 * navigator.notification.{alert|confirm|prompt}, and that there is a separate
 * implementation in org.apache.cordova.CordovaChromeClient that gets
 * called on a simple window.{alert|confirm|prompt}.
 */
public class BarcodeLucy extends CordovaPlugin {

    private static final String TAG = "NOTIFICATION";

    public BarcodeLucy() {
        super();

    }

    private ScanditPrac scan;

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArray of arguments for the plugin.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return True when the action was valid, false otherwise.
     */
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Toast.makeText(cordova.getActivity(), "Notification. call: " + action + " args: " + args, Toast.LENGTH_LONG).show();
        LOG.e("Notification", "call: " + action);

        if (this.cordova.getActivity().isFinishing()) return true;
        else if (action.equals("startCamera")) {
            if (scan != null) {
                callbackContext.error("Camera already running");
                return false;
            }
            try {
                final int width = (int) convertDpToPixel(args.getInt(0), cordova.getActivity());
                final int height = (int) convertDpToPixel(args.getInt(1), cordova.getActivity());
                final int x = (int) convertDpToPixel(args.getInt(2), cordova.getActivity());
                final int y = (int) convertDpToPixel(args.getInt(3), cordova.getActivity());
                Toast.makeText(cordova.getActivity(), width + " " + height + " " + x + " " + y, Toast.LENGTH_LONG).show();
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scan = new ScanditPrac(cordova.getActivity(), cordova, callbackContext);
                        scan.start(width, height, x, y);
                    }
                });
            } catch (JSONException e) {
                LOG.e(TAG, e.getLocalizedMessage());
                Toast.makeText(cordova.getActivity(), "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
                callbackContext.error("Camera not running");
                return false;
            }
        } else if (action.equals("stopCamera")) {
            if (scan == null) {
                callbackContext.error("Camera not running");
                return false;
            }
            scan.stop();
            scan = null;
        } else {
            Toast.makeText(cordova.getActivity(), "Stopping scannning", Toast.LENGTH_LONG).show();
        }
        return true;
    }


    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
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

}
