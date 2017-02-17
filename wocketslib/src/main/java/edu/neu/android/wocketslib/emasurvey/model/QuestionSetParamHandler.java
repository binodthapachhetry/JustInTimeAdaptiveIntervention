package edu.neu.android.wocketslib.emasurvey.model;

import java.io.Serializable;

public class QuestionSetParamHandler implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String   mClassName;
	private Object[] mParams;

	public QuestionSetParamHandler(String className, Object[] params) {
		super();
		this.mClassName = className;
		this.mParams    = params;
	}
	
	public String getQuestionSetClassName() {
		return mClassName;
	}
	
	public void setQuestionSetClassName(String className) {
		mClassName = className;
	}

	public Object[] getParams() {
		return mParams;
	}

	public void setParams(Object[] params) {
		this.mParams = params;
	}

}
