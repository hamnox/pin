package hamlah.pin;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hamlah.pin.service.Settings;
import rx.functions.Action1;
import rx.functions.Func0;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    Settings settings;

    private Settings.AlarmSettings[] alarms;

    @Bind(R.id.show_complice)
    CheckBox _showCompliceCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = new Settings(this);
        alarms = new Settings.AlarmSettings[]{settings.bother, settings.main};
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        bindSetting(_showCompliceCheckbox, settings::setShowComplice, settings::getShowComplice);
    }

    private void bindSetting(CheckBox pref, Action1<Boolean> set, Func0<Boolean> get) {
        pref.setChecked(get.call());
        pref.setOnCheckedChangeListener((buttonView, isChecked) -> {
            set.call(isChecked);
        });
    }

    @OnClick(R.id.set_bother_alarm_sound)
    public void onBotherClick() {
        getSound(0, R.string.bother_alarm_sound, settings.bother.getSound());

    }

    @OnClick(R.id.set_main_alarm_sound)
    public void onMainClick() {
        getSound(1, R.string.main_alarm_sound, settings.main.getSound());
    }

    private void getSound(int index, int label, Uri current) {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(label));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        if (current == null) {
            current = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, current);
        startActivityForResult(intent, index + 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, R.string.seriouserror, Toast.LENGTH_LONG).show();
            Log.wtf(TAG, "Error: " + resultCode + ", " + data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        alarms[requestCode - 1000].setSound(uri);
        Toast.makeText(this, R.string.sound_changed, Toast.LENGTH_LONG).show();
    }
}
