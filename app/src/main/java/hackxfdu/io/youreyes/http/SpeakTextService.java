package hackxfdu.io.youreyes.http;

import hackxfdu.io.youreyes.entity.ImageResult;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.adapter.rxjava.Result;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

/**
 * A service interface for text speaking.
 * We use Baidu API to convert string to actual voice.
 *
 * @author sczyh30
 */

public interface SpeakTextService {

    String API_BASE = "http://10.221.65.83:8000/";

    /**
     * Send the speech content to the server.
     *
     * @param file the image file to upload
     * @return asynchronous result
     */
    @Multipart
    @POST("audio")
    Observable<Result<ResponseBody>> doSpeech(@Part MultipartBody.Part file);
}
