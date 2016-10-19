package hamlah.pin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import hamlah.pin.service.Timers;


public class BotherBotherReceiver extends BroadcastReceiver {
    private static final String TAG = BotherBotherReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        //Timers.setOffBotherAlarm(context);
    }
    // - mainactivity sees flag, shows "acknowledge bother alarm" button
    // - when that button is pressed, shut off beeping and set a new bother alarm
    // - then when actual alarm is set, disable bother alarm
    // - (and when opening main activity and actual alarm is set, don't do anything, and instead open the main alarm countdown activity)

    //// TODO: 2/2/16 check to see if a bother alarm is set before setting
    //TODO: sleep for sleeping

}