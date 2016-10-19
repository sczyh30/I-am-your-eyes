package hackxfdu.io.youreyes.ui;

import android.hardware.Camera;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import hackxfdu.io.youreyes.R;
import hackxfdu.io.youreyes.base.BaseActivity;
import hackxfdu.io.youreyes.http.ImageHttpService;
import hackxfdu.io.youreyes.http.SpeakTextService;
import hackxfdu.io.youreyes.utils.AudioRecorder;
import hackxfdu.io.youreyes.utils.DeviceUtils;
import hackxfdu.io.youreyes.utils.FileUtils;
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

import static hackxfdu.io.youreyes.utils.FileUtils.MEDIA_TYPE_IMAGE;

/**
 * Main activity of the app.
 * INFO: Currently only a demo!
 *
 * @author sczyh30
 */
public class MainActivity extends BaseActivity {

    // TODO: Refactor and refinement is required!

    private Camera mCamera;
    private SpeechSynthesizer mSpeechSynthesizer;
    private AudioRecorder mRecord;

    private Retrofit retrofit;

    @BindView(R.id.button_image)
    Button mImageButton;
    @BindView(R.id.button_lingStart)
    Button mLingStartButton;
    @BindView(R.id.button_lingEnd)
    Button mLingEndButton;

    @Override
    protected void init(Bundle savedInstanceState) {
        initCamera();
        initNetwork();
        initSpeech();
        mRecord = AudioRecorder.getInstance();
        if (DeviceUtils.getNetworkType(this) <= 0) {
            speakInformation(R.string.err_no_available_internet);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @OnClick(R.id.button_image)
    void describeEnvironmentImage() {
        if (mCamera != null) {
            mCamera.startPreview();
            // Take the picture
            mCamera.takePicture(null, null, (data, camera) -> {

                File pictureFile = FileUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d("YourEyes", "Error creating media file, check storage permissions!");
                    return;
                }
                try (FileOutputStream fos = new FileOutputStream(pictureFile)) {
                    fos.write(data);
                } catch (IOException e) {
                    Log.e("YourEyes", "Error accessing file: " + e.getMessage());
                    e.printStackTrace();
                }

                mCamera.stopPreview();

                // TODO: Need an encapsulation to simplify the architecture
                ImageHttpService service = retrofit.create(ImageHttpService.class);
                uploadImage(service, pictureFile)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(ar -> {
                            try {
                                if (ar.isError()) {
                                    ar.error().printStackTrace();
                                    speakInformation(R.string.err_request_fail_common);
                                } else {
                                    mSpeechSynthesizer.speak(ar.response().body().string());
                                }
                                pictureFile.delete();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }, Throwable::printStackTrace);

            });
        } else {
            speakInformation(R.string.err_no_available_camera);
        }
    }

    @OnClick(R.id.button_lingStart)
    void startLinguist() {
        mRecord.startRecordAndFile();
    }

    @OnClick(R.id.button_lingEnd)
    void stopLinguist() {
        mRecord.stopRecordAndFile();
        SpeakTextService service = retrofit.create(SpeakTextService.class);
        uploadAudioAwaitResult(service)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ar -> {
                    try {
                        if (ar.isError()) {
                            ar.error().printStackTrace();
                            speakInformation(R.string.err_request_fail_common);
                        } else {
                            mSpeechSynthesizer.speak(ar.response().body().string());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);
    }

    /**
     * Initialize the network component (e.g. OkHttp and Retrofit).
     */
    private void initNetwork() {
        if (this.retrofit == null) {
            // Set logging interceptor
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .retryOnConnectionFailure(true)
                    .connectTimeout(5, TimeUnit.SECONDS) // Connection timeout
                    .build();
            // Create Retrofit instance
            this.retrofit = new Retrofit.Builder()
                    .baseUrl(ImageHttpService.API_BASE)
                    .client(client)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
        }
    }

    /**
     * Initialize the mobile camera.
     */
    private void initCamera() {
        this.mCamera = getCameraInstance();
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureSize(3264, 2448);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            mCamera.setParameters(parameters);
        }
    }

    /**
     * Initialize the speech component.
     */
    private void initSpeech() {
        if (this.mSpeechSynthesizer == null) {
            mSpeechSynthesizer = SpeechSynthesizer.getInstance();
            mSpeechSynthesizer.setContext(this);
            mSpeechSynthesizer.setSpeechSynthesizerListener(new SpeechSynthesizerListener() {
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
            mSpeechSynthesizer.setAppId("8750514");
            mSpeechSynthesizer.setApiKey("PVPtCfacKZsxsgY0SrHVS4Bs", "6e9d49b1d66991c4455dc9c2b20ee844");
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "3");
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);

            // Init the speech engine
            mSpeechSynthesizer.initTts(TtsMode.MIX);
        }
    }

    private Observable<Result<ResponseBody>> uploadImage(ImageHttpService service, File pictureFile) {
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), pictureFile);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("picture", pictureFile.getName(), requestFile);

        return service.distinguish(body);
    }

    private Observable<Result<ResponseBody>> uploadAudioAwaitResult(SpeakTextService service) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + AudioRecorder.AUDIO_WAV_FILENAME_DEFAULT);
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("speech", file.getName(), requestFile);
        return service.doSpeech(body);
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // Attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c; // Returns null if camera is unavailable
    }

    private void speakInformation(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
        mSpeechSynthesizer.speak(this.getResources().getText(resId).toString());
    }
}
