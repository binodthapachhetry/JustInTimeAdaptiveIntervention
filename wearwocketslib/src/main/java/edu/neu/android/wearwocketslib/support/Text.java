package edu.neu.android.wearwocketslib.support;

/**
 * Created by jarvis on 4/6/17.
 */

public class Text {

    public String english;

    public String spanish;

    public Text() {
    }

    public Text(String english, String spanish) {
        this.english = english;
        this.spanish = spanish;
    }

    public Text setEnglish(String english) {
        this.english = english;
        return this;
    }

    public Text setSpanish(String spanish) {
        this.spanish = spanish;
        return this;
    }

    public Text createText() {
        return new Text(english, spanish);
    }
}