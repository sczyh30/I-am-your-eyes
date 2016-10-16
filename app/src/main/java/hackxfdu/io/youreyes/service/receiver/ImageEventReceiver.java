package hackxfdu.io.youreyes.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import hackxfdu.io.youreyes.http.ImageHttpService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.Result;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A broadcast receiver for image event (e.g. transmit).
 *
 * @author sczyh30
 */

public class ImageEventReceiver extends BroadcastReceiver {

    private Camera mCamera;
    private SpeechSynthesizer speechSynthesizer;

    @Override
    public void onReceive(Context context, Intent intent) {
        mCamera = getCameraInstance();
        if (mCamera != null) {
            // Settings
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureSize(3264, 2448);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(parameters);
            // Start preview and take photos
            mCamera.startPreview();
            mCamera.takePicture(null, null, (data, camera) -> {
                // Need optimization
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d("YourEyes", "Error creating media file, check storage permissions!");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    mCamera.stopPreview();

                    initSpeech(context);

                    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(interceptor)
                            .retryOnConnectionFailure(true)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .build();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(ImageHttpService.API_BASE)
                            .client(client)
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .build();
                    ImageHttpService service = retrofit.create(ImageHttpService.class);
                    uploadImage(service, pictureFile)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(ar -> {
                                try {
                                    speechSynthesizer.speak(ar.response().body().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                } catch (FileNotFoundException e) {
                    Log.d("YourEyes", "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d("YourEyes", "Error accessing file: " + e.getMessage());
                }
            });
        }
    }

    private void initSpeech(Context context) {
        speechSynthesizer = SpeechSynthesizer.getInstance();
        speechSynthesizer.setContext(context);
        speechSynthesizer.setSpeechSynthesizerListener(new SpeechSynthesizerListener() {
            @Override
            public void onSynthesizeStart(String s) {

            }

            @Override
            public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

            }

            @Override
            public void onSynthesizeFinish(String s) {

            }

            @Override
            public void onSpeechStart(String s) {

            }

            @Override
            public void onSpeechProgressChanged(String s, int i) {

            }

            @Override
            public void onSpeechFinish(String s) {

            }

            @Override
            public void onError(String s, SpeechError speechError) {

            }
        });
        // API Keys
        speechSynthesizer.setAppId("8750514");
        speechSynthesizer.setApiKey("PVPtCfacKZsxsgY0SrHVS4Bs", "6e9d49b1d66991c4455dc9c2b20ee844");
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "3");
        // Mix mode strategy
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);

        // Init the speech engine
        speechSynthesizer.initTts(TtsMode.MIX);
    }

    private Observable<Result<ResponseBody>> uploadImage(ImageHttpService service, File pictureFile) {
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), pictureFile);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("picture", pictureFile.getName(), requestFile);

        return service.distinguish(body);
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file for saving an image or video.
     *
     * @param type file type
     */
    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "YourEyes");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("YourEyes", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }
}
