package hamlah.pin;

import android.os.Handler;
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


public class MainActivity extends AppCompatActivity {
    //private AlarmManager alarmMgr;
    //private PendingIntent alarmIntent;
    @Bind(R.id.timerminutes)
    EditText minutesEditor;

    @Bind(R.id.bother_countdown)
    TextView botherCountdown;

    @Bind(R.id.acknowledgebutton)
    Button acknowledgeButton;

    private Integer timerMinutes;

    private Handler handler = new Handler();

    private Runnable countdownCallback;

    private void setNextCountDown(long time) {
        //// TODO: 2/2/16 get real num
        cancelCountdown();
        handler.postDelayed(countdownCallback, time);
    }

    private void cancelCountdown() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerMinutes = 10;

        ButterKnife.bind(this);
        MainTimerReceiver.setPinOpen(true);
    }

    @Override
    protected void onPause() {
        MainTimerReceiver.setPinOpen(false);
        super.onPause();
        countdownCallback = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainTimerReceiver.setPinOpen(true);
        BotherBotherReceiver.setAlarm(this);
        countdownCallback = new Runnable() {
            @Override
            public void run() {
                if (this != countdownCallback) {
                    return;
                }
                if (new Settings(MainActivity.this).isBotherAlarmTriggered()) {
                    botherCountdown.setVisibility(View.GONE);
                    acknowledgeButton.setVisibility(View.VISIBLE);
                    return;
                }
                long timeUntilNext = BotherBotherReceiver.getTimeUntilNext(MainActivity.this);
                if (timeUntilNext < 0) {
                    botherCountdown.setVisibility(View.GONE);
                    timeUntilNext = 500;
                } else {
                    botherCountdown.setVisibility(View.VISIBLE);
                }
                botherCountdown.setText("Time Left: " + timeUntilNext / 1000 + 1);
                setNextCountDown(timeUntilNext % 1000);
            }
        };
        setNextCountDown(0);
    }

    @Override
    protected void onDestroy() {
        MainTimerReceiver.setPinOpen(false);
        super.onDestroy();
    }

    @OnTextChanged(R.id.timerminutes)
    public void onTimeEdit() {
        try {
            timerMinutes = Integer.valueOf(minutesEditor.getText().toString());
        } catch (NumberFormatException e) {
            minutesEditor.setText(timerMinutes.toString());
        }
        // save in shared preferences or something
    }

    @OnClick(R.id.thebutton)
    public void onClicked() {
        MainTimerReceiver.setAlarm(this, timerMinutes);
        Toast.makeText(this, "Timer set for " + timerMinutes.toString()
               + " minutes.", Toast.LENGTH_SHORT).show();
        BotherBotherReceiver.cancelAlarm(this);
    }

    @OnClick(R.id.stopbutton)
    public void onStopperClicked() {
        MainTimerReceiver.cancelAlarm(this);
    }

    @OnClick(R.id.acknowledgebutton)
    public void onAcknowledgeClicked() {
        AsyncRingtonePlayer.getAsyncRingtonePlayer(this).stop();
        new Settings(this).setBotherAlarmTriggered(false);
        BotherBotherReceiver.setAlarm(this);
        setNextCountDown(0);
        acknowledgeButton.setVisibility(View.GONE);
    }
}