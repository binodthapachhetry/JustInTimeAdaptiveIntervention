package edu.neu.android.wocketslib.activities.datasummaryviewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.view.View;

public class DataSummaryViewer extends View {
//	private final static String TAG = "DataSummary";
	private int xOffsets;
	private int[] yOffsets = {66,98,140,172,214,246,288,320,362,394,436,468,506,534};
	private int[][] dateOffset = {{10,8},{(60*25)*GetDataSummaryActivity.pointPxs-40,8}};
	private String[] dateLabel = {"Yesterday", "Today"};
	private Paint paint;
	private String[] timeLabel;
	private int[] data;
	private float[] points;
	private Typeface externalFont;
	private GetDataSummaryActivity activity;
	private WocketsDataSPHelper dataHelper;
	public DataSummaryViewer(GetDataSummaryActivity activity, int xOffset) {
		super(activity);
		this.externalFont = Typeface.createFromAsset(activity.getAssets(), "fonts/verdana.ttf");
		this.xOffsets = xOffset;
		this.activity = activity;
		this.paint = new Paint();
		this.paint.setAntiAlias(true);
		dataHelper = new WocketsDataSPHelper(activity);
		initDataPoints();
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		invalidate();
		// Date
		this.paint.setColor(Color.BLACK);
		this.paint.setTextSize(10);
		this.paint.setTextAlign(Align.LEFT);
		for (int i = 0; i < dateLabel.length; i++) {
			canvas.drawText(dateLabel[i], dateOffset[i][0], dateOffset[i][1], this.paint);
		}
		//Time
		this.paint.setTypeface(this.externalFont);
		this.paint.setTextSize(18);
		this.paint.setTextAlign(Align.LEFT);
		this.paint.setColor(Color.DKGRAY);
		paint.setStrokeWidth(2);
		for (int i = 0; i < timeLabel.length; i++) {
			float x = 60*4*i + xOffsets;
			canvas.drawText(timeLabel[i], x, 30, paint);
			canvas.drawLine(x, 30, x, 36, paint);
		}
		
		/**
		 * draw data
		 */
		paint.setColor(Color.RED);
		paint.setStrokeWidth(4);
		canvas.drawPoints(points, paint);		
		
		Intent intent = new Intent(GetDataSummaryActivity.INTENT_ACTION_EXIT);
		activity.sendBroadcast(intent);
	}

	private void initDataPoints(){
		this.timeLabel = getHorizontalLabels();
		ArrayList<Float> pointList = new ArrayList<Float>();
		float[] pointArray;
		for (int i = 0; i < WocketsDataSPHelper.TAGS.length; i++){ 
			data = dataHelper.getData(WocketsDataSPHelper.TAGS[i]);
			pointArray = getPointsFromData(data, yOffsets[i]);
			if(pointArray != null)
				for (float f : pointArray) {
					pointList.add(f);
				}
		}
		points = new float[pointList.size()];
		for (int i = 0; i < points.length; i++) {
			points[i] = pointList.get(i);
		}
//		android.os.Debug.waitForDebugger();
		return;
	}
	private String[] getHorizontalLabels(){
		Calendar now = Calendar.getInstance();
		boolean isRunningmorethanADay = true;// TODO give by real data
		
		if(isRunningmorethanADay){
			String[] aWholeDay = {"1AM","2AM","3AM","4AM","5AM","6AM","7AM","8AM","9AM","10AM","11AM","12PM",
					"1PM","2PM","3PM","4PM","5PM","6PM","7PM","8PM","9PM","10PM","11PM","12AM"};
			int hr = now.get(Calendar.HOUR_OF_DAY);
			String[] labels = new String[25];
			for (int i = 0; i < 24; i++){
				labels[i+1] = aWholeDay[(hr+i)>=24?(hr+i-24):(hr+i)];
			}
			labels[0] = labels[24];
			return labels;
		}
		return null;	
	}
	private float[] getPointsFromData(int[] data, int yOffset){
		List<Float> coordinate = new ArrayList<Float>();
		Calendar now = Calendar.getInstance();
		int hr = now.get(Calendar.HOUR_OF_DAY);
		int min = now.get(Calendar.MINUTE);
		
		for (int i = 0; i < 1440 ; i++) {
			float relativeX;
			if(i <= hr*60+min)
				relativeX = (float)(xOffsets+ (i+(24-hr)*60)*GetDataSummaryActivity.pointPxs);
			else 
				relativeX = (float)(xOffsets+ (i-hr*60)*GetDataSummaryActivity.pointPxs);
			if(data[i] > 0){
				coordinate.add(relativeX);
				coordinate.add((float)yOffset);
			}
		}

		float[] points = new float[coordinate.size()];
		for (int i = 0; i < points.length; i++) {
			points[i] = coordinate.get(i);
		}
		return points;	
	}
	public void update(){
		initDataPoints();
		postInvalidate();
	}
}