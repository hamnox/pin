package hamlah.pin.complice;

import android.graphics.Color;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
@SuppressWarnings("unused")
class CurrentTaskResponse {
    @JsonField(name="nexa")
    NextAction nextAction;

    @JsonField(name="remainingcount")
    int remainingCount;

    @JsonField(name="remainingDenominator")
    int totalIntentions;

    @JsonField(name="linkifiedNexaHtml")
    @Nullable
    String _linkNextActionText;

    @JsonField(name="goalName")
    @Nullable
    String goalName;

    @JsonField(name = "colors")
    @Nullable
    ActionColors colors;


    /// above fields are filled if we have tasks. these are filled if we don't.



    @JsonField(name="username")
    @Nullable
    String username;

    @JsonField(name="noIntentions")
    boolean noIntentions = false;

    @JsonField(name="submitOutcomes")
    boolean submitOutcomes = false;

    @JsonObject
    static class NextAction {
        @JsonField(name="code")
        String goalCode;

        @JsonField(name = "text")
        String text;

        @JsonField(name = "done")
        boolean done;

        @JsonField(name = "id")
        String id;
    }

    @JsonObject
    static class ActionColors {
        @JsonField(name = "color")
        String color;

        @JsonField(name = "lightc")
        String lightColor;

        @JsonField(name = "whitishc")
        String whitishColor;

        @JsonField(name = "darkc")
        String darkColor;

        @JsonField(name = "greyc")
        String greyColor;

        public Integer getIntColor() {
            return Color.parseColor(color);
        }

        public Integer getIntLightColor() {
            return Color.parseColor(lightColor);
        }

        public Integer getIntWhitishColor() {
            return Color.parseColor(whitishColor);
        }

        public Integer getIntDarkColor() {
            return Color.parseColor(darkColor);
        }

        public Integer getIntGreyColor() {
            return Color.parseColor(greyColor);
        }
    }
}
