package hamlah.pin;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.Bind;
import butterknife.OnClick;

public class AcknowledgeActivity extends AppCompatActivity {
   // SoundPool soundPool = new SoundPool.Builder().setAudioAttributes(AudioAttributes.USAGE_ALARM).build();


    @OnClick(R.id.offbutton)
    public void onOffClicked(){
        AsyncRingtonePlayer.getAsyncRingtonePlayer(this).stop();
        Intent mainIntent = new Intent(this, AcknowledgeActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(mainIntent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acknowledge_alarm);



    }
}