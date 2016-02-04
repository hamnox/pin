package hamlah.pin;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hamnox on 2/3/16.
 */
public class Settings {
    private static final String BOTHER_ALARM_KEY = "botheralarm";
    private static final String BOTHER_ALARM_TRIGGERED_KEY = "botheralarm_triggered";
    private static final String MAIN_ALARM_TRIGGERED_KEY = "mainalarm_triggered";
    private final SharedPreferences preferences;

    public Settings(Context context) {
        this.preferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
    }

    public long getCurrentBotherAlarm() {
        return this.preferences.getLong(BOTHER_ALARM_KEY, -1);
    }

    public void setCurrentBotherAlarm(long value) {
        this.preferences.edit().putLong(BOTHER_ALARM_KEY, value).commit();
    }

    public boolean isBotherAlarmTriggered() {
        return this.preferences.getBoolean(BOTHER_ALARM_TRIGGERED_KEY, false);
    }

    public boolean setBotherAlarmTriggered(boolean value) {
        return this.preferences.edit().putBoolean(BOTHER_ALARM_TRIGGERED_KEY, value).commit();
    }

    public boolean isMainAlarmTriggered() {
        return this.preferences.getBoolean(MAIN_ALARM_TRIGGERED_KEY, false);
    }

    public boolean setMainAlarmTriggered(boolean value) {
        return this.preferences.edit().putBoolean(MAIN_ALARM_TRIGGERED_KEY, value).commit();
    }
}