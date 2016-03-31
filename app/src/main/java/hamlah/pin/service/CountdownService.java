package hamlah.pin.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;

import hamlah.pin.AcknowledgeActivity;
import hamlah.pin.CountDownTimer;
import hamlah.pin.MainActivity;
import hamlah.pin.R;

public class CountdownService extends Service {
    private static final String TAG = CountdownService.class.getSimpleName();
    private static final int INTENT_REQUEST_CODE = 412;
    private boolean isForeground = false;
    private static final int NOTIFICATION_ID = 6928;
    private CountDownTimer countdown;
    private int countdownid = -1;
    private boolean supadead = false;
    private DateTime last = DateTime.now();
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // update if the screen turns on
            go(context);
        }
    };

    public CountdownService() {
    }

    private class CustomCountdown extends CountDownTimer {

        public CustomCountdown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            updateOrDie();
        }

        @Override
        public void onFinish() {
            updateOrDie();
        }
    }

    private synchronized void updateOrDie() {
        if (!update()) {
            die();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        supadead = true;
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if (!update()) {
            die();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    private synchronized void die() {
        if (supadead) {
            return;
        }
        if (isForeground) {
            stopForeground(true);
            unregisterReceiver(receiver);
            isForeground = false;
        }
        if (countdown != null) {
            countdown.cancel();
            countdown = null;
        }
        stopSelf();
        Log.v(TAG, "die(): done");
    }

    public static String formatTime(long time, boolean showSeconds) {
        time += 999;
        long seconds = (time / 1000) % 60;
        long minutes = (time / (60 * 1000)) % 60;
        long hours = (time / (60 * 60 * 1000)) % 24;
        long days = (time / (60 * 60 * 24 * 1000));
        String result = "";
        int count = 0;
        if (days > 0) {
            result += String.format("%s days and ", days);
            count++;
        }
        if (hours > 0 || count > 0) {
            result += String.format("%s:", hours);
            count++;
        }
        if (!showSeconds || minutes > 0 || count > 0) {
            if (count > 0) {
                result += String.format("%02d", minutes);
            } else {
                result += String.format("%d", minutes);
            }
            if (count == 0 && !showSeconds) {
                result += " minutes";
            }
            count++;
        }
        if (showSeconds || (time < 1000 * 60)) {
            if (count > 0) {
                result += ":";
            }
            result += String.format("%02d", seconds);
            if (time < 1000 * 60 || count == 1) {
                result += " seconds";
            }
        }
        return result;
    }

    private synchronized boolean update() {
        if (supadead) {
            return false;
        }
        final Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon_alarm);

        Settings settings = new Settings(this);
        Class<?> activity;
        long remaining;
        String title;
        int content;
        boolean showSeconds;
        DateTime now = DateTime.now(DateTimeZone.getDefault());
        last = now;
        if (settings.main.isCounting()) {

            title = settings.main.getLabel();
            remaining = settings.main.remaining();
            content = R.string.main_alarm_detail;
            showSeconds = false;
            updateCountdown(1, remaining, 20);

            activity = AcknowledgeActivity.class;

        } else if (settings.bother.isCounting()) {
            title = settings.bother.getLabel();
            remaining = settings.bother.remaining();
            content = R.string.bother_alarm_detail;
            showSeconds = true;
            updateCountdown(2, remaining, 1);

            activity = MainActivity.class;

        } else {
            Log.v(TAG, "nothing is counting");
            return false;
        }
        builder.setContentTitle(title);
        String finalContent = String.format(getString(content),
                formatTime(remaining, showSeconds));
        builder.setContentText(finalContent);
        builder.setStyle(new Notification.BigTextStyle().bigText(finalContent));


        Intent notificationIntent = new Intent(this, activity);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, INTENT_REQUEST_CODE, notificationIntent, 0);
        Notification notification = builder.setContentIntent(pendingIntent).build();
        if (isForeground) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, notification);
        } else {
            Log.v(TAG, "showing notification with startForeground()");
            startForeground(NOTIFICATION_ID, notification);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(receiver, filter);
            isForeground = true;
        }
        return true;
    }

    private synchronized void updateCountdown(int id, long remaining, int secondsBetweenUpdates) {
        if (countdownid == id && countdown != null) {
            return;
        }
        countdownid = id;
        if (countdown != null) {
            countdown.cancel();
        }
        countdown = new CustomCountdown(remaining, secondsBetweenUpdates * 1000);
        countdown.start();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void go(Context context) {
        Log.i(TAG, "go!");
        Intent intent = new Intent(context, CountdownService.class);
        context.startService(intent);
    }
}
