package edu.neu.android.wocketslib.activities.datasummaryviewer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.view.View;

public class DateOverlayView extends View {
	private int xOffset;
	private Paint paint;
	private Typeface externalFont;
	public DateOverlayView(final GetDataSummaryActivity activity, int xOffset) {
		super(activity);
		this.xOffset = xOffset;
		this.paint = new Paint();
		this.paint.setAntiAlias(true);
		this.externalFont = Typeface.createFromAsset(activity.getAssets(),
				"fonts/verdana.ttf");
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		this.paint.setTextAlign(Align.LEFT);
		this.paint.setTypeface(this.externalFont);
		this.paint.setTextSize(14);
		this.paint.setColor(Color.BLACK);
		String[] labels = GetDataSummaryActivity.labels;
		int[][] offsets = GetDataSummaryActivity.offsets;
		for (int i = 0; i < labels.length; i++) {
			canvas.drawText(labels[i], offsets[i][0]+xOffset, offsets[i][1], this.paint);
		}

	}
}

