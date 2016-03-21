package hamlah.pin;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import hamlah.pin.service.Settings;
import hamlah.pin.service.Timers;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        Timers.go(this);
        new Settings(this).refreshAlarms();
    }
}
