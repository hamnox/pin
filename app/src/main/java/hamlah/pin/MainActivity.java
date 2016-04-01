package hamlah.pin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import hamlah.pin.complice.Complice;
import hamlah.pin.complice.CompliceLoginTask;
import hamlah.pin.complice.CompliceRemoteTask;
import hamlah.pin.complice.CompliceTask;
import hamlah.pin.complice.CompliceTaskChangedEvent;
import hamlah.pin.service.CountdownService;
import hamlah.pin.service.Settings;
import hamlah.pin.service.Timers;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {
    //private AlarmManager alarmMgr;
    //private PendingIntent alarmIntent;
    private static final String defaultWakeTime = "7:00";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int BUTTON_COLOR_ALPHA = 0xff000000;
    private static boolean isResumed = false;

    @Bind(R.id.timerminutes)
    EditText minutesEditor;

    @Bind(R.id.label)
    TextInputEditText label;

    @Bind(R.id.waketime)
    EditText waketime;

    @Bind(R.id.bother_countdown)
    TextView botherCountdown;

    @Bind(R.id.acknowledgebutton)
    Button acknowledgeButton;

    @Bind(R.id.sleep_until)
    Button sleepButton;

    @Bind(R.id.thebutton)
    Button thebutton;

    @Bind(R.id.complice_log_in)
    Button logIntoComplice;

    @Bind(R.id.complice_nevermind_task)
    Button compliceNevermind;

    @Bind(R.id.complice_apply_action)
    ImageButton compliceApplyTask;

    @Bind(R.id.complice_task_label)
    TextView compliceTaskLabel;



    @Nullable
    private Integer timerMinutes;

    private Handler handler = new Handler();

    private Runnable countdownCallback;

    private static DateTimeFormatter format = DateTimeFormat.forPattern("H:m");
    @Nullable
    private DateTime nextwake;
    @Nullable
    private CompliceTask compliceWaitingTask;
    @Nullable
    private CompliceRemoteTask availableCompliceTask;

    @Nullable
    private String previousMinutesValue;

    @Nullable
    private String previousLabelValue;
    Settings settings;

    public static void launch(Context context) {
        Log.i(TAG, "Launching, resumed: " + isResumed);
        if (isResumed) {
            return;
        }
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void setNextCountDown(long time) {
        handler.postDelayed(countdownCallback, time);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.app().bus.unregister(this);
        countdownCallback = null;
        isResumed = false;
        Log.i(TAG, "paused");
    }

    /**
     * Copied in both AcknowledgeActivity and MainActivity
     */
    @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    /**
     * Copied in both AcknowledgeActivity and MainActivity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /**
     * Copied in both AcknowledgeActivity and MainActivity
     */
    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        App.permissionResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onResume() {
        super.onResume();
        App.checkPermissions(this);
        App.app().bus.register(this);
        isResumed = true;
        Log.i(TAG, "resumed");
        Timers.armBotherAlarm(this, "mainactivity");

        settings = new Settings(this);

        refreshCompliceTask();
        compliceWaitingTask = settings.getLastWaitingCompliceTask();
        updateComplice();

        String lastMinutes = settings.getLastMinutesText();
        if (lastMinutes != null) {
            minutesEditor.setText(lastMinutes);
        } else {
            label.setText("");
        }
        onTimeEdit();

        String lastTitle = settings.getLastTitleText();
        if (lastTitle != null) {
            label.setText(lastTitle);
        } else {
            label.setText("");
        }
        onLabelEdit();

        String lastWakeTime = settings.getLastWakeTimeText();
        if (lastWakeTime != null) {
            waketime.setText(lastWakeTime);
        } else {
            waketime.setText(defaultWakeTime);
        }
        onWakeTimeEdit();

        waketime.setVisibility(shouldShowSleepButtons() ? View.VISIBLE : View.GONE);
        sleepButton.setVisibility(shouldShowSleepButtons() ? View.VISIBLE : View.GONE);

        countdownCallback = new Runnable() {
            @Override
            public void run() {
                if (this != countdownCallback) {
                    return;
                }
                Settings settings = new Settings(MainActivity.this);
                if (settings.main.isCounting() || settings.main.isTriggered()) {
                    AcknowledgeActivity.launch(MainActivity.this);
                    finish();
                    return;
                }
                if (settings.bother.isTriggered()) {
                    botherCountdown.setVisibility(View.GONE);
                    acknowledgeButton.setVisibility(View.VISIBLE);
                    return;
                }
                long timeUntilNext = settings.bother.remaining();
                if (timeUntilNext < 0) {
                    botherCountdown.setVisibility(View.GONE);
                    timeUntilNext = 50;
                } else {
                    botherCountdown.setVisibility(View.VISIBLE);
                }
                botherCountdown.setText(CountdownService.formatTime(timeUntilNext, true));
                setNextCountDown(timeUntilNext % 1000);
            }
        };
        setNextCountDown(0);
    }


    @Subscribe
    public void onCompliceTaskChanged(CompliceTaskChangedEvent event) {
        refreshCompliceTask();
    }

    public void refreshCompliceTask() {
        if (Complice.get().isLoggedIn()) {
            Complice.get().getNextAction()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<CompliceRemoteTask>() {
                        @Override
                        public void onCompleted() {
                            Log.i(TAG, "done getting current tasks");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "Error getting current tasks", e);
                        }

                        @Override
                        public void onNext(CompliceRemoteTask s) {
                            availableCompliceTask = s;
                            updateComplice();
                        }
                    });
        }
    }

    private double smoothclamp(double in, double newcenter, double curvature, double scale) {
        return Math.tanh((in - 50) / (50 * curvature)) * (scale * 50) + (100 * newcenter);
    }

    private int squashColor(int incolor) {
        double[] husl = HUSLColorConverter.rgbToHusl(HUSLColorConverter.intToRgb(incolor));
        double[] clamped = {
                husl[0],
                smoothclamp(husl[1], 0.6, 1, 0.3),
                smoothclamp(husl[2], 0.5, 0.8, 0.3)
        };
        if (husl[1] < 1) {
            // in case of zero saturation, let's not violently boost it.
            clamped[1] = 0;
        }
        return HUSLColorConverter.rgbToInt(HUSLColorConverter.huslToRgb(clamped));
    }

    private void updateComplice() {
        logIntoComplice.setVisibility((compliceWaitingTask != null || Complice.get().isLoggedIn())
                                    ? View.GONE : View.VISIBLE);
        logIntoComplice.setVisibility((compliceWaitingTask != null || Complice.get().isLoggedIn())
                ? View.GONE : View.VISIBLE);

        if (compliceWaitingTask != null) {
            thebutton.getBackground().setColorFilter(BUTTON_COLOR_ALPHA | squashColor(compliceWaitingTask.getColor()), PorterDuff.Mode.MULTIPLY);
            label.setTextColor(0xff000000 | squashColor(compliceWaitingTask.getColor()));
            setButtonText(compliceWaitingTask.getGoText());
            compliceNevermind.setVisibility(View.VISIBLE);
            compliceTaskLabel.setVisibility(View.GONE);
            compliceApplyTask.setVisibility(View.GONE);
        } else {
            label.setHighlightColor(ContextCompat.getColor(this, R.color.colorAccent));
            thebutton.getBackground().clearColorFilter();
            setButtonText(null);
            compliceNevermind.setVisibility(View.GONE);
            if (availableCompliceTask != null) {
                compliceTaskLabel.setTextColor(0xff000000 | squashColor(availableCompliceTask.getColor()));
                compliceTaskLabel.setText(availableCompliceTask.getLabel());
                compliceTaskLabel.setVisibility(View.VISIBLE);
                compliceApplyTask.setVisibility(View.VISIBLE);
            } else {
                compliceTaskLabel.setVisibility(View.GONE);
                compliceApplyTask.setVisibility(View.GONE);
            }
        }
    }

    private void setButtonText(@Nullable String s) {
        if (s == null) {
            thebutton.setText(R.string.set_alarm);
        } else {
            thebutton.setText(s);
        }
    }

    @OnClick(R.id.complice_log_in)
    public void logIntoComplice() {
        setWaitingCompliceTask(new CompliceLoginTask(this));
    }

    @OnClick(R.id.complice_nevermind_task)
    public void clearWaitingComplice() {
        setWaitingCompliceTask(null);
        if (previousMinutesValue != null) {
            minutesEditor.setText(previousMinutesValue);
            previousMinutesValue = null;
        }
        if (previousLabelValue != null) {
            label.setText(previousLabelValue);
            previousLabelValue = null;
        }
    }

    @OnClick(R.id.complice_apply_action)
    public void applyAvailableCompliceTask() {
        if (availableCompliceTask == null) {
            updateComplice();
            return;
        }

        setWaitingCompliceTask(availableCompliceTask);
    }

    private void setWaitingCompliceTask(CompliceTask compliceTask) {
        this.compliceWaitingTask = compliceTask;
        if (compliceTask != null) {
            if (compliceTask.getRecommendedTime() != null) {
                previousMinutesValue = minutesEditor.getText().toString();
                minutesEditor.setText(String.format("%d", compliceTask.getRecommendedTime()));
            }
            previousLabelValue = label.getText().toString();
            label.setText(compliceTask.getLabel());
        }
        settings.setLastWaitingCompliceTask(compliceTask);
        updateComplice();
    }

    private boolean startCompliceTask() {
        boolean intentLaunched = false;
        if (compliceWaitingTask != null) {
            intentLaunched = compliceWaitingTask.startAction(this);
            settings.setLastWaitingCompliceTask(null);
            settings.setCurrentActiveCompliceTask(compliceWaitingTask);
            compliceWaitingTask = null;
        } else {
            settings.setCurrentActiveCompliceTask(null);
        }
        return intentLaunched;
    }

    private boolean shouldShowSleepButtons() {
        DateTime now = DateTime.now(DateTimeZone.getDefault());
        Log.i(TAG, "now: " + now);
        return now.getHourOfDay() < 5 || now.getHourOfDay() >= 9 + 12;
    }

    @OnTextChanged(R.id.timerminutes)
    public void onTimeEdit() {
        final String text = minutesEditor.getText().toString();
        Settings settings = new Settings(this);
        settings.setLastMinutesText(text);
        try {
            Pattern pattern = Pattern.compile("^\\s*(?:([0-9]*)\\s*:)?\\s*([0-9]*)\\s*$");
            Matcher matcher = pattern.matcher(text);
            if (!matcher.matches()) {
                Log.v(TAG, "matcher doesn't match: '" + text + "'");
                return;
            }
            String minutes = matcher.group(2);
            String hours = matcher.group(1);
            Log.v(TAG, "Minutes: " + minutes + ", hours: " + hours);
            timerMinutes = Integer.valueOf(minutes);
            if (hours != null && !hours.isEmpty()) {
                timerMinutes += Integer.valueOf(hours) * 60;
            }
        } catch (NumberFormatException e) {
            timerMinutes = null;
        }
    }

    @OnTextChanged(R.id.label)
    public void onLabelEdit() {
        final String text = label.getText().toString();
        Settings settings = new Settings(this);
        settings.setLastTitleText(text);
    }

    public void onWakeTimeEdit() {
        final String text = waketime.getText().toString();
        Settings settings = new Settings(this);
        settings.setLastWakeTimeText(text);
        try {
            LocalTime parsed = format.parseLocalTime(text);
            DateTime now = DateTime.now(DateTimeZone.getDefault());
            DateTime result = now.withTime(parsed);
            int expiry = 0;
            while (result.isBefore(now) && expiry < 10) {
                result = result.plusDays(1).withTime(parsed);
                expiry += 1;
            }
            nextwake = result;
        } catch (IllegalArgumentException e) {
            nextwake = null;
        }
    }

    @OnClick(R.id.sleep_until)
    public void onSleepClicked() {
        onWakeTimeEdit();
        onLabelEdit();
        if (nextwake == null) {
            Toast.makeText(this, "Invalid wake time", Toast.LENGTH_SHORT).show();
            return;
        }
        Minutes minutes = Minutes.minutesBetween(DateTime.now(DateTimeZone.getDefault()), nextwake);
        timerMinutes = minutes.getMinutes();
        setWaitingCompliceTask(null);
        go(false);
    }

    @OnClick(R.id.thebutton)
    public void onClicked() {
        onLabelEdit();
        onTimeEdit();
        go(startCompliceTask());
    }

    private void go(boolean activityStartHandled) {
        if (timerMinutes == null) {
            Toast.makeText(this, "Invalid duration", Toast.LENGTH_SHORT).show();
            return;
        }
        Timers.setMainAlarm(this, timerMinutes, label.getText().toString());
        Toast.makeText(this, "Timer set for " + timerMinutes.toString()
               + " minutes.", Toast.LENGTH_SHORT).show();
        Settings settings = new Settings(this);
        settings.setLastMinutesText(null);
        settings.setLastTitleText(null);
        clearWaitingComplice();
        if (!activityStartHandled) {
            AcknowledgeActivity.launch(MainActivity.this);
            finish();
        }
    }

    @OnClick(R.id.acknowledgebutton)
    public void onAcknowledgeClicked() {
        Timers.ackBotherAlarm(this);
        setNextCountDown(50);
        acknowledgeButton.setVisibility(View.GONE);
    }
}