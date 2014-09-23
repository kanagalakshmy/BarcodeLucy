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

    private static final String TAG = "BarcodeLucy";

    ZbarPrac prac;

    public BarcodeLucy() {
        super();

    }


    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArray of arguments for the plugin.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return True when the action was valid, false otherwise.
     */
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        LOG.e(TAG, "call: " + action+" args: "+args);

        if (this.cordova.getActivity().isFinishing()) return true;
        else if (action.equals("startCamera")) {
            if (prac != null) {
                callbackContext.error("Camera already running");
                return false;
            }
            try {
                final int width = (int) convertDpToPixel(args.getInt(0), cordova.getActivity());
                final int height = (int) convertDpToPixel(args.getInt(1), cordova.getActivity());
                final int x = (int) convertDpToPixel(args.getInt(2), cordova.getActivity());
                final int y = (int) convertDpToPixel(args.getInt(3), cordova.getActivity());
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        prac = new ZbarPrac(cordova.getActivity(), cordova);
                        prac.setup(callbackContext,y);
                    }
                });
            } catch (JSONException e) {
                LOG.e(TAG, e.getLocalizedMessage());
                callbackContext.error("Camera not running");
                return false;
            }
        } else if (action.equals("stopCamera")) {
            if (prac == null) {
                callbackContext.error("Zbar not yet started");
                return false;
            }
            prac.stop();
            prac = null;
        }  else if (action.equals("unfreeze")) {
            if (prac == null) {
                callbackContext.error("Zbar not yet started");
                return false;
            }
            prac.unfreeze();

        } else {
            LOG.e(TAG, "Stopping scannning");
        }
        return true;
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
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



}
