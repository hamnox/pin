package hamlah.pin;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AcknowledgeAlarm extends AppCompatActivity {
   // SoundPool soundPool = new SoundPool.Builder().setAudioAttributes(AudioAttributes.USAGE_ALARM).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acknowledge_alarm);


    }
}