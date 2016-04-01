package hamlah.pin.complice;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class CompliceTaskJsonWrapper {

    @JsonField
    CompliceLoginTask loginTask;

    @JsonField
    CompliceRemoteTask remoteTask;

    CompliceTaskJsonWrapper() {

    }

    CompliceTaskJsonWrapper(CompliceTask task) {
        if (task instanceof CompliceLoginTask) {
            loginTask = (CompliceLoginTask) task;
        } else if (task instanceof CompliceRemoteTask) {
            remoteTask = (CompliceRemoteTask) task;
        } else {
            throw new RuntimeException("Can't use generic complice tasks, sorry. if logansquare let us make it abstract we would");
        }
    }

    public CompliceTask get() {
        if (loginTask != null) {
            return loginTask;
        }
        if (remoteTask != null) {
            return remoteTask;
        }
        throw new NullPointerException();
    }
}
