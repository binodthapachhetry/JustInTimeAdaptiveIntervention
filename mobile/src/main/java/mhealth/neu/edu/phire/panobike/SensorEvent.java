package mhealth.neu.edu.phire.panobike;

/**
 * Created by jarvis on 2/23/17.
 */

public class SensorEvent {
    public String value;
    public String field;

    public SensorEvent(String st, String val){
        this.field = st;
        this.value = val;
    }
}

