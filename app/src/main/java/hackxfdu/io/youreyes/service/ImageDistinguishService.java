package hackxfdu.io.youreyes.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import hackxfdu.io.youreyes.service.receiver.ImageEventReceiver;
import hackxfdu.io.youreyes.utils.AlarmUtils;

/**
 * Image distinguishing service.
 *
 * @author sczyh30
 */
public class ImageDistinguishService extends Service {

    int period = 6 * 1000;

    public ImageDistinguishService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO: read period settings from user
        AlarmUtils.setPeriodic(this, ImageEventReceiver.class, period);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
