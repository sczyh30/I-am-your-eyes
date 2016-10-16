package hackxfdu.io.youreyes;

import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

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

import hackxfdu.io.youreyes.http.ImageHttpService;
import hackxfdu.io.youreyes.http.SpeakTextService;
import hackxfdu.io.youreyes.utils.AudioRecordFunc;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.Result;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private SpeechSynthesizer speechSynthesizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mImageButton = (Button)findViewById(R.id.button_image);
        Button mLingStartButton = (Button)findViewById(R.id.button_lingStart);
        Button mLingEndButton = (Button)findViewById(R.id.button_lingEnd);

        this.mCamera = getCameraInstance();
        initSpeech();

        mImageButton.setOnClickListener(v -> {
            if (mCamera != null) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPictureSize(3264, 2448);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                mCamera.setParameters(parameters);
                mCamera.startPreview();

                mCamera.takePicture(null, null, (data, camera) -> {

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

                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(ImageHttpService.API_BASE)
                                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                                .build();
                        ImageHttpService service = retrofit.create(ImageHttpService.class);
                        uploadImage(service, pictureFile)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(ar -> {
                                    try {
                                        if (ar.isError()) {
                                            ar.error().printStackTrace();
                                        }
                                        speechSynthesizer.speak(ar.response().body().string());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }, Throwable::printStackTrace);
                    } catch (FileNotFoundException e) {
                        Log.d("YourEyes", "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d("YourEyes", "Error accessing file: " + e.getMessage());
                    }
                });
            }
        });

        mLingStartButton.setOnClickListener(v -> {
            AudioRecordFunc mRecord = AudioRecordFunc.getInstance();
            mRecord.startRecordAndFile();
        });

        mLingEndButton.setOnClickListener(v -> {
            AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
            mRecord_1.stopRecordAndFile();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SpeakTextService.API_BASE)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            SpeakTextService service = retrofit.create(SpeakTextService.class);
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FinalAudio.wav");
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), file);

            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("speech", file.getName(), requestFile);

            service.doSpeech(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ar -> {
                        try {
                            speechSynthesizer.speak(ar.response().body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }, Throwable::printStackTrace);
        });
    }


    private void initSpeech() {
        speechSynthesizer = SpeechSynthesizer.getInstance();
        // 设置 app 上下文(必需参数)
        speechSynthesizer.setContext(this);
        // 设置 tts 监听器
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

        speechSynthesizer.setAppId("8750514");
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        speechSynthesizer.setApiKey("PVPtCfacKZsxsgY0SrHVS4Bs", "6e9d49b1d66991c4455dc9c2b20ee844");
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "3");
        // 设置Mix模式的合成策略
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
     * Create a file Uri for saving an image or video.
     *
     * @param type file type
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

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
