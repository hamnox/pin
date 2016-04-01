package hamlah.pin.complice;

import android.util.Log;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.regex.Pattern;

import hamlah.pin.App;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@JsonObject
public class CompliceRemoteTask extends CompliceTask {

    private static final String TAG = CompliceRemoteTask.class.getSimpleName();

    @JsonField
    String id;

    @JsonField
    int goalCode;

    CompliceRemoteTask() {

    }

    public CompliceRemoteTask(int color, String label, String id, int goalCode) {
        super(color, null, null, label);
        this.id = id;
        this.goalCode = goalCode;
    }

    public int getGoalCode() {
        return goalCode;
    }

    public String getId() {
        return id;
    }

    @Override
    public Integer getRecommendedTime() {
        if (this.recommendedTime != null) {
            return recommendedTime;
        }
        Pattern timesExplicit = Pattern.compile("\\([^0-9]*\\)");
        return 5;
    }

    @Override
    public void endAction(boolean isComplete) {
        if (isComplete) {
            Complice.get().finishAction(this)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "ERRORror", e);
                        }

                        @Override
                        public void onNext(String s) {
                            App.app().bus.post(new CompliceTaskChangedEvent());
                            Log.i(TAG, "response: " + s);
                        }
                    });
        }
    }
}
