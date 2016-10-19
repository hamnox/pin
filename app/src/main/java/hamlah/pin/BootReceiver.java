package hamlah.pin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import hamlah.pin.service.Timers;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Timers.armBotherAlarm(context, "boot");
    }
}