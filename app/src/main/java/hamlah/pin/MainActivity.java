package hamlah.pin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Environment;
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
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
//import hamlah.pin.service.CountdownService;
import hamlah.pin.service.Settings;
import hamlah.pin.service.Timers;


public class MainActivity extends AppCompatActivity {
    //private AlarmManager alarmMgr;
    //private PendingIntent alarmIntent;
    private static final String defaultWakeTime = "7:00";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int BUTTON_COLOR_ALPHA = 0xff000000;
    private static boolean isResumed = false;

    private Random rand;
    private int randcount = 0;

//    @Bind(R.id.timerminutes)
//    EditText minutesEditor;

//    @Bind(R.id.label)
//    TextInputEditText label;

    @Bind(R.id.bug_text)
    EditText bug_text;

//    @Bind(R.id.waketime)
//    EditText waketime;
//
//    @Bind(R.id.bother_countdown)
//    TextView botherCountdown;
//
//    @Bind(R.id.acknowledgebutton)
//    Button acknowledgeButton;
//
//    @Bind(R.id.sleep_until)
//    Button sleepButton;
//
//    @Bind(R.id.thebutton)
//    Button thebutton;


    @Bind(R.id.numdisplay)
    TextView nshow;

    @Bind(R.id.numpick)
    NumberPicker np;


    @OnClick(R.id.numbutton)
    public void onNumDisplayClick() {
        randcount += 1;
        if (randcount > 10) {
            nshow.setText("Out: ".toString());
            randcount = 1;
        }
        int val = rand.nextInt(np.getValue()) + 1;
        nshow.setText(nshow.getText() + " " + String.valueOf(val));
    }

    @Nullable
    private Integer timerMinutes;

    private Handler handler = new Handler();

    private Runnable countdownCallback;

    private static DateTimeFormatter format = DateTimeFormat.forPattern("H:m");
    @Nullable
    private DateTime nextwake;


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

        rand = new Random();

        np.setMinValue(1);
        np.setMaxValue(32);
        np.setValue(2);
        np.setWrapSelectorWheel(true);

        nshow.setText("Out: ".toString());
        np.setOnValueChangedListener((picker, oldVal, newVal) -> {
            // Nothing!
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.app().bus.unregister(this);
        countdownCallback = null;
        isResumed = false;
        Log.i(TAG, "paused");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

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
    @OnClick(R.id.settings)
    public void showSettings() {
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
        /*
        App.checkPermissions(this);
        App.app().bus.register(this);
        isResumed = true;
        Log.i(TAG, "resumed");
        Timers.armBotherAlarm(this, "mainactivity");

        settings = new Settings(this);
        if (settings.main.isCounting() || settings.main.isTriggered()) {
            AcknowledgeActivity.launch(MainActivity.this);
            finish();
            return;
        }

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
*/
        String lastBugText = settings.getLastBugText();
        if (lastBugText != null) {
            bug_text.setText(lastBugText);
        } else {
            bug_text.setText("");
        }
/*
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
        */
    }

    /*
    private void setButtonText(@Nullable String s) {
        if (s == null) {
            thebutton.setText(R.string.set_alarm);
        } else {
            thebutton.setText(s);
        }
    }

    private boolean shouldShowSleepButtons() {
        DateTime now = DateTime.now(DateTimeZone.getDefault());
        Log.i(TAG, "now: " + now);
        return true;
//        return now.getHourOfDay() < 5 || now.getHourOfDay() >= 9 + 12;
    }
    /*

    @OnClick(R.id.sleep_until)
    public void onSleepClicked() {
            if (nextwake == null) {
                    Toast.makeText(this, "Invalid wake time", Toast.LENGTH_SHORT).show();
                    return;
                }
            Minutes minutes = Minutes.minutesBetween(DateTime.now(DateTimeZone.getDefault()), nextwake);
            timerMinutes = minutes.getMinutes();
            onClicked();
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

*/
    @OnTextChanged(R.id.bug_text)
    public void onBugEdit() {
        final String text = bug_text.getText().toString();
        Settings settings = new Settings(this);
        settings.setLastBugText(text);
    }
/*
    @OnTextChanged(R.id.waketime)
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
            Log.e(TAG, "aargggh");
            nextwake = null;
        }
    }
*/
    private void go() {
        if (timerMinutes == null) {
            Toast.makeText(this, "Invalid duration", Toast.LENGTH_SHORT).show();
            return;
        }
        // Timers.setMainAlarm(this, timerMinutes, label.getText().toString());
        Toast.makeText(this, "Timer nonexistant", Toast.LENGTH_SHORT).show();
        Settings settings = new Settings(this);
        settings.setLastMinutesText(null);
        settings.setLastTitleText(null);
        //AcknowledgeActivity.launch(MainActivity.this);
    }

/*


    @OnClick(R.id.thebutton)
    public void onClicked() {
        onLabelEdit();
        go();
    }

    @OnClick(R.id.acknowledgebutton)
    public void onAcknowledgeClicked() {
        //Timers.ackBotherAlarm(this);
        setNextCountDown(50);
        acknowledgeButton.setVisibility(View.GONE);
    }
*/
    @OnClick(R.id.bug_button)
    public void onSubmitBugClicked() {
        bugLog(bug_text.getText().toString(), MainActivity.this);
        Settings settings = new Settings(MainActivity.this);
        settings.setLastBugText(null);
        bug_text.setText("");
    }


    public static synchronized void bugLog(String bug, Context context) {
        PrintWriter out = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());

        try {
            File external = Environment.getExternalStorageDirectory();
            File filepath = new File(external, "stride_bugs.log");
            out = new PrintWriter(new BufferedWriter(new FileWriter(filepath, true)));
            Date result_date = new Date(System.currentTimeMillis());

            final String formatted = String.format("%s %s", sdf.format(result_date), bug).trim();

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
}