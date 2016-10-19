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
 * Image http request service.
 *
 * @author sczyh30
 */
public interface ImageHttpService {

    String API_BASE = "http://10.221.65.83:8000/";

    /**
     * Send the image content to the backend server.
     * The server should return a {@link ImageResult} as a result of recognition.
     *
     * @param file the image file to upload
     * @return asynchronous result
     */
    @Multipart
    @POST("photo")
    Observable<Result<ResponseBody>> distinguish(@Part MultipartBody.Part file);
}
