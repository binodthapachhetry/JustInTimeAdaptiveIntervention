package mhealth.neu.edu.phire.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.neu.mhealth.android.wockets.library.data.DataManager;
import edu.neu.mhealth.android.wockets.library.database.entities.study.Study;
import edu.neu.mhealth.android.wockets.library.support.Log;
import mhealth.neu.edu.phire.R;


public class FeedbackChoices extends AppCompatActivity {

    private static final String TAG = "FeedbackChoices";
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_choices);
        ButterKnife.bind(this);
        mContext = getApplicationContext();
    }

    @OnClick(R.id.activity_current_info)
    public void onCurrentInfoSelected(){
        Study study = DataManager.getStudy(mContext);
        if (study == null) {
            Log.e(TAG, "onCurrentInfoSelected - No study found", mContext);
            return;
        }
        Intent intent = new Intent(this, CurrentEEdistance.class);
        startActivity(intent);


    }

    @OnClick(R.id.activity_week_info_ee)
    public void onWeekInfoEEselected(){
        Study study = DataManager.getStudy(mContext);
        if (study == null) {
            Log.e(TAG, "onWeekInfoEEselected - No study found", mContext);
            return;
        }
        Intent intent = new Intent(this, EnergyPlot.class);
        startActivity(intent);

    }

    @OnClick(R.id.activity_week_info_distance)
    public void onWeekInfoDistanceSelected(){
        Study study = DataManager.getStudy(mContext);
        if (study == null) {
            Log.e(TAG, "onWeekInfoDistanceSelected - No study found", mContext);
            return;
        }
        Intent intent = new Intent(this, DistancePlot.class);
        startActivity(intent);

    }

    @OnClick(R.id.feedback_choice_done)
    public void onFeedbackChoiceDoneSelected(){
        Study study = DataManager.getStudy(mContext);
        if (study == null) {
            Log.e(TAG, "onFeedbackChoiceDoneSelected - No study found", mContext);
            return;
        }
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);

        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
//        this.finish();

    }

}


