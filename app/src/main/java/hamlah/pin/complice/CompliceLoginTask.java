package hamlah.pin.complice;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.login_process)
                .setCancelable(false)
                .setPositiveButton(R.string.open_complice, (dialog, id) -> {
                    Complice.get().launchLogin(context);
                });
        AlertDialog alert = builder.create();
        alert.show();
        return true;
    }
}
