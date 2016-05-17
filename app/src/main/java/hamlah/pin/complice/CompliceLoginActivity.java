package hamlah.pin.complice;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import hamlah.pin.AcknowledgeActivity;
import hamlah.pin.App;
import hamlah.pin.R;
import hamlah.pin.service.Settings;

public class CompliceLoginActivity extends Activity {

    private static final String TAG = CompliceLoginActivity.class.getSimpleName();
    Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(this);
        setContentView(R.layout.activity_complice_login);
        Intent intent = getIntent();
        if (intent == null || intent.getData() == null) {
            finish();
            return;
        }
        Uri data = intent.getData();
        Log.i(TAG, "Uri: " + data);

        App.wrap(Complice.get().completeLogin(data)).subscribe(b -> {
            CompliceTask activeCompliceTask = settings.getCurrentActiveCompliceTask();
            if (activeCompliceTask instanceof CompliceLoginTask
                    && (settings.main.isTriggered() || settings.main.isCounting())) {
                AcknowledgeActivity.completeMainAlarm(this, true);
            } else {
                AcknowledgeActivity.launch(this);
            }
            finish();
            Log.i(TAG, "Login done! " + activeCompliceTask
                    + ", " + settings.main.isTriggered()
                    + ", " + settings.main.isCounting());
        });
        // this is actually just a broadcast receiver
        // disguised as an activity
        // it's midnight don't ask questions
        // blows raspberry
    }
}
