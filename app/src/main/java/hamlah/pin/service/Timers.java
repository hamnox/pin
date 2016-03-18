package hamlah.pin.service;

import android.content.Context;
import android.util.Log;

import com.squareup.otto.Bus;

public class Timers {
    private static final String TAG = Timers.class.getSimpleName();
    public static final Bus bus = new Bus();

    public static synchronized void setOffBotherAlarm(Context context) {
        Log.i(TAG, "Bother Alarm went off");
        Settings settings =  new Settings(context);
        settings.main.disarm();
        settings.bother.markFiring();
        go(context);
    }

    public static synchronized void setOffMainAlarm(Context context) {
        Log.i(TAG, "Main Alarm went off");
        Settings settings = new Settings(context);
        settings.bother.disarm();
        settings.main.markFiring();
        go(context);
    }

    public static synchronized void ackBotherAlarm(Context context) {
        Log.i(TAG, "Bother Alarm acknowledged");
        Settings settings =  new Settings(context);
        settings.bother.rearm();
        go(context);
    }

    public static synchronized void ackMainAlarm(Context context) {
        Log.i(TAG, "Main alarm acknowledged");
        Settings settings = new Settings(context);
        settings.main.disarm();
        settings.bother.rearm();
        go(context);
    }

    public static synchronized void setMainAlarm(Context context, long minutes) {
        Log.i(TAG, "Main alarm being set");
        Settings settings = new Settings(context);
        settings.bother.disarm();
        settings.main.rearm(minutes);
        go(context);

    }

    public static synchronized void armBotherAlarm(Context context) {
        Log.i(TAG, "Bother alarm being set");
        Settings settings = new Settings(context);
        if (settings.main.isCounting()) {
            return;
        }
        settings.bother.arm();
        go(context);
    }

    public static void go(Context context) {
        Log.i(TAG, "go!");
        AsyncRingtoneService.go(context);
        CountdownService.go(context);
    }
}
