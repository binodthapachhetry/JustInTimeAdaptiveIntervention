package mhealth.neu.edu.phire.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import mhealth.neu.edu.phire.R;
import edu.neu.android.wearwocketslib.utils.io.SharedPrefs;

/**
 * Created by qutang on 7/13/15.
 */
public class KeyInformationFragment extends Fragment {
    private TextClock runningClock;
    private TextView samplingRateText;
    private Timer updateTimer;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.key_info_fragment, container, false);
        samplingRateText = (TextView) view.findViewById(R.id.sampling_rate_text);
        runningClock = (TextClock) view.findViewById(R.id.running_clock);
        runningClock.setFormat12Hour(null);
        runningClock.setFormat24Hour("HH:mm:ss");
        runningClock.setTextColor(Color.BLACK);
        samplingRateText.setTextColor(Color.BLACK);
        return view;
    }

    @Override
    public void onStart() {
        updateTimer = new Timer("SR_TIMER");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                updateSamplingRateDisplay();
            }
        };
        updateTimer.scheduleAtFixedRate(task, 0, 60*1000);
        super.onStart();
    }

    public void updateSamplingRateDisplay(){
        DecimalFormat formatter = new DecimalFormat("0000");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                samplingRateText.setText(0 + "-" + 0 + "-" + 0);
            }
        });
    }

    @Override
    public void onStop() {
        updateTimer.cancel();
        super.onStop();
    }
}
