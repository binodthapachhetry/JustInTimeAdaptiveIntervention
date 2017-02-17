package edu.neu.android.wocketslib.emasurvey.model;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.emasurvey.SurveyActivity;

public class ChoiceAdapter extends BaseAdapter {
	public final static int UPDATE_ANSWER_SELECTED = 0;
	public final static int UPDATE_ANSWER_CANCELLED = 1;
	private Context mContext;
	private SurveyAnswer[] mAnswers;
	private boolean mIsSingleChoice;

	@Override
	public int getCount() {
		return mAnswers.length;
	}

	@Override
	public Object getItem(int relativePosition) {
		return mAnswers[relativePosition];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public ChoiceAdapter(Context context, SurveyAnswer[] answers, boolean isSingleChoice) {
		mContext = context;
		mAnswers = answers;
		mIsSingleChoice = isSingleChoice;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		View v = LayoutInflater.from(mContext).inflate(R.layout.tracker_list_item, null);
		
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		int minHeight = Math.round((dm.densityDpi  / 160f) * Globals.MIN_HEIGHT_SURVEY_ITEM);
		v.setMinimumHeight(minHeight);
		
		SurveyAnswer choice = mAnswers[position];
		TextView tv = (TextView) v.findViewById(R.id.physicalActivityName);
		tv.setText(choice.getAnswerText());
		
		CheckBox choiceSelectView = (CheckBox) v.findViewById(R.id.physicalActivitySelect);			
		choiceSelectView.setTag(choice);
		choiceSelectView.setChecked(choice.isSelected());
		choiceSelectView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CheckBox choiceSelectView = (CheckBox) v;
				if (choiceSelectView.isChecked()) {
					updateAnsSelected(position);
					if (mIsSingleChoice) {
						uncheckOtherItems(parent, position);
					}
				} else {
					updateAnsCanceled(position);
				}
			}
		});

		v.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CheckBox choiceSelectView = (CheckBox) v.findViewById(R.id.physicalActivitySelect);
				if (choiceSelectView.isChecked()) {
					choiceSelectView.setChecked(false);
					updateAnsCanceled(position);
				} else {
					choiceSelectView.setChecked(true);
					updateAnsSelected(position);
					if (mIsSingleChoice) {
						uncheckOtherItems(parent, position);
					}
				}
			}
		});
		return v;
	}

	private void uncheckOtherItems(ViewGroup parent, int position) {
		position -= ((ListView) parent).getFirstVisiblePosition();
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (i != position) {
				View otherView = parent.getChildAt(i);
				CheckBox otherChoice = (CheckBox) otherView.findViewById(R.id.physicalActivitySelect);
				if (otherChoice.isChecked()) {
					otherChoice.setChecked(false);
				}
			}
		}
	}

	private void updateAnsSelected(int index) {
		Intent intent = new Intent(SurveyActivity.TAG + UPDATE_ANSWER_SELECTED);
		intent.putExtra("index", index);
		mContext.sendBroadcast(intent);
	}

	private void updateAnsCanceled(int index) {
		Intent intent = new Intent(SurveyActivity.TAG + UPDATE_ANSWER_CANCELLED);
		intent.putExtra("index", index);
		mContext.sendBroadcast(intent);
	}
}
