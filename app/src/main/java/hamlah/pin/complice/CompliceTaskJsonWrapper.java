package hamlah.pin.complice;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class CompliceTaskJsonWrapper {

    @JsonField
    CompliceLoginTask loginTask;

    @JsonField
    CompliceRemoteTask remoteTask;

    @JsonField
    CompliceEditTask editTask;

    CompliceTaskJsonWrapper() {

    }

    CompliceTaskJsonWrapper(CompliceTask task) {
        if (task instanceof CompliceLoginTask) {
            loginTask = (CompliceLoginTask) task;
        } else if (task instanceof CompliceRemoteTask) {
            remoteTask = (CompliceRemoteTask) task;
        } else if (task instanceof CompliceEditTask) {
            editTask = (CompliceEditTask) task;
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
        if (editTask != null) {
            return editTask;
        }
        throw new NullPointerException();
    }
}
