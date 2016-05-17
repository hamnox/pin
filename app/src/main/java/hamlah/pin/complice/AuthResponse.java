package hamlah.pin.complice;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class AuthResponse {
    @JsonField(name="access_token")
    String accessToken;
}
