package edu.neu.android.wocketslib.emasurvey.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.SortedSet;

import edu.neu.android.wocketslib.emasurvey.model.PromptRecorder.SURVEY_LOG_TYPE;
import edu.neu.android.wocketslib.emasurvey.rule.QuesAsGroup;
import edu.neu.android.wocketslib.emasurvey.rule.QuesAsSequence;
import edu.neu.android.wocketslib.emasurvey.rule.QuesFromAns;
import edu.neu.android.wocketslib.emasurvey.rule.Rule;
import edu.neu.android.wocketslib.emasurvey.rule.Rule.RULE_TYPE;

public class QuestionQueueHandler extends PriorityQueue<SurveyQuestion> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long startTime;

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public QuestionQueueHandler() {
		super();
	}

	public QuestionQueueHandler(Collection<? extends SurveyQuestion> c) {
		super(c);
	}

	public QuestionQueueHandler(int initialCapacity, Comparator<? super SurveyQuestion> comparator) {
		super(initialCapacity, comparator);
	}

	public QuestionQueueHandler(int initialCapacity) {
		super(initialCapacity);
	}

	public QuestionQueueHandler(PriorityQueue<? extends SurveyQuestion> c) {
		super(c);
	}

	public QuestionQueueHandler(SortedSet<? extends SurveyQuestion> c) {
		super(c);
	}

	public void quesEnQueue(SurveyQuestion question) {

		boolean isQuestionPresent = false;

		for(SurveyQuestion existingQuestion : this) {
			if(existingQuestion.getAliasID().equals(question.getAliasID())) {
				isQuestionPresent = true;
			}
		}
		if (!isQuestionPresent)
			super.add(question);
	}

	public void quesGroupEnQueue(ArrayList<SurveyQuestion> questionGroup) {
		for (SurveyQuestion question : questionGroup) {
			quesEnQueue(question);
		}
	}

	public void quesDeQueue(SurveyQuestion question) {
		super.remove(question);
	}

	public void quesGroupDeQueue(ArrayList<SurveyQuestion> questionGroup) {
		for (SurveyQuestion question : questionGroup) {
			quesDeQueue(question);
		}
	}

	public void addQuesInGroup(SurveyQuestion question) {
		ArrayList<Rule> nextQues = question.getRuleByType(RULE_TYPE.QUES_AS_GROUP);
		for (Iterator<Rule> iterator = nextQues.iterator(); iterator.hasNext();) {
			QuesAsGroup rule = (QuesAsGroup) iterator.next();
			ArrayList<SurveyQuestion> nextQus = rule.getIngroupQuestions();
			quesGroupEnQueue(nextQus);
		}
	}

	public void addFollowQuesAsSeq(SurveyQuestion question) {
		ArrayList<Rule> nextQues = question.getRuleByType(RULE_TYPE.QUES_AS_SEQUENTIAL);
		for (Iterator<Rule> iterator = nextQues.iterator(); iterator.hasNext();) {
			QuesAsSequence rule = (QuesAsSequence) iterator.next();
			ArrayList<SurveyQuestion> nextQus = rule.getPossibleNextQuestions();
			quesGroupEnQueue(nextQus);
		}
	}

	public void addFollowQuesFromAns(Context mContext, ArrayList<SurveyQuestion> originalQuestions, SurveyQuestion mainActivity, SurveyQuestion question, int ansID) {
		if (question.getAnswers()[ansID].isSelected()) {
			String activityName = getActivityName(mainActivity, question, ansID);
			generateBranchQuestionFromAns(mContext, originalQuestions, question, ansID, activityName);
			quesGroupEnQueue(question.getAnswers()[ansID].getNextQues());
		}
	}

	public void clearFollowQuesAsSeq(SurveyQuestion question) {
		ArrayList<Rule> nextQues = question.getRuleByType(RULE_TYPE.QUES_AS_SEQUENTIAL);
		if (!nextQues.isEmpty())
			quesGroupDeQueue(((QuesAsSequence) nextQues.get(0)).getPossibleNextQuestions());
	}

	public void clearFollowQuesFromAns(SurveyQuestion question) {
        if (question.getQuestionTYPE() != SurveyQuestion.TYPE.MESSAGE) {
            for (int i = 0; i < question.getAnswers().length; i++) {
                if (question.getAnswers()[i] != null && question.getAnswers()[i].isSelected()) {
                    quesGroupDeQueue(question.getAnswers()[i].getNextQues());
                }
            }
        }
    }

	public void clearFollowQuesFromAns(SurveyQuestion question, int ansID) {
		quesGroupDeQueue(question.getAnswers()[ansID].getNextQues());
	}

	public void clearFollowQuesAsGroup(SurveyQuestion question) {
		ArrayList<Rule> nextQues = question.getRuleByType(RULE_TYPE.QUES_AS_GROUP);
		if (!nextQues.isEmpty())
			quesGroupDeQueue(((QuesAsGroup) nextQues.get(0)).getIngroupQuestions());
	}

	private void generateBranchQuestionAsGroup(ArrayList<SurveyQuestion> originalQuestions, SurveyQuestion question, String suffix, String activityName) {
		ArrayList<Rule> quesAsGroup = question.getRuleByType(RULE_TYPE.QUES_AS_GROUP);
		int[] questionIDs = QuestionComparator.parseID(question.getQuestionId());
		if (questionIDs[questionIDs.length - 1] != 0)
			suffix = "_" + questionIDs[questionIDs.length - 1];
		if (quesAsGroup.isEmpty())
			return;
		ArrayList<SurveyQuestion> nextQuestions = ((QuesAsGroup) quesAsGroup.get(0)).getIngroupQuestions();
		if (nextQuestions == null)
			return;
		ArrayList<SurveyQuestion> clonedQuestions = new ArrayList<SurveyQuestion>();
		SurveyQuestion origQues = null;
		for (int i = 0; i < nextQuestions.size(); i++) {
			origQues = nextQuestions.get(i);
			String branchID = origQues.getQuestionId() + "_" + suffix;
			SurveyQuestion branchQus = QuestionSet.getQuestionByID(originalQuestions, branchID);
			if (branchQus == null) {
				branchQus = new SurveyQuestion(origQues, branchID);
				if (branchQus.getQuestionText().contains(QuestionSet.mainActivity))
					branchQus.setQuestionText(branchQus.getQuestionText().replace(QuestionSet.mainActivity, activityName));
				for (Rule rule : branchQus.getRules()) {
					if (rule.getType() == RULE_TYPE.QUES_AS_SEQUENTIAL) {
						generateBranchQuestionAsGroup(originalQuestions, branchQus, suffix, activityName);
					}
				}
				clonedQuestions.add(branchQus);
				originalQuestions.add(branchQus);
			}
		}
		((QuesAsGroup) quesAsGroup.get(0)).setIngroupQuestions(clonedQuestions);
	}

	private void generateBranchQuestionAsSeq(ArrayList<SurveyQuestion> originalQuestions, SurveyQuestion question, String suffix, String activityName) {
		ArrayList<Rule> nextQuesFromAns = question.getRuleByType(RULE_TYPE.QUES_AS_SEQUENTIAL);
		int[] questionIDs = QuestionComparator.parseID(question.getQuestionId());
		if (questionIDs[questionIDs.length - 1] != 0)
			suffix = "_" + questionIDs[questionIDs.length - 1];
		if (nextQuesFromAns.isEmpty())
			return;
		ArrayList<SurveyQuestion> nextQuestions = ((QuesAsSequence) nextQuesFromAns.get(0)).getPossibleNextQuestions();
		if (nextQuestions == null)
			return;
		ArrayList<SurveyQuestion> clonedQuestions = new ArrayList<SurveyQuestion>();
		SurveyQuestion origQues = null;
		for (int i = 0; i < nextQuestions.size(); i++) {
			origQues = nextQuestions.get(i);
			String branchID = origQues.getQuestionId() + "_" + suffix;
			SurveyQuestion branchQus = QuestionSet.getQuestionByID(originalQuestions, branchID);
			if (branchQus == null) {
				branchQus = new SurveyQuestion(origQues, branchID);
				if (branchQus.getQuestionText().contains(QuestionSet.mainActivity))
					branchQus.setQuestionText(branchQus.getQuestionText().replace(QuestionSet.mainActivity, activityName));
				for (Rule rule : branchQus.getRules()) {
					if (rule.getType() == RULE_TYPE.QUES_AS_SEQUENTIAL) {
						generateBranchQuestionAsSeq(originalQuestions, branchQus, suffix, activityName);
					}
				}
				clonedQuestions.add(branchQus);
				originalQuestions.add(branchQus);
			}
		}
		((QuesAsSequence) nextQuesFromAns.get(0)).setPossibleNextQuestions(clonedQuestions);
	}

	private void generateBranchQuestionFromAns(Context mContext, ArrayList<SurveyQuestion> originalQuestions, SurveyQuestion question, int ansID, String activityName) {
		ArrayList<Rule> nextQuesFromAns = question.getRuleByType(RULE_TYPE.QUES_FROM_ANS);
		double randomProb = 1;
		if (nextQuesFromAns.isEmpty())
			return;
		ArrayList<SurveyQuestion> nextQuestions = new ArrayList<SurveyQuestion>();
		for (int i = 0; i < nextQuesFromAns.size(); i++) {
			QuesFromAns rule = (QuesFromAns) nextQuesFromAns.get(i);
			randomProb = question.getAnswers()[ansID].getRandomProb(i);
			int[] answerID = rule.getBranchingAns();
			for (int j = 0; j < answerID.length; j++) {
				// SKIP or USE, Index, Question or SetOfQuestions,
				// RandomSkipProb, ThresholdProbability
				if (answerID[j] == ansID) {
					if (rule.isChosen(randomProb)) {
						nextQuestions.addAll(rule.getPossibleNextQuestions());
						PromptRecorder.writeDetailedLog(mContext, SURVEY_LOG_TYPE.DELIBERATION, startTime, System.currentTimeMillis(), "USE", question.getQuestionId(),
								"QuestionSet", randomProb, rule.getProbability(), rule.getPossibleNextQuestions().toString());
					} else {
						PromptRecorder.writeDetailedLog(mContext, SURVEY_LOG_TYPE.DELIBERATION, startTime, System.currentTimeMillis(), "SKIP", question.getQuestionId(),
								"QuestionSet", randomProb, rule.getProbability(), rule.getPossibleNextQuestions().toString());
					}
					break;
				}
			}
		}
		if (nextQuestions.isEmpty())
			return;
		SurveyQuestion origQues = null;
		for (int i = 0; i < nextQuestions.size(); i++) {
			origQues = nextQuestions.get(i);
			if (!origQues.isMainActivity()) {
				String suffix;
				int[] questionIDs = QuestionComparator.parseID(question.getQuestionId());
				if (questionIDs[questionIDs.length - 1] != 0)
					suffix = "_" + questionIDs[questionIDs.length - 1];
				else
					suffix = "_" + ansID;
				String branchID = origQues.getQuestionId() + suffix;
				SurveyQuestion branchQus = QuestionSet.getQuestionByID(originalQuestions, branchID);
				if (branchQus == null) {
					branchQus = new SurveyQuestion(origQues, branchID);
					if (branchQus.getQuestionText().contains(QuestionSet.mainActivity))
						branchQus.setQuestionText(branchQus.getQuestionText().replace(QuestionSet.mainActivity, activityName));
					for (Rule seqRule : branchQus.getRules()) {
						if (seqRule.getType() == RULE_TYPE.QUES_AS_SEQUENTIAL) {
							generateBranchQuestionAsSeq(originalQuestions, branchQus, Integer.toString(ansID), activityName);
						} else if (seqRule.getType() == RULE_TYPE.QUES_AS_GROUP) {
							generateBranchQuestionAsGroup(originalQuestions, branchQus, Integer.toString(ansID), activityName);
						}
					}
					question.getAnswers()[ansID].getNextQues().add(branchQus);
					originalQuestions.add(branchQus);
				}
			} else {
				boolean isExist = false;
				//TODO Yifei fix warning 
				for (Iterator iterator = question.getAnswers()[ansID].getNextQues().iterator(); iterator.hasNext();) {
					SurveyQuestion subQuestion = (SurveyQuestion) iterator.next();
					if (subQuestion.equals(origQues))
						isExist = true;
				}
				if (!isExist)
					question.getAnswers()[ansID].getNextQues().add(origQues);
			}
		}
	}

	private String getActivityName(SurveyQuestion mainActivity, SurveyQuestion currentQuestion, int ansID) {
		if (mainActivity == null)
			return null;
		/*if (mainActivity.equals(currentQuestion))
			return currentQuestion.getAnswers()[ansID].getAnswerText();
		else {
			int i = currentQuestion.getQuestionId().lastIndexOf('_');			
			int id = Integer.parseInt(currentQuestion.getQuestionId().substring(i + 1));
			return mainActivity.getAnswers()[id].getAnswerText();
		}*/
		return currentQuestion.getAnswers()[ansID].getAnswerText();
	}
}
