package hamlah.pin.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

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
            isForeground = false;
        }
        if (countdown != null) {
            countdown.cancel();
            countdown = null;
        }
        stopSelf();
        Log.v(TAG, "die(): done");
    }

    private String formatTime(long time, boolean showSeconds) {
        time += 999;
        long seconds = (time / 1000) % 60;
        long minutes = (time / (60 * 1000)) % 60;
        long hours = (time / (60 * 60 * 1000)) % 24;
        long days = (time / (60 * 60 * 24 * 1000));
        String result = "";
        if (days > 0) {
            result += String.format("%s days and ", days);
        }
        if (hours > 0 || days > 0) {
            result += String.format("%s:", hours);
        }
        if (!showSeconds || days > 0 || hours > 0 || minutes > 0) {
            result += String.format("%02d", minutes);
        }
        if (showSeconds || (time < 1000 * 60)) {
            if (minutes > 0 || hours > 0 || days > 0) {
                result += ":";
            }
            result += String.format("%02d", seconds);
            if (time < 1000 * 60) {
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
        int title;
        int content;
        boolean showSeconds;
        if (settings.main.isCounting()) {
            title = R.string.main_alarm;

            remaining = settings.main.remaining();
            content = R.string.main_alarm_detail;
            showSeconds = false;
            updateCountdown(1, remaining, 60);

            activity = AcknowledgeActivity.class;

        } else if (settings.bother.isCounting()) {
            title = R.string.main_alarm;

            remaining = settings.bother.remaining();
            content = R.string.bother_alarm_detail;
            showSeconds = true;
            updateCountdown(2, remaining, 1);

            activity = MainActivity.class;

        } else {
            Log.v(TAG, "nothing is counting");
            return false;
        }
        builder.setContentTitle(getText(title));
        builder.setContentText(String.format(getString(content),
                formatTime(remaining, showSeconds)));


        Intent notificationIntent = new Intent(this, activity);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, INTENT_REQUEST_CODE, notificationIntent, 0);
        Notification notification = builder.setContentIntent(pendingIntent).build();
        if (isForeground) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, notification);
        } else {
            Log.v(TAG, "showing notification with startForeground()");
            startForeground(NOTIFICATION_ID, notification);
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
