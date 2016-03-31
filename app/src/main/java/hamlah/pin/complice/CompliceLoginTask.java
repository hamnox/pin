package hamlah.pin.complice;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;

import com.bluelinelabs.logansquare.annotation.JsonObject;

import hamlah.pin.R;

@JsonObject
public class CompliceLoginTask extends CompliceTask {
    CompliceLoginTask() {

    }

    public CompliceLoginTask(Context context) {
        super(ContextCompat.getColor(context, R.color.complice_purple),
                context.getString(R.string.log_in),
                5,
                context.getString(R.string.log_into_complice));
    }

    @Override
    public boolean startAction(Context context) {
        Complice.get().launchLogin(context);
        return true;
    }
}
