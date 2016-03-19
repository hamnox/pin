package hamlah.pin;

import android.app.Application;

import hamlah.pin.service.Settings;
import hamlah.pin.service.Timers;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Timers.go(this);
        new Settings(this).refreshAlarms();
    }
}
