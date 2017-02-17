package mhealth.neu.edu.phire.fragments;

import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import mhealth.neu.edu.phire.R;

/**
 * Created by qutang on 7/10/15.
 */
public class SplashFragment extends Fragment {

    private TextView deviceText;
    private TextView transferStatus;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.splash_fragment, container, false);
        final TextView versionText = (TextView) view.findViewById(R.id.activity_version_text);
        deviceText = (TextView) view.findViewById(R.id.activity_device_text);
        transferStatus = (TextView) view.findViewById(R.id.activity_transfer_text);
        if(getActivity().getIntent().getBooleanExtra("START_TRANSFER", false)){
            setTransferStatus("START_TRANSFER");
        }
        setTransferStatus("IDLE");
        try {
            versionText.setText(getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(),
                            0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return view;
    }

    public void setConnectedDevice(String deviceName){
        deviceText.setText(deviceName);
    }

    public void setTransferStatus(String status){
        if(status.equals("START_TRANSFER")){
            transferStatus.setText("Transferring...");
        }else if(status.equals("FINISH_TRANSFER")){
            transferStatus.setText("Idle");
        }else{
            transferStatus.setText("Idle");
        }
    }
}
