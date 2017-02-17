package edu.neu.mhealth.android.wockets.library.database.entities.study;

/**
 * @author Dharam Maniar
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
