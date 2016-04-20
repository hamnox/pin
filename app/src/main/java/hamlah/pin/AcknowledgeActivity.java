package hamlah.pin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
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
import hamlah.pin.complice.CompliceTask;
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

    private Settings settings;

    private void setNextCountDown(long time) {
        handler.postDelayed(countdownCallback, time);
    }

    @OnClick(R.id.offbutton)
    public void onOffClicked() {
        complete(true);
    }

    public static void completeMainAlarm(Context context, boolean complete) {
        Timers.ackMainAlarm(context);
        Settings settings = new Settings(context);
        CompliceTask task = settings.getCurrentActiveCompliceTask();
        if (task != null) {
            task.endAction(complete);
        }
        settings.setCurrentActiveCompliceTask(null);
        MainActivity.launch(context);
    }

    private void complete(boolean isComplete) {
        completeMainAlarm(this, isComplete);
        finish();
    }

    @OnClick(R.id.mark_did_something_else)
    public void markDidSomethingElse() {
        Timers.log("did_something_else", "main", null, null, this);
        complete(false);
    }

    @OnClick(R.id.mark_distracted)
    public void markDistracted() {
        Timers.log("distracted", "main", null, null, this);
        complete(false);
    }

    @OnClick(R.id.mark_typoed)
    public void markTypoed() {
        Timers.log("typoed_alarm_settings", "main", null, null, this);
        complete(false);
    }

    @OnClick(R.id.mark_underestimated)
    public void markUnderestimated() {
        Timers.log("underestimated_duration", "main", null, null, this);
        complete(false);
    }

    @OnClick(R.id.mark_partial)
    public void markSubtaskDone() {
        Timers.log("subtask_done", "main", null, null, this);
        complete(false);
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
        settings = new Settings(this);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        App.permissionResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.checkPermissions(this);
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
        updateComplice();
    }

    private void updateComplice() {
        CompliceTask task = settings.getCurrentActiveCompliceTask();
        if (task != null) {
            offbutton.getBackground().setColorFilter(0xff000000 | task.getMidSquashedColor(),
                    PorterDuff.Mode.MULTIPLY);
        } else {
            offbutton.getBackground().clearColorFilter();
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
}