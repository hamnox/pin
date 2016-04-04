package hamlah.pin.complice;

import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Toast;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hamlah.pin.App;
import hamlah.pin.R;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@JsonObject
public class CompliceRemoteTask extends CompliceTask {

    private static final String TAG = CompliceRemoteTask.class.getSimpleName();
    public static final Pattern TIME_PATTERN = Pattern.compile("([0-9]+)\\s*(?:(m|mins?|minutes?)|(hs?|hours?))(?![a-z])\\s*[,.;!]?\\s*");
    public static final Pattern PARENS_PATTERN = Pattern.compile("\\(([^()]*)\\)\\s*");

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

    public Pair<Integer,String> parse() {
        if (this.recommendedTime != null) {
            return new Pair<>(recommendedTime, this.label);
        }
        for (MatchResult match : allMatches(PARENS_PATTERN, this.label)) {
            Pair<Integer,String> result = parseTimeField(match.group(1));
            if (result.first != null) {
                if (result.second.equals("")) {
                    String removed = this.label.substring(0, match.start())
                            + this.label.substring(match.end(), this.label.length());
                    result = new Pair<>(result.first, removed.trim());
                }
                return result;
            }
        }
        return parseTimeField(this.label);
    }

    @Override
    public Integer getRecommendedTime() {
        return parse().first;
    }

    @Override
    public String getLabel() {
        return parse().second;
    }

    private Pair<Integer,String> parseTimeField(String timeField) {
        Integer total = null;
        String filtered = "";
        int lastresult = 0;
        for (MatchResult result : allMatches(TIME_PATTERN, timeField)) {
            filtered += timeField.substring(lastresult, result.start());
            lastresult = result.end();
            int count = Integer.parseInt(result.group(1));
            if (result.group(3) != null) {
                count *= 60;
            }
            if (total == null) {
                total = 0;
            }
            total += count;
        }
        filtered = filtered + timeField.substring(lastresult, timeField.length());
        return new Pair<>(total, filtered);
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

                            Toast.makeText(App.app(), R.string.error_finishing, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(String s) {
                            App.app().bus.post(new CompliceTaskChangedEvent());
                            Log.i(TAG, "response: " + s);
                        }
                    });
        }
    }

    /**
     * from http://stackoverflow.com/a/6020436/1102705
     */
    private static Iterable<MatchResult> allMatches(
            final Pattern p, final CharSequence input) {
        return new Iterable<MatchResult>() {
            public Iterator<MatchResult> iterator() {
                return new Iterator<MatchResult>() {
                    // Use a matcher internally.
                    final Matcher matcher = p.matcher(input);
                    // Keep a match around that supports any interleaving of hasNext/next calls.
                    MatchResult pending;

                    public boolean hasNext() {
                        // Lazily fill pending, and avoid calling find() multiple times if the
                        // clients call hasNext() repeatedly before sampling via next().
                        if (pending == null && matcher.find()) {
                            pending = matcher.toMatchResult();
                        }
                        return pending != null;
                    }

                    public MatchResult next() {
                        // Fill pending if necessary (as when clients call next() without
                        // checking hasNext()), throw if not possible.
                        if (!hasNext()) { throw new NoSuchElementException(); }
                        // Consume pending so next call to hasNext() does a find().
                        MatchResult next = pending;
                        pending = null;
                        return next;
                    }

                    /** Required to satisfy the interface, but unsupported. */
                    public void remove() { throw new UnsupportedOperationException(); }
                };
            }
        };
    }
}
