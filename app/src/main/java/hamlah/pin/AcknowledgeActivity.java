package hamlah.pin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = AcknowledgeActivity.class.getSimpleName();
    private static boolean isResumed = false;
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
        MainActivity.launch(this);
        finish();
    }

    @OnClick(R.id.mark_did_something_else)
    public void markDidSomethingElse() {
        Timers.log("did_something_else", "main", null, null, this);
        onOffClicked();
    }

    @OnClick(R.id.mark_distracted)
    public void markDistracted() {
        Timers.log("distracted", "main", null, null, this);
        onOffClicked();
    }

    @OnClick(R.id.mark_failed)
    public void markFailed() {
        Timers.log("failed", "main", null, null, this);
        onOffClicked();
    }

    @OnClick(R.id.mark_typoed)
    public void markTypoed() {
        Timers.log("typoed_alarm_settings", "main", null, null, this);
        onOffClicked();
    }


    public static void launch(Context context) {
        Log.i(TAG, "Launching, resumed: " + isResumed);
        if (isResumed) {
            return;
        }
        Intent intent = new Intent(context, AcknowledgeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
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
        isResumed = false;
        Log.i(TAG, "paused, resumed: " + isResumed);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        Log.i(TAG, "resumed, resumed: " + isResumed);
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