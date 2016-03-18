package hamlah.pin.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.annotation.Nullable;
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

    public final AlarmSettings bother = new AlarmSettings("botheralarm", 0, BotherBotherReceiver.class, 1000, 59, "Bother Countdown");
    public final AlarmSettings main = new AlarmSettings("mainalarm", 1, MainTimerReceiver.class, 60 * 1000);
    private final Context context;

    public Settings(Context context) {
        this.preferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        this.context = context;
    }

    public void refreshAlarms() {
        bother.refresh();
        main.refresh();
    }

    @SuppressLint("CommitPrefEdits")
    public class AlarmSettings {
        private final String ALARM_LABEL_KEY;
        private final int alarmIndex;
        private final Class<?> receiver;
        private final String ALARM_KEY;
        private final String ALARM_TRIGGERED_KEY;
        private final Integer defaultvalue;
        private final long scale;
        private final String defaultlabel;

        private AlarmSettings(String name, int alarmIndex, Class<?> receiver, long scale) {
            this(name, alarmIndex, receiver, scale, null, null);
        }

        private AlarmSettings(String name, int alarmIndex, Class<?> receiver, long scale, Integer defValue, String defaultlabel) {

            this.alarmIndex = alarmIndex;
            this.receiver = receiver;
            ALARM_KEY = name;
            this.defaultlabel = defaultlabel;
            ALARM_TRIGGERED_KEY = name + "_triggered";
            ALARM_LABEL_KEY = name + "_label";
            this.defaultvalue = defValue;
            this.scale = scale;
        }


        public boolean arm() {
            if (defaultvalue == null) {
                throw new UnsupportedOperationException();
            }
            return arm(defaultvalue, defaultlabel);
        }

        public boolean arm(long value, String label) {
            if (remaining() >= 0 || isTriggered()) {
                return false;
            }
            setLabel(label);
            long end = SystemClock.elapsedRealtime() + value * scale;
            _createAndroidAlarm(end);
            return true;
        }

        private void refresh() {
            final long l = get();
            if (l != -1) {
                _createAndroidAlarm(l);
            }
        }

        private void _createAndroidAlarm(long end) {
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, receiver);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, alarmIndex, intent, 0);

            alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, end, alarmIntent);

            setTime(end);
            Log.i(TAG, "alarm set: " + ALARM_KEY);
        }

        void rearm() {
            markNotFiring();
            arm();
        }

        void rearm(long value, String label) {
            markNotFiring();
            arm(value, label);
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

        @Nullable
        public String getLabel() {
            return preferences.getString(ALARM_LABEL_KEY, defaultlabel);
        }

        private void setLabel(String value) {
            preferences.edit().putString(ALARM_LABEL_KEY, value).commit();
        }
    }
}