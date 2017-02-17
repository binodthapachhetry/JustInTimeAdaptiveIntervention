package edu.neu.mhealth.android.wockets.library.database.entities.study;

import java.util.List;

/**
 * @author Dharam Maniar
 *
 * This is the root entity on the remote database.
 */
public class Study {

	public List<Survey> surveys;

	public List<String> supportedLanguages;

	public Study() {}

	public Study(
			List<Survey> surveys,
	        List<String> supportedLanguages
	) {
		this.surveys = surveys;
		this.supportedLanguages = supportedLanguages;
	}

	public Study setSurveys(List<Survey> surveys) {
		this.surveys = surveys;
		return this;
	}

	public Study setSupportedLanguages(List<String> supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
		return this;
	}

	public Study createStudy() {
		return new Study(surveys, supportedLanguages);
	}
}
