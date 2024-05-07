package by.chemerisuk.cordova;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WebSharePlugin extends CordovaPlugin {
    private static final int SHARE_REQUEST_CODE = 18457896;

    private CallbackContext shareCallbackContext;
    private PendingIntent chosenComponentPI;

    @Override
    protected void pluginInitialize() {
        Context context = cordova.getActivity().getApplicationContext();
        chosenComponentPI = PendingIntent.getBroadcast(context,
                SHARE_REQUEST_CODE + 1,
                new Intent(context, WebShareReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if ("share".equals(action)) {
            share(args, callbackContext);
            return true;
        }
        return false;
    }

    private void share(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        JSONObject options = args.getJSONObject(0);
        String text = options.optString("text");
        String title = options.optString("title");
        String url = options.optString("url");
        if (!url.isEmpty()) {
            text = text.isEmpty() ? url : text + "\n" + url;
        }

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        if (!title.isEmpty()) {
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        }
        if (chosenComponentPI != null) {
            WebShareReceiver.resetChosenComponent();
            sendIntent = Intent.createChooser(sendIntent, title, chosenComponentPI.getIntentSender());
        } else {
            sendIntent = Intent.createChooser(sendIntent, title);
        }

        cordova.getActivity().startActivityForResult(sendIntent, SHARE_REQUEST_CODE);
        shareCallbackContext = callbackContext;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SHARE_REQUEST_CODE && shareCallbackContext != null) {
            JSONArray packageNames = new JSONArray();
            if (resultCode == Activity.RESULT_OK) {
                packageNames.put(WebShareReceiver.getChosenComponentPackage());
            }
            shareCallbackContext.success(packageNames);
            shareCallbackContext = null;
        }
    }
}
