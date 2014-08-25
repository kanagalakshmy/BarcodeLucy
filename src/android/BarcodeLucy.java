package org.apache.cordova.dialogs;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

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

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArray of arguments for the plugin.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return True when the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Toast.makeText(cordova.getActivity(),"Notification. call: "+action, Toast.LENGTH_LONG).show();
        LOG.e("Notification", "call: " + action);

    	/*
         * Don't run any of these if the current activity is finishing
    	 * in order to avoid android.view.WindowManager$BadTokenException
    	 * crashing the app. Just return true here since false should only
    	 * be returned in the event of an invalid action.
    	 */
        if (this.cordova.getActivity().isFinishing()) return true;
        else if (action.equals("alert")) {
            this.alert(args.getString(0), args.getString(1), args.getString(2), callbackContext);
        } else {
            Toast.makeText(cordova.getActivity(),"Stopping scannning", Toast.LENGTH_LONG).show();
        }

        // Only alert and confirm are async.
        callbackContext.success();
        return true;
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
