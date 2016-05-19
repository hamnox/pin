package hamlah.pin.service;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import hamlah.pin.App;
import hamlah.pin.R;

public class Timers {
    private static final String TAG = Timers.class.getSimpleName();

    public static synchronized void setOffBotherAlarm(Context context) {
        Log.i(TAG, "Bother Alarm went off");
        Settings settings =  new Settings(context);
        log("fire", "bother", null, settings.bother.getLabel(), context);
        settings.main.disarm();
        settings.bother.markFiring();
        go(context);
    }

    public static synchronized void bugLog(String bug, String label, Context context) {
        PrintWriter out = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());

        try {
            File external = Environment.getExternalStorageDirectory();
            File filepath = new File(external, "stride_bugs.log");
            out = new PrintWriter(new BufferedWriter(new FileWriter(filepath, true)));
            Date result_date = new Date(System.currentTimeMillis());

            final String formatted = String.format("%s %s <%s>", sdf.format(result_date), bug, label);

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

    public static synchronized void log(String event, String alarmtype, Long timeleft, String label, Context context) {
        if (!App.canLog()) {
            Toast.makeText(context, R.string.seriouserror, Toast.LENGTH_LONG).show();
        }
        PrintWriter out = null;
        try {
            File external = Environment.getExternalStorageDirectory();
            File filepath = new File(external, "stride_timers.log");
            out = new PrintWriter(new BufferedWriter(new FileWriter(filepath, true)));
            long time = System.currentTimeMillis();
            final String formatted = String.format("%s %s:%s:%s %s", time, event, alarmtype, timeleft, label);
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
            log("set", "bother", 1L, reason, context);
        }
        go(context);
    }

    public static void go(Context context) {
        Log.i(TAG, "go!");
        AsyncRingtoneService.go(context);
        CountdownService.go(context);
    }
}
