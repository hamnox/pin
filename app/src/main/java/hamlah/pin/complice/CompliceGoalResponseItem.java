package hamlah.pin.complice;

import android.graphics.Color;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class CompliceGoalResponseItem {
    @JsonField
    String code;

    @JsonField
    String color;

    public Integer getIntColor() {
        return Color.parseColor(color);
    }


/*[
    {
        "code": "1",
        "stats": {
            "totalOutcomes": 41,
            "currentStreak": 7,
            "maxStreak": 7
        },
        "name": "name",
        "privacy": 30,
        "color": "#ff00ff",
        "milestone": {
            "type": "checkpoint",
            "startStamp": 1460229918401,
            "ymd": "2016-04-23",
            "spec": "",
            "name": "Replace this goal"
        }
    },
    ...
]
*/
}
