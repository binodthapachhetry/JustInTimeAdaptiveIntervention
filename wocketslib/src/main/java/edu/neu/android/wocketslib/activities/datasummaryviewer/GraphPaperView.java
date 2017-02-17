package edu.neu.android.wocketslib.activities.datasummaryviewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.View;

public class GraphPaperView extends View {

	private Paint paint;
	private String[] verticalPositions;

	public GraphPaperView(Context context, int verticalPositions) {
		super(context);
		this.verticalPositions = new String[verticalPositions + 1];
		this.paint = new Paint();
		this.paint.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float topBorder = 52;
		float border = 0;
		float horizontalStart = border * 2;
		float height = getHeight();
		float width = getWidth() - 1;
		float graphHeight = height - topBorder;

		this.paint.setTextAlign(Align.LEFT);
		int verticalPosition = this.verticalPositions.length - 1;
		for (int i = 0; i < this.verticalPositions.length; i++) {
			this.paint.setColor(Color.LTGRAY);
			float y = ((graphHeight / verticalPosition) * i) + topBorder;
			canvas.drawLine(horizontalStart, y, width, y, this.paint);
		}
	}

}
