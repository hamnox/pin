package hamlah.pin.complice;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class CompliceTaskJsonWrapper {

    @JsonField
    CompliceLoginTask loginTask;

    @JsonField
    CompliceTask genericTask;

    CompliceTaskJsonWrapper() {

    }

    CompliceTaskJsonWrapper(CompliceTask task) {
        if (task instanceof CompliceLoginTask) {
            loginTask = (CompliceLoginTask) task;
        } else {
            genericTask = task;
        }
    }

    public CompliceTask get() {
        if (loginTask != null) {
            return loginTask;
        }
        if (genericTask != null) {
            return genericTask;
        }
        throw new NullPointerException();
    }
}
