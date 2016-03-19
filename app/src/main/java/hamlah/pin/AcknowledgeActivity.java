package hamlah.pin;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hamlah.pin.service.CountdownService;
import hamlah.pin.service.Settings;
import hamlah.pin.service.Timers;

public class AcknowledgeActivity extends AppCompatActivity {

    private Handler handler = new Handler();

    private Runnable countdownCallback;


    @Bind(R.id.countdown)
    TextView countdown;

    @Bind(R.id.offbutton)
    Button offbutton;

    @Bind(R.id.label)
    TextView label;

    private void setNextCountDown(long time) {
        handler.postDelayed(countdownCallback, time);
    }

    @OnClick(R.id.offbutton)
    public void onOffClicked(){
        Timers.ackMainAlarm(this);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acknowledge_alarm);
        ButterKnife.bind(this);


    }

    @Override
    protected void onPause() {
        super.onPause();
        countdownCallback = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Settings settings = new Settings(this);
        if (settings.main.getLabel() != null
                && (settings.main.isCounting() || settings.main.isTriggered())) {
            label.setVisibility(View.VISIBLE);
            label.setText(settings.main.getLabel());
        } else {
            label.setVisibility(View.GONE);
        }
        countdownCallback = new Runnable() {
            @Override
            public void run() {
                if (this != countdownCallback) {
                    return;
                }
                Settings settings = new Settings(AcknowledgeActivity.this);
                if (settings.main.isTriggered()) {
                    countdown.setVisibility(View.GONE);
                    offbutton.setText(R.string.alarm_off);
                    return;
                } else {
                    offbutton.setText(R.string.alarm_cancel);
                }
                long timeUntilNext = settings.main.remaining();
                if (timeUntilNext < 0) {
                    countdown.setVisibility(View.GONE);
                    timeUntilNext = 500;
                } else {
                    countdown.setVisibility(View.VISIBLE);
                }
                countdown.setText(CountdownService.formatTime(timeUntilNext, true));
                setNextCountDown(timeUntilNext % 1000);
            }
        };
        setNextCountDown(1);
    }
}