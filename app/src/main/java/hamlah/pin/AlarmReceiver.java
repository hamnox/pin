package hamlah.pin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by RingtoneManager on 1/12/16.
 *
 mRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
 */
public class AlarmReceiver extends BroadcastReceiver
{
    private static final String TAG = AlarmReceiver.class.getSimpleName();
    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;
    private static boolean isPinOpen;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "");
        wl.acquire();
        AsyncRingtonePlayer.getAsyncRingtonePlayer(context.getApplicationContext()).play(null);
        Log.i(TAG, "Alarm went off");

        // Checking whether the main activity is open
        // http://stackoverflow.com/questions/3667022/checking-if-an-android-application-is-running-in-the-background


        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (!isPinOpen) {
            context.startActivity(mainIntent);
//        }
        wl.release();

        setAlarm(context);
    }

    public static void setAlarm(Context context) {
        alarmMgr = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        30 * 1000, alarmIntent);
        Log.i(TAG, "Alarm set");
    }

    public static void setAlarm(Context context, int minutes) {
        alarmMgr = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        if (minutes <= 0) {
            Toast.makeText(context, "Invalid Timer", Toast.LENGTH_SHORT).show();
            setAlarm(context);
            return;
        }

        AsyncRingtonePlayer.getAsyncRingtonePlayer(context.getApplicationContext()).stop();
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        1 * minutes * 1000 + 1, alarmIntent);
        Log.i(TAG, String.format("%d minute alarm set", minutes));
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public static void setPinOpen(boolean status) {
        isPinOpen = status;
    }
}