package hamlah.pin.complice;

import android.content.Context;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.io.IOException;

import hamlah.pin.HUSLColorConverter;

@JsonObject
public class CompliceTask {
    @JsonField
    int color;

    @JsonField
    String goText;

    @JsonField
    Integer recommendedTime;

    @JsonField
    String label;

    CompliceTask() {

    }

    public CompliceTask(int color, String goText, Integer recommendedTime, String label) {
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

    private double smoothclamp(double in, double newcenter, double curvature, double scale) {
        return Math.tanh((in - 50) / (50 * curvature)) * (scale * 50) + (100 * newcenter);
    }

    private int squashColor(int incolor) {
        double[] husl = HUSLColorConverter.rgbToHusl(HUSLColorConverter.intToRgb(incolor));
        double[] clamped = {
                husl[0],
                smoothclamp(husl[1], 0.6, 1, 0.3),
                smoothclamp(husl[2], 0.65, 0.9, 0.2)
        };
        if (husl[1] < 1) {
            // in case of zero saturation, let's not violently boost it.
            clamped[1] = 0;
        }
        return HUSLColorConverter.rgbToInt(HUSLColorConverter.huslToRgb(clamped));
    }

    public int getSquashedColor() {
        return squashColor(getColor());
    }
}
