package hamlah.pin.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import hamlah.pin.AcknowledgeActivity;
import hamlah.pin.MainActivity;

public class AsyncRingtoneService extends Service {

    private static final String TAG = AsyncRingtoneService.class.getSimpleName();
    private static PowerManager.WakeLock lock = null;

    boolean botherAlarmPlaying = false;
    boolean mainAlarmPlaying = false;

    synchronized private static PowerManager.WakeLock getLock(Context context) {
        if (lock == null) {
            context = context.getApplicationContext();
            PowerManager mgr =
                    (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            lock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, AsyncRingtoneService.class.getName());
            lock.setReferenceCounted(false);
        }

        return lock;
    }

    static void go(Context context) {
        Log.i(TAG, "go!");
        getLock(context).acquire();
        Intent intent = new Intent(context, AsyncRingtoneService.class);
        context.startService(intent);
        Log.i(TAG, "went!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shutdown();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");
        PowerManager.WakeLock lock = getLock(this);

        if (!lock.isHeld()) {
            lock.acquire();
            Log.i(TAG, "Lock re-acquired");
        }

        Context context = this.getApplicationContext();
        Settings settings = new Settings(context);
        final boolean mainAlarmTriggered = settings.main.isTriggered();
        final boolean botherAlarmTriggered = settings.bother.isTriggered();
        if (mainAlarmTriggered) {
            Log.i(TAG, "main alarm is triggered...");
            settings.bother.disarm();

            if (!mainAlarmPlaying) {
                Log.i(TAG, "... and wasn't playing. starting it");
                AsyncRingtonePlayer.get(context).play(null);

                Intent mainIntent = new Intent(context, AcknowledgeActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mainIntent);
            }
            botherAlarmPlaying = false;
            mainAlarmPlaying = true;
        } else if (botherAlarmTriggered) {
            Log.i(TAG, "bother alarm is triggered...");
            if (!botherAlarmPlaying) {
                Log.i(TAG, "... and wasn't playing. starting it");
                AsyncRingtonePlayer.get(context).play(null);

                Intent mainIntent = new Intent(context, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mainIntent);
            }
            botherAlarmPlaying = true;
            mainAlarmPlaying = false;
        } else {
            Log.i(TAG, "no alarm is triggered, shutting down");
            shutdown();
        }

        return START_STICKY;
    }

    private void shutdown() {
        if (mainAlarmPlaying || botherAlarmPlaying) {
            AsyncRingtonePlayer.get(this).stop();
        }
        botherAlarmPlaying = false;
        mainAlarmPlaying = false;

        if (lock.isHeld()) {
            try {
                lock.release();
            } catch (Exception e) {
                Log.e(TAG, "Exception when releasing wakelock", e);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}