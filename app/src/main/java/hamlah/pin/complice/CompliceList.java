package hamlah.pin.complice;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.ArrayList;

@JsonObject
public class CompliceList {

    @JsonField
    ArrayList<CompliceTodoItem> todolist = new ArrayList<>();

    @JsonObject
    public static class CompliceTodoItem {
        @JsonField
        String text;

        @JsonField
        String code;

        @JsonField
        boolean done;

        @JsonField
        boolean nevermind = false;

        @JsonField(name="_id")
        String id;
    }




    /*
    example:

    {
        "dayofweek": "Friday",
        "todate": "2016-04-15",
        "updated": 1460775554116,
        "drafts": false,
        "todolist": [
            {
                "text": "&) task",
                "code": "x", (because "&")
                "done": true,
                "uid": same as _id,
                "_id": "1234567eadc"
            },
            {
                "text": "1) another task (4 minutes)",
                "code": "1", (because 1)
                "done": false,
                "nevermind": true,
                "uid": same as _id,
                "_id": "1234567eadc"
            },
            ...
        ],
        "alerts": {},
        "timer": {
            "state": "inactive",
            "continuous": false,
            "mode": "basic"
        },
        "loggedinusername": "username",
        "ymd": "2016-04-15", (current day we're working on)
        "breakCount": 0,
        "todaySettings": { // don't care about any of this
            "dingVolume": 0.8,
            "dingSoundPath": "/audio/toaster.mp3",
            "boilerplate": {
                "daily": "continuous string of daily actions"
            },
            "tickVolume": 0.01,
            "distractionFreeByDefault": false,
            "breakTime": 5,
            "longBreakInterval": 4,
            "pomoTime": 25,
            "longBreakDuration": 20
        },
        "sparePomos": 0
    }

    */


    /** ######################################################################################### */

    /*
    no intentions yet:
    {
        "dayofweek": "Saturday",
        "todate": "2016-04-16",
        "updated": 1460777785250,
        "drafts": false,
        "todolist": [],
        ... otherwise the same ...
    }

     */

}
