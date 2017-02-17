package edu.neu.android.wocketslib.emasurvey.model;

import android.text.InputFilter;
import android.text.Spanned;

public class EditTextNumInputFilter implements InputFilter {

	private int min, max;

	public EditTextNumInputFilter(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public EditTextNumInputFilter(String min, String max) {
		this.min = Integer.parseInt(min);
		this.max = Integer.parseInt(max);
	}

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		try {
			int input = Integer.parseInt(dest.toString() + source.toString());
			if (isInRange(min, max, input))
				return null;
		} catch (NumberFormatException nfe) {
		}
		return "";
	}

	private boolean isInRange(int a, int b, int c) {
		return b > a ? c >= a && c <= b : c >= b && c <= a;
	}
}
