package com.bl.barcode;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.widget.Toast;
import org.apache.cordova.*;
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
    public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) throws JSONException {
        Toast.makeText(cordova.getActivity(), "Notification. call: " + action + " args: " + args, Toast.LENGTH_LONG).show();
        LOG.e("Notification", "call: " + action);

    	/*
         * Don't run any of these if the current activity is finishing
    	 * in order to avoid android.view.WindowManager$BadTokenException
    	 * crashing the app. Just return true here since false should only
    	 * be returned in the event of an invalid action.
    	 */
        if (this.cordova.getActivity().isFinishing()) return true;
        else if (action.equals("startCamera")) {
            if (scan != null) {
                callbackContext.error("Camera already running");
                return false;
            }
            try {
                final int width = (int) convertDpToPixel(args.getInt(0),cordova.getActivity());
                final int height = (int) convertDpToPixel(args.getInt(1),cordova.getActivity());
                final int x = (int) convertDpToPixel(args.getInt(2),cordova.getActivity());
                final int y = (int) convertDpToPixel(args.getInt(3),cordova.getActivity());
                Toast.makeText(cordova.getActivity(), width+" "+height+" "+x+" "+y, Toast.LENGTH_LONG).show();
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scan = new ScanditPrac(cordova.getActivity(), cordova);
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
        } else if (action.equals("alert")) {
            this.alert(args.getString(0), args.getString(1), args.getString(2), callbackContext);
        } else {
            Toast.makeText(cordova.getActivity(), "Stopping scannning", Toast.LENGTH_LONG).show();
        }

        // Only alert and confirm are async.
        callbackContext.success();
        return true;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * Builds and shows a native Android alert with given Strings
     *
     * @param message         The message the alert should display
     * @param title           The title of the alert
     * @param buttonLabel     The label of the button
     * @param callbackContext The callback context
     */
    public synchronized void alert(final String message, final String title, final String buttonLabel, final CallbackContext callbackContext) {
        final CordovaInterface cordova = this.cordova;

        Runnable runnable = new Runnable() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void run() {

                AlertDialog.Builder dlg = new AlertDialog.Builder(cordova.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                dlg.setMessage(message);
                dlg.setTitle(title);
                dlg.setCancelable(true);
                dlg.setPositiveButton(buttonLabel,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 0));
                            }
                        });
                dlg.setOnCancelListener(new AlertDialog.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 0));
                    }
                });

                dlg.create();
                dlg.show();
            }

            ;
        };
        this.cordova.getActivity().runOnUiThread(runnable);
    }


}
