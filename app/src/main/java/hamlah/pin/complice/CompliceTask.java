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

    /**
     * Clamp values ranged 0-100 smoothly, with tanh.
     * @param in starting value
     * @param newcenter the original center is considered to be at 0.5; this will be the new center.
     * @param curvature lower values produce sharper curves.
     * @param scale simple multiplier for the resulting value. If this is 1 and newcenter is 0.5, the new range will be the same, just curved.
     * @return
     */
    private double smoothclamp(double in, double newcenter, double curvature, double scale) {
        return Math.tanh((in - 50) / (50 * curvature)) * (scale * 50) + (100 * newcenter);
    }

    private int squashColor(int incolor, double brightness, double saturation) {
        double brightnessScale = 0.2;
        double saturationScale = 0.3;
        if (!(brightness <= 1 - brightnessScale && brightness >= brightnessScale)) {
            throw new AssertionError();
        }
        if (!(saturation <= 1 - saturationScale && saturation >= saturationScale)) {
            throw new AssertionError();
        }
        double[] husl = HUSLColorConverter.rgbToHusl(HUSLColorConverter.intToRgb(incolor));
        double[] clamped = {
                husl[0],
                smoothclamp(husl[1], saturation, 1, saturationScale),
                smoothclamp(husl[2], brightness, 0.9, brightnessScale)
        };
        if (husl[1] < 1) {
            // in case of zero saturation, let's not violently boost it.
            clamped[1] = 0;
        }
        return HUSLColorConverter.rgbToInt(HUSLColorConverter.huslToRgb(clamped));
    }

    public int getDarkSquashedColor() {
        return squashColor(getColor(), 0.3, 0.32);
    }

    public int getMidSquashedColor() {
        return squashColor(getColor(), 0.65, 0.68);
    }
}
