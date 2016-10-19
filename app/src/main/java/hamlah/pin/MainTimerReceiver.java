package hamlah.pin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import hamlah.pin.service.Timers;

/**
 * Created by RingtoneManager on 1/12/16.
 *
 mRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
 */
public class MainTimerReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent) {
        // Timers.setOffMainAlarm(context);
    }
}