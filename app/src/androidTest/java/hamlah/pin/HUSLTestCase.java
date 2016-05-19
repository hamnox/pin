package hamlah.pin;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class HUSLTestCase {

    @JsonField
    double[] husl;
    @JsonField
    double[] huslp;
    @JsonField
    double[] lch;
    @JsonField
    double[] luv;
    @JsonField
    double[] rgb;
    @JsonField
    double[] xyz;
}
