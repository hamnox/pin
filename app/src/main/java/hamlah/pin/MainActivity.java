package hamlah.pin;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import hamlah.pin.service.CountdownService;
import hamlah.pin.service.Settings;
import hamlah.pin.service.Timers;


public class MainActivity extends AppCompatActivity {
    //private AlarmManager alarmMgr;
    //private PendingIntent alarmIntent;
    @Bind(R.id.timerminutes)
    EditText minutesEditor;

    @Bind(R.id.label)
    EditText label;

    @Bind(R.id.bother_countdown)
    TextView botherCountdown;

    @Bind(R.id.acknowledgebutton)
    Button acknowledgeButton;

    @Nullable
    private Integer timerMinutes;

    private Handler handler = new Handler();

    private Runnable countdownCallback;

    private void setNextCountDown(long time) {
        //// TODO: 2/2/16 get real num
        handler.postDelayed(countdownCallback, time);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerMinutes = 10;
        ButterKnife.bind(this);
        // TODO: new Settings(this).verifyAlarms();
    }

    @Override
    protected void onPause() {
        super.onPause();
        countdownCallback = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timers.armBotherAlarm(this, "mainactivity");
        countdownCallback = new Runnable() {
            @Override
            public void run() {
                if (this != countdownCallback) {
                    return;
                }
                Settings settings = new Settings(MainActivity.this);
                if (settings.main.isCounting()) {
                    Intent intent = new Intent(MainActivity.this, AcknowledgeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
                    timeUntilNext = 500;
                } else {
                    botherCountdown.setVisibility(View.VISIBLE);
                }
                botherCountdown.setText(CountdownService.formatTime(timeUntilNext, true));
                setNextCountDown(timeUntilNext % 1000);
            }
        };
        setNextCountDown(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnTextChanged(R.id.timerminutes)
    public void onTimeEdit() {
        try {
            timerMinutes = Integer.valueOf(minutesEditor.getText().toString());
        } catch (NumberFormatException e) {
            timerMinutes = null;
        }
        // save in shared preferences or something
    }

    @OnClick(R.id.thebutton)
    public void onClicked() {
        if (timerMinutes == null) {
            Toast.makeText(this, "Invalid Integer", Toast.LENGTH_SHORT).show();
            return;
        }
        Timers.setMainAlarm(this, timerMinutes, label.getText().toString());
        Toast.makeText(this, "Timer set for " + timerMinutes.toString()
               + " minutes.", Toast.LENGTH_SHORT).show();
        finish();
    }

    /** @OnClick(R.id.stopbutton)
    public void onStopperClicked() {
        MainTimerReceiver.cancelAlarm(this);
    } **/

    @OnClick(R.id.acknowledgebutton)
    public void onAcknowledgeClicked() {
        Timers.ackBotherAlarm(this);
        setNextCountDown(250);
        acknowledgeButton.setVisibility(View.GONE);
    }
}