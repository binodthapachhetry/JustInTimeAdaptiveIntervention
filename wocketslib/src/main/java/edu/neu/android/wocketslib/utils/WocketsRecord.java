package edu.neu.android.wocketslib.utils;


public class WocketsRecord {
//	private Context cxt;
	private final String[] position = { "Right Wrist", "Right Ankle", "Left Wrist", "Left Ankle", "Right Pocket", "Left Pocket" };
	private String[] item;

	public String[] getItem() {
		return item;
	}

	public WocketsRecord() {
		super();
//		this.cxt = cxt;
		item = new String[position.length];
		for (int i = 0; i < position.length; i++) {
			item[i] = null;
		}
	}

	public void setLabel(int location, String label) {
		item[location] = label;
	}

	public String wocketsRecord() {
		String record = "";
		int counter = 0;
		for (int i = 0; i < position.length; i++) {
			if (item[i] != null) {
				record += " the " + item[i] + " on your " + position[i] + " and";
				counter++;
			}
		}
		if (counter != 0)
			record = record.substring(0, record.length() - 4) + ".";
		else
			record = " nothing.";
		return record;
	}

}
