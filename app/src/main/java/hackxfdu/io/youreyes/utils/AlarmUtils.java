package hackxfdu.io.youreyes.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Alarm utils class.
 *
 * @author sczyh30
 */
public final class AlarmUtils {

    private AlarmUtils() {
    }

    /**
     * Set a one-time alarm so that we can run tasks in the backend.
     *
     * @param context current context
     * @param klass   the {@link Class} of the destination receiver
     * @param period  task period
     * @return true if the alarm is set correctly; else false
     */
    public static boolean setAlarm(Context context, Class<?> klass, int period) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager == null)
            return false;
        long triggerAtTime = SystemClock.elapsedRealtime() + period;
        Intent i = new Intent(context, klass);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, triggerAtTime, pi);
        return true;
    }

    /**
     * Set a periodic alarm so that we can run periodic tasks in the backend.
     *
     * @param mode repeat type
     * @param context current context
     * @param klass the {@link Class} of the destination receiver
     * @param period task period
     * @return true if the alarm is set correctly; else false
     */
    public static boolean setPeriodic(int mode, Context context, Class<?> klass, int period) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager == null) {
            return false;
        }
        long start = SystemClock.elapsedRealtime();
        Intent i = new Intent(context, klass);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        alarmManager.setRepeating(mode, start, period, pi);
        return true;
    }

    public static boolean setPeriodic(Context context, Class<?> klass, int period) {
        return setPeriodic(AlarmManager.ELAPSED_REALTIME, context, klass, period);
    }

    public static boolean setPeriodicWakeup(Context context, Class<?> klass, int period) {
        return setPeriodic(AlarmManager.ELAPSED_REALTIME_WAKEUP, context, klass, period);
    }
}
