package edu.neu.android.wocketslib.emasurvey.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.neu.android.wocketslib.R;

public class NumberPickerSpinnerAdapter extends ArrayAdapter<String> {
	private Context cxt;
	private String[] items;

	public NumberPickerSpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
		super(context, textViewResourceId, objects);
		this.cxt = context;
		this.items = objects;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	public View getCustomView(int position, View convertView, ViewGroup parent) {
		LayoutInflater layoutInflater = (LayoutInflater) cxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = layoutInflater.inflate(R.layout.numberpickerspinneritem, parent, false);
		TextView label = (TextView) row.findViewById(R.id.numberpickeritem);
		label.setText(items[position]);
		return row;
	}
}
