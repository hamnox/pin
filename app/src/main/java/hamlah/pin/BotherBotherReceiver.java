package hamlah.pin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;


public class BotherBotherReceiver extends BroadcastReceiver {
    private static final String TAG = BotherBotherReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        AsyncRingtonePlayer.getAsyncRingtonePlayer(context).play(null);
        Log.i(TAG, "Bother Alarm went off");
        Settings settings =  new Settings(context);
        settings.setCurrentBotherAlarm(-1);
        settings.setBotherAlarmTriggered(true);
    }
    // - set off asyncringtone, and any other alarm-is-going-off state management, when bother alarm hits
    // - beeping happens
    // - mainactivity sees flag, shows "acknowledge bother alarm" button
    // - when that button is pressed, shut off beeping and set a new bother alarm
    // - then when actual alarm is set, disable bother alarm
    // - (and when opening main activity and actual alarm is set, don't do anything, and instead open the main alarm countdown activity)

    //// TODO: 2/2/16 check to see if a bother alarm is set before setting
    //TODO: sleep for sleeping
    public static void setAlarm(Context context) {
        if (getTimeUntilNext(context) >= 0 || new Settings(context).isBotherAlarmTriggered()) {
            return;
        }
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, BotherBotherReceiver.class);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        long end = SystemClock.elapsedRealtime() + 15 * 1000;
        alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, end, alarmIntent);

        new Settings(context).setCurrentBotherAlarm(end);
        Log.i(TAG, "Bother alarm set");
    }


    public static long getTimeUntilNext(Context context) {
        long currentBotherAlarm = new Settings(context).getCurrentBotherAlarm();
        if (currentBotherAlarm < 0) {
            return -1;
        }
        return currentBotherAlarm - SystemClock.elapsedRealtime();
    }

    public static void cancelAlarm(Context context) {
        AsyncRingtonePlayer.getAsyncRingtonePlayer(context).stop();
        Settings settings =  new Settings(context);
        settings.setCurrentBotherAlarm(-1);
        settings.setBotherAlarmTriggered(false);
    }
}