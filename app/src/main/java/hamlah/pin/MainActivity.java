package hamlah.pin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;


public class MainActivity extends AppCompatActivity {
    //private AlarmManager alarmMgr;
    //private PendingIntent alarmIntent;
    @Bind(R.id.timerminutes) EditText minutesEditor;
    private Integer timerMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerMinutes = 10;

        AlarmReceiver.setAlarm(this);
        ButterKnife.bind(this);
        AlarmReceiver.setPinOpen(true);
    }

    @Override
    protected void onPause() {
        AlarmReceiver.setPinOpen(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        AlarmReceiver.setPinOpen(true);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        AlarmReceiver.setPinOpen(false);
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
        AlarmReceiver.setAlarm(this, timerMinutes);
       Toast.makeText(MainActivity.this, "Timer set for " + timerMinutes.toString()
               + " minutes.", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.stopbutton)
    public void onStopperClicked() {
        AlarmReceiver.cancelAlarm(this);
    }
}