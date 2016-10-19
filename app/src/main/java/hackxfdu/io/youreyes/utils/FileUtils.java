package hackxfdu.io.youreyes.utils;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * File util class.
 * This helper class provides helper functions for file system.
 *
 * @author sczyh30
 * @since 0.2
 */

public final class FileUtils {

    public static final String APP_EXT_DIR = "YourEyes";

    private FileUtils() {
    }

    /**
     * Get the size of the file.
     *
     * @param path absolute path of the file
     * @return the size of the file. If the file does not exist, return -1.
     */
    public static long getFileSize(String path) {
        File mFile = new File(path);
        if (!mFile.exists()) {
            return -1;
        }
        return mFile.length();
    }

    /**
     * Find if there exists external SD card.
     */
    public static boolean existsSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file Uri for saving an image or video.
     *
     * @param type file type
     */
    public static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a file for saving an image or video.
     *
     * @param type file type
     */
    public static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), APP_EXT_DIR);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("YourEyes", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        switch (type) {
            case MEDIA_TYPE_IMAGE:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                        "IMG_" + timeStamp + ".jpg");
                break;
            case MEDIA_TYPE_VIDEO:
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                        "VID_" + timeStamp + ".mp4");
                break;
            default:
                return null;
        }

        return mediaFile;
    }
}
