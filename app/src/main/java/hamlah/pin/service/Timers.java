package hamlah.pin.service;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import hamlah.pin.R;

public class Timers {
    private static final String TAG = Timers.class.getSimpleName();
    public static final Bus bus = new Bus();

    public static synchronized void setOffBotherAlarm(Context context) {
        Log.i(TAG, "Bother Alarm went off");
        Settings settings =  new Settings(context);
        log("fire", "bother", null, settings.bother.getLabel(), context);
        settings.main.disarm();
        settings.bother.markFiring();
        go(context);
    }

    private static synchronized void log(String event, String alarmtype, Long timeleft, String label, Context context) {
        PrintWriter out = null;
        try {
            File external = Environment.getExternalStorageDirectory();
            File filepath = new File(external, "pin_timers.log");
            out = new PrintWriter(new BufferedWriter(new FileWriter(filepath, true)));
            final String formatted = String.format("%s:%s:%s %s", event, alarmtype, timeleft, label);
            out.println(formatted);
            out.close();
            Log.i(TAG, formatted);
        } catch (IOException e) {
            Toast.makeText(context, R.string.seriouserror, Toast.LENGTH_LONG).show();
            Log.wtf(TAG, e);
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }

    public static synchronized void setOffMainAlarm(Context context) {
        Log.i(TAG, "Main Alarm went off");
        Settings settings = new Settings(context);
        log("fire", "main", null, settings.main.getLabel(), context);
        settings.bother.disarm();
        settings.main.markFiring();
        go(context);
    }

    public static synchronized void ackBotherAlarm(Context context) {
        Log.i(TAG, "Bother Alarm acknowledged");
        Settings settings =  new Settings(context);
        log("ack", "bother", null, settings.bother.getLabel(), context);
        settings.bother.rearm();
        go(context);
    }

    public static synchronized void ackMainAlarm(Context context) {
        Log.i(TAG, "Main alarm acknowledged");
        Settings settings = new Settings(context);
        log("ack", "main", null, settings.main.getLabel(), context);
        settings.main.disarm();
        settings.bother.rearm();
        go(context);
    }

    public static synchronized void setMainAlarm(Context context, long minutes, String label) {
        log("set", "main", minutes, label, context);
        Settings settings = new Settings(context);
        settings.bother.disarm();
        settings.main.rearm(minutes, label);
        go(context);

    }

    public static synchronized void armBotherAlarm(Context context, String reason) {
        Settings settings = new Settings(context);
        if (settings.main.isCounting()) {
            return;
        }
        if (settings.bother.arm()) {
            log("set", "bother", 1l, "bother", context);
        }
        go(context);
    }

    public static void go(Context context) {
        Log.i(TAG, "go!");
        AsyncRingtoneService.go(context);
        CountdownService.go(context);
    }
}
