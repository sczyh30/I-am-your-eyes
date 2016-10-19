package hackxfdu.io.youreyes.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Speech distinguishing service (mobile backend).
 *
 * @author sczyh30
 */
public class SpeechDistinguishService extends Service {
    public SpeechDistinguishService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
