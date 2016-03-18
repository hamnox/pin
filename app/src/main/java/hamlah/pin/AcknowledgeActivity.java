package hamlah.pin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.ButterKnife;
import butterknife.OnClick;
import hamlah.pin.service.Timers;

public class AcknowledgeActivity extends AppCompatActivity {

    @OnClick(R.id.offbutton)
    public void onOffClicked(){
        Timers.ackMainAlarm(this);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acknowledge_alarm);
        ButterKnife.bind(this);


    }
}