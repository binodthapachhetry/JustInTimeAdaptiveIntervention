package edu.neu.android.wearwocketslib.tasks;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.neu.android.wearwocketslib.utils.log.Log;


/**
 * Created by qutang on 9/16/15.
 *
 */
public class CheckPhoneConnectionTask {

    public interface OnPhoneConnectionCallBack{
        void onPhoneInConnection(Set<Node> nodes);

        void onPhoneNotInConnection();
    }

    private static final String TAG = "CheckPhoneConnectionTask";

    private GoogleApiClient mGoogleApiClient;

    public CheckPhoneConnectionTask(GoogleApiClient googleApiClient){
        mGoogleApiClient = googleApiClient;
    }

    public void check(final String capabilityName, final OnPhoneConnectionCallBack callBack) {
        if(!mGoogleApiClient.isConnected()){
            Log.d(TAG,"Google API client not connected!!");
            return;
        }
        Wearable.CapabilityApi.getAllCapabilities(mGoogleApiClient,
                CapabilityApi.FILTER_ALL).setResultCallback(

                new ResultCallback<CapabilityApi.GetAllCapabilitiesResult>() {
                    @Override
                    public void onResult(CapabilityApi.GetAllCapabilitiesResult getAllCapabilitiesResult) {
                        if (!getAllCapabilitiesResult.getStatus().isSuccess()) {
                            Log.d(TAG,"FROM HERE!");
                            callBack.onPhoneNotInConnection();
                            return;
                        }
                        Map<String, CapabilityInfo>
                                capabilitiesMap = getAllCapabilitiesResult.getAllCapabilities();

                        Set<Node> nodes = new HashSet<>();

                        CapabilityInfo capabilityInfo = capabilitiesMap.get(capabilityName);
                        if (capabilityInfo != null) {
                            nodes.addAll(capabilityInfo.getNodes());
                        }
                        if (nodes.size() > 0) {
                            callBack.onPhoneInConnection(nodes);
                        } else {
                            Log.d(TAG,"ACTUALLY FROM HERE!");
                            callBack.onPhoneNotInConnection();
                        }
                    }
                });
    }
}
