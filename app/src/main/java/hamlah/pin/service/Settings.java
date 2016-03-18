package hamlah.pin.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import hamlah.pin.BotherBotherReceiver;
import hamlah.pin.BuildConfig;
import hamlah.pin.MainTimerReceiver;

/**
 * Created by hamnox on 2/3/16.
 */
public class Settings {
    private static final String TAG = Settings.class.getSimpleName();
    private final SharedPreferences preferences;

    public final AlarmSettings bother = new AlarmSettings("botheralarm", 0, BotherBotherReceiver.class, 1000, 59);
    public final AlarmSettings main = new AlarmSettings("mainalarm", 1, MainTimerReceiver.class, 60 * 1000);
    private final Context context;

    public Settings(Context context) {
        this.preferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        this.context = context;
    }

    public void verifyAlarms() {
        if (bother.isCounting()) {
            bother.rearm(bother.remaining());
        }
        if (main.isCounting()) {
            main.rearm(main.remaining());
        }
    }

    @SuppressLint("CommitPrefEdits")
    public class AlarmSettings {
        private final int alarmIndex;
        private final Class<?> receiver;
        private final String ALARM_KEY;
        private final String ALARM_TRIGGERED_KEY;
        private final Integer defaultvalue;
        private final long scale;

        private AlarmSettings(String name, int alarmIndex, Class<?> receiver, long scale) {
            this(name, alarmIndex, receiver, scale, null);
        }

        private AlarmSettings(String name, int alarmIndex, Class<?> receiver, long scale, Integer defValue) {

            this.alarmIndex = alarmIndex;
            this.receiver = receiver;
            ALARM_KEY = name;
            ALARM_TRIGGERED_KEY = name + "_triggered";
            this.defaultvalue = defValue;
            this.scale = scale;
        }


        public void arm() {
            if (defaultvalue == null) {
                throw new UnsupportedOperationException();
            }
            arm(defaultvalue);
        }

        public void arm(long value) {
            if (remaining() >= 0 || isTriggered()) {
                return;
            }
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, receiver);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, alarmIndex, intent, 0);

            long end = SystemClock.elapsedRealtime() + value * scale;
            alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, end, alarmIntent);

            setTime(end);
            Log.i(TAG, "alarm set: " + ALARM_KEY);
        }

        void rearm() {
            markNotFiring();
            arm();
        }

        void rearm(long value) {
            markNotFiring();
            arm(value);
        }

        public long remaining() {
            long current = get();
            if (current < 0) {
                return -1;
            }
            return current - SystemClock.elapsedRealtime();
        }

        public void disarm() {
            Intent intent = new Intent(context, receiver);
            PendingIntent sender = PendingIntent.getBroadcast(context, alarmIndex, intent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(sender);
            markNotFiring();
        }

        void flagAsNotScheduled() {
            setTime(-1);
        }

        void markNotFiring() {
            flagAsNotScheduled();
            setTriggered(false);

        }

        void markFiring() {
            flagAsNotScheduled();
            setTriggered(true);
        }

        private long get() {
            return preferences.getLong(ALARM_KEY, -1);
        }

        private void setTime(long value) {
             preferences.edit().putLong(ALARM_KEY, value).commit();
        }

        public boolean isTriggered() {
            return preferences.getBoolean(ALARM_TRIGGERED_KEY, false);
        }

        private void setTriggered(boolean value) {
            preferences.edit().putBoolean(ALARM_TRIGGERED_KEY, value).commit();
        }


        public boolean isCounting() {
            return get() >= 0;
        }
    }
}