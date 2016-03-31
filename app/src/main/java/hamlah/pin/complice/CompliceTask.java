package hamlah.pin.complice;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.io.IOException;

@JsonObject
public class CompliceTask {
    @JsonField
    int color;

    @JsonField
    String goText;

    @JsonField
    int recommendedTime;

    @JsonField
    String label;

    CompliceTask() {

    }

    public CompliceTask(int color, String goText, int recommendedTime, String label) {
        this.color = color;
        this.goText = goText;
        this.recommendedTime = recommendedTime;
        this.label = label;
    }

    public int getColor() {
        return color;
    }

    public String getGoText() {
        return goText;
    }

    public Integer getRecommendedTime() {
        return recommendedTime;
    }

    public String getLabel() {
        return label;
    }

    public boolean startAction(Context context) {
        return false;
    }

    public void endAction(boolean isComplete) {

    }

    public String toJson() throws IOException {
        return LoganSquare.serialize(new CompliceTaskJsonWrapper(this));
    }

    public static CompliceTask fromJson(String input) throws IOException {
        return LoganSquare.parse(input, CompliceTaskJsonWrapper.class).get();
    }
}
