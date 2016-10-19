package hackxfdu.io.youreyes.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Helper class for Android device information.
 *
 * @author sczyh30
 * @since 0.2
 */
public final class DeviceUtils {

    private DeviceUtils() {
    }

    private static final int NET_TYPE_WIFI = 0x01;
    public static final int NET_TYPE_CMWAP = 0x02;
    public static final int NET_TYPE_CMNET = 0x03;

    /**
     * Get the type of current network.
     *
     * @param context application context
     * @return 0 if the network is unavailable; 1 if current network is WIFI;
     * other if current network is 2G/3G/4G
     */
    public static int getNetworkType(Context context) {
        int netType = 0;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!extraInfo.equals("")) {
                if (extraInfo.toLowerCase().equals("cmnet")) {
                    netType = NET_TYPE_CMNET;
                } else {
                    netType = NET_TYPE_CMWAP;
                }
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = NET_TYPE_WIFI;
        }
        return netType;
    }

}
