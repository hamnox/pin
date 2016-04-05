package hamlah.pin;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.github.aurae.retrofit2.LoganSquareConverterFactory;
import com.squareup.otto.Bus;

import net.danlew.android.joda.JodaTimeAndroid;

import hamlah.pin.service.Settings;
import hamlah.pin.service.Timers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

public class App extends Application {
    private static final int PERMISSION_REQUEST = 67;
    private static boolean loggingEnabled = false;
    private static App app;
    private Retrofit http;
    public Bus bus;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        loggingEnabled = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        JodaTimeAndroid.init(this);

        Timers.go(this);
        new Settings(this).refreshAlarms();

        http = new Retrofit.Builder()
                .baseUrl("https://complice.co:443")
                .addConverterFactory(LoganSquareConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        bus = new MainThreadBus();
    }

    public static App app() {
        return app;
    }

    public Retrofit http() {
        return http;
    }

    public static void checkPermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                App.loggingEnabled = false;
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST);
            }
        }
    }

    public static void permissionResult(int requestCode, String[] resultCode, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    App.loggingEnabled = true;
                } else {
                    App.loggingEnabled = false;

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public static boolean canLog() {
        return loggingEnabled;
    }

    public class MainThreadBus extends Bus {
        private final Handler mHandler = new Handler(Looper.getMainLooper());

        @Override
        public void post(final Object event) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.post(event);
            } else {
                mHandler.post(() -> MainThreadBus.super.post(event));
            }
        }
    }
}
