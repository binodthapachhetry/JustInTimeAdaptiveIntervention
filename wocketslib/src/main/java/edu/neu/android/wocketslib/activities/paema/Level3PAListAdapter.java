package edu.neu.android.wocketslib.activities.paema;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.activities.paema.model.PhysicalActivity;
import edu.neu.android.wocketslib.utils.Util;

public class Level3PAListAdapter extends BaseAdapter {

	private Context ctx;
	private ArrayList<PhysicalActivity> physicalActivities;
	private int itemCount;
	private int startIndex;
	private int page = 1;
	private int MAX_ITEMS_IN_A_PAGE = 6;

	Level3PAListAdapter(Context ctx,
			ArrayList<PhysicalActivity> physicalActivities) {
		this.ctx = ctx;
		this.physicalActivities = physicalActivities;
		computePageInfo();
	}

	@Override
	public int getCount() {
		return itemCount;
	}

	@Override
	public Object getItem(int relativePosition) {
		return physicalActivities.get(startIndex + relativePosition);
	}

	@Override
	public long getItemId(int position) {
		return startIndex + position;
	}

	private void computePageInfo() {
		startIndex = (page - 1) * MAX_ITEMS_IN_A_PAGE;
		itemCount = Math.min(MAX_ITEMS_IN_A_PAGE, physicalActivities.size()
				- startIndex);
	}

	public void nextPage() {
		page++;
		computePageInfo();
		notifyDataSetChanged();
	}

	public void previousPage() {
		page--;
		computePageInfo();
		notifyDataSetChanged();
	}

	public boolean hasNextPage() {
		page++;
		computePageInfo();
		boolean nextPageFlag = (itemCount > 0) ? true : false;
		page--;
		computePageInfo();
		return nextPageFlag;
	}

	public boolean hasPreviousPage() {
		return (page == 1) ? false : true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = LayoutInflater.from(ctx).inflate(R.layout.tracker_list_item,
				null);
		
		if (Util.isAria(ctx)) {
			//((TextView)v.findViewById(R.id.text)).setTextSize(18.0f);
			CheckBox cb = ((CheckBox)v.findViewById(R.id.physicalActivitySelect));
			android.view.ViewGroup.LayoutParams params = cb.getLayoutParams();
			params.height = 30;
			cb.setLayoutParams(params);
		}

		// When user touches a row in the list then state of checkbox need to be
		// toggled
		v.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CheckBox physicalActivitySelectView = (CheckBox) v
						.findViewById(R.id.physicalActivitySelect);
				PhysicalActivity pa = (PhysicalActivity) physicalActivitySelectView
						.getTag();
				if (physicalActivitySelectView.isChecked()) {
					pa.setSelected(false);
					physicalActivitySelectView.setChecked(false);
				} else {
					pa.setSelected(true);
					physicalActivitySelectView.setChecked(true);
				}
			}
		});

		PhysicalActivity pa = (PhysicalActivity) getItem(position);

		((TextView) v.findViewById(R.id.physicalActivityName)).setText(pa
				.getName());

		CheckBox physicalActivitySelectView = (CheckBox) v
				.findViewById(R.id.physicalActivitySelect);
		physicalActivitySelectView.setTag(pa);
		if (pa.isSelected())
			physicalActivitySelectView.setChecked(true);

		physicalActivitySelectView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CheckBox physicalActivitySelectView = (CheckBox) v;
				PhysicalActivity pa = (PhysicalActivity) physicalActivitySelectView
						.getTag();
				if (physicalActivitySelectView.isChecked())
					pa.setSelected(true);
				else
					pa.setSelected(false);
			}
		});

		return v;
	}

}
