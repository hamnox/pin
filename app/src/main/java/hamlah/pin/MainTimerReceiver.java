package hamlah.pin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by RingtoneManager on 1/12/16.
 *
 mRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
 */
public class MainTimerReceiver extends BroadcastReceiver
{
    private static final String TAG = MainTimerReceiver.class.getSimpleName();
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


        Log.i(TAG, "Alarm went off");

        // Checking whether the main activity is open
        // http://stackoverflow.com/questions/3667022/checking-if-an-android-application-is-running-in-the-background


        // TODO: hold a service open until the user acknowledges
        // TODO: hold a wake lock until the user acknowledges
        Intent mainIntent = new Intent(context, AcknowledgeActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (!isPinOpen) {
            context.startActivity(mainIntent);
//        }
        AsyncRingtonePlayer.getAsyncRingtonePlayer(context).play(null);
        wl.release();
    }

    public static void setAlarm(Context context, int minutes) {

        alarmMgr = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context, MainTimerReceiver.class);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        if (minutes <= 0) {
            Toast.makeText(context, "Invalid Timer", Toast.LENGTH_SHORT).show();
            return;
        }

        AsyncRingtonePlayer.getAsyncRingtonePlayer(context.getApplicationContext()).stop();
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        minutes * 1000 + 1, alarmIntent);
        // TODO: don't forget to add 60* back in for reals
        Log.i(TAG, String.format("%d minute alarm set", minutes));
    }

    public static void cancelAlarm(@NonNull Context context) {
        Intent intent = new Intent(context, MainTimerReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}