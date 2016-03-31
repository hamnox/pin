package hamlah.pin.complice;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import hamlah.pin.AcknowledgeActivity;
import hamlah.pin.R;
import hamlah.pin.service.Settings;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

        Complice.get().completeLogin(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    public void onCompleted() {
                        // TODO: what happens if config change
                        // TODO: I'm sure there are like 10 ways for the lifecycle to break this, it's 2 am
                        if (!isFinishing()) {
                            finish();
                        }
                    }

                    public void onError(Throwable e) {
                        Log.i(TAG, "error", e);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        CompliceTask activeCompliceTask = settings.getCurrentActiveCompliceTask();
                        if (activeCompliceTask instanceof CompliceLoginTask
                                && (settings.main.isTriggered() || settings.main.isCounting())) {
                            AcknowledgeActivity.completeMainAlarm(CompliceLoginActivity.this, true);
                        } else {
                            AcknowledgeActivity.launch(CompliceLoginActivity.this);
                        }
                        finish();
                        Log.i(TAG, "Login done! " + activeCompliceTask + ", " + settings.main.isTriggered() + ", " + settings.main.isCounting());
                    }
                });
        // this is actually just a broadcast receiver
        // disguised as an activity
        // it's midnight don't ask questions
        // blows raspberry
        //finish();
    }
}
