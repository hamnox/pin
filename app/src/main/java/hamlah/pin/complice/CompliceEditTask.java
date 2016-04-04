package hamlah.pin.complice;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.bluelinelabs.logansquare.annotation.JsonObject;

import hamlah.pin.R;

@JsonObject
public class CompliceEditTask extends CompliceTask {

    CompliceEditTask() {

    }

    public CompliceEditTask(Context context, boolean isEmpty, boolean isMorning) {
        super(ContextCompat.getColor(context, R.color.complice_purple),
                context.getString(R.string.edit_simple),
                null,
                context.getString(isEmpty ? (isMorning ? R.string.enter_day_intentions_complice
                                                        : R.string.enter_day_outcomes_complice)
                                          : R.string.edit_complice));
    }

    @Override
    public boolean startAction(Context context) {
        Complice.get().launchEdit(context);
        return true;
    }
}
