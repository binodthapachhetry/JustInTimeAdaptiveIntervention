package edu.neu.mhealth.android.wockets.library.database.entities.study;

import java.util.List;

/**
 * @author Dharam Maniar
 */
public class Question {

	public String key;

	public String type;

	public Text text;

	public Text firstPromptPrefixText;

	public Text nonFirstPromptPrefixText;

	public List<Answer> answers;

	public double probability;

	public boolean firstSurveyForTheDay;

    public boolean firstAnsweredSurveyForTheDay;

    public boolean onlyWeekdays;

	public boolean alwaysPrompt;

	public List<String> answerDependency;

	public List<String> specificPrompts;

	public boolean isBackButtonDisabled;

	public boolean isReplaceNextWithFinish;

	public boolean allowToSkip;

	public boolean isSleepTimeQuestion;

	public boolean isWakeTimeQuestion;

	public List<Integer> range;

	public boolean repeat;

	public int repeatCount = 0;

	public String textInsertToTitle = "";



	public Question() {
	}

	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}

	public void setTextInsertToTitle(String textInsertToTitle) {
		this.textInsertToTitle = textInsertToTitle;
	}

	public Question(
			String key,
			String type,
			Text text,
            Text firstPromptPrefixText,
            Text nonFirstPromptPrefixText,
			List<Answer> answers,
			double probability,
	        boolean firstSurveyForTheDay,
            boolean firstAnsweredSurveyForTheDay,
            boolean onlyWeekdays,
	        boolean alwaysPrompt,
	        List<String> answerDependency,
			List<String> specificPrompts,
	        boolean isBackButtonDisabled,
			boolean isReplaceNextWithFinish,
            boolean allowToSkip,
            boolean isSleepTimeQuestion,
            boolean isWakeTimeQuestion,
			List<Integer> range,
			boolean repeat,
			int repeatCount,
			String textInsertToTitle
	) {
		this.key = key;
		this.type = type;
		this.text = text;
        this.firstPromptPrefixText = firstPromptPrefixText;
        this.nonFirstPromptPrefixText = nonFirstPromptPrefixText;
		this.answers = answers;
		this.probability = probability;
		this.firstSurveyForTheDay = firstSurveyForTheDay;
        this.firstAnsweredSurveyForTheDay = firstAnsweredSurveyForTheDay;
        this.onlyWeekdays = onlyWeekdays;
		this.alwaysPrompt = alwaysPrompt;
		this.answerDependency = answerDependency;
		this.specificPrompts = specificPrompts;
		this.isBackButtonDisabled = isBackButtonDisabled;
		this.isReplaceNextWithFinish = isReplaceNextWithFinish;
        this.allowToSkip = allowToSkip;
        this.isSleepTimeQuestion = isSleepTimeQuestion;
        this.isWakeTimeQuestion = isWakeTimeQuestion;
		this.range = range;
		this.repeat = repeat;
		this.repeatCount = repeatCount;
		this.textInsertToTitle = textInsertToTitle;


	}

	public Question setKey(String key) {
		this.key = key;
		return this;
	}

	public Question setType(String type) {
		this.type = type;
		return this;
	}

	public Question setText(Text text) {
		this.text = text;
		return this;
	}

	public Question setFirstPromptPrefixText(Text firstPromptPrefixText) {
		this.firstPromptPrefixText = firstPromptPrefixText;
		return this;
	}

	public Question setNonFirstPromptPrefixText(Text nonFirstPromptPrefixText) {
		this.nonFirstPromptPrefixText = nonFirstPromptPrefixText;
		return this;
	}

	public Question setAnswers(List<Answer> answers) {
		this.answers = answers;
		return this;
	}

	public Question setProbability(double probability) {
		this.probability = probability;
		return this;
	}

	public Question setFirstSurveyForTheDay(boolean firstSurveyForTheDay) {
		this.firstSurveyForTheDay = firstSurveyForTheDay;
		return this;
	}

	public Question setFirstAnsweredSurveyForTheDay(boolean firstAnsweredSurveyForTheDay) {
		this.firstAnsweredSurveyForTheDay = firstAnsweredSurveyForTheDay;
		return this;
	}

	public Question setOnlyWeekdays(boolean onlyWeekdays) {
		this.onlyWeekdays = onlyWeekdays;
		return this;
	}

	public Question setAlwaysPrompt(boolean alwaysPrompt) {
		this.alwaysPrompt = alwaysPrompt;
		return this;
	}

	public Question setAnswerDependency(List<String> answerDependency) {
		this.answerDependency = answerDependency;
		return this;
	}

	public Question setSpecificPrompts(List<String> specificPrompts) {
		this.specificPrompts = specificPrompts;
		return this;
	}

	public Question setIsBackButtonDisabled(boolean isBackButtonDisabled) {
		this.isBackButtonDisabled = isBackButtonDisabled;
		return this;
	}

	public Question setIsReplaceNextWithFinish(boolean isReplaceNextWithFinish) {
		this.isReplaceNextWithFinish = isReplaceNextWithFinish;
		return this;
	}

	public Question setAllowToSkip(boolean allowToSkip) {
		this.allowToSkip = allowToSkip;
		return this;
	}

	public Question setIsSleepTimeQuestion(boolean isSleepTimeQuestion) {
		this.isSleepTimeQuestion = isSleepTimeQuestion;
		return this;
	}

	public Question setIsWakeTimeQuestion(boolean isWakeTimeQuestion) {
		this.isWakeTimeQuestion = isWakeTimeQuestion;
		return this;
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

	public void setRange(List<Integer> range) {
		this.range = range;
	}

	public Question createQuestion() {
		return this;
	}
}
