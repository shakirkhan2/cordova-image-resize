package org.apache.cordova.resizer;

import java.io.FileNotFoundException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;

import org.apache.cordova.resizer.FileHelper;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.content.Context;
import android.provider.MediaStore.Images.Media;
import android.net.Uri;
import android.os.Environment;

public class ImgResizer extends CordovaPlugin {
  private String uri;
  private String folderName;
  private String fileName;
  private int quality;
  private int width;
  private int height;
  public CallbackContext callbackContext;

  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    try {
      this.callbackContext = callbackContext;

      if (action.equals("resize")) {
        JSONObject jsonObject = args.getJSONObject(0);
        uri = jsonObject.getString("uri");
        folderName = jsonObject.getString("folderName");
        fileName = jsonObject.getString("fileName");
        quality = jsonObject.getInt("quality");
        width = jsonObject.getInt("width");
        height = jsonObject.getInt("height");

        Bitmap bitmap = getScaledBitMapFromURI(uri, width, height);
        Uri scaledFile = saveFile(bitmap);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, scaledFile.toString()));
        return true;
      } else {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
        return false;
      }
    } catch(JSONException e) {
        Log.e("Resizer", "Expecting JSON!");
    }
    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
    return false;
  }

  /**
   * Loads a Bitmap of the given uri path
   *
   * @param uri URI of image
   * @return bitmap
   **/
  private Bitmap getScaledBitMapFromURI(String uriString, int width, int height) {
    try {
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(FileHelper.getInputStreamFromUriString(uriString, cordova), null, options);

      int[] retval = calculateAspectRatio(options.outWidth, options.outHeight);

      options.inJustDecodeBounds = false;
      options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, width, height);
      Bitmap unscaledBitmap = BitmapFactory.decodeStream(FileHelper.getInputStreamFromUriString(uriString, cordova), null, options);
      return Bitmap.createScaledBitmap(unscaledBitmap, retval[0], retval[1], true);
    } catch (FileNotFoundException e) {
      Log.e("Resizer", "File not found. :(");
    } catch (IOException e) {
      Log.e("Resizer", "IO Exception :(");
    } catch(Exception e) {
      Log.e("Resizer", e.toString());
    }
    return null;
  }

  private Uri saveFile(Bitmap bitmap) {
    File folder = new File(Environment.getExternalStorageDirectory() + "/" + folderName);
    boolean success = true;
    if (!folder.exists()) {
      success = folder.mkdir();
    }

    if (success) {
      if (fileName == null) {
        fileName = System.currentTimeMillis() + ".jpg";
      }
      File file = new File(folder, fileName);
      if (file.exists()) file.delete();
      try {
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        out.flush();
        out.close();
      } catch(Exception e) {
        Log.e("Resizer", e.toString());
      }
      return Uri.fromFile(file);
    }
    return null;
  }

  /**
   * Figure out what ratio we can load our image into memory at while still being bigger than
   * our desired width and height
   *
   * @param srcWidth
   * @param srcHeight
   * @param dstWidth
   * @param dstHeight
   * @return
   */
  private int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
    final float srcAspect = (float)srcWidth / (float)srcHeight;
    final float dstAspect = (float)dstWidth / (float)dstHeight;

    if (srcAspect > dstAspect) {
      return srcWidth / dstWidth;
    } else {
      return srcHeight / dstHeight;
    }
  }

  /**
   * Maintain the aspect ratio so the resulting image does not look smooshed
   *
   * @param origWidth
   * @param origHeight
   * @return
   */
  private int[] calculateAspectRatio(int origWidth, int origHeight) {
    int newWidth = width;
    int newHeight = height;

    if (newWidth <= 0 && newHeight <= 0) {
      newWidth = origWidth;
      newHeight = origHeight;
    } else if (newWidth > 0 && newHeight <= 0) {
      newHeight = (newWidth * origHeight) / origWidth;
    } else if (newWidth <= 0 && newHeight > 0) {
      newWidth = (newHeight * origWidth) / origHeight;
    } else {
      double newRatio = newWidth / (double) newHeight;
      double origRatio = origWidth / (double) origHeight;

      if (origRatio > newRatio) {
        newHeight = (newWidth * origHeight) / origWidth;
      } else if (origRatio < newRatio) {
        newWidth = (newHeight * origWidth) / origHeight;
      }
    }

    int[] retval = new int[2];
    retval[0] = newWidth;
    retval[1] = newHeight;
    return retval;
  }
}