package lt.joku.runnandrest.utils;

import co.lujun.lmbluetoothsdk.base.State;

/**
 * @author Jonas Kundra
 * @since 0.8
 */

public class BluetoothUtils {

    public static String transConnStateAsString(int state) {
        String result;
        if (state == State.STATE_NONE) {
            result = "NONE";
        } else if (state == State.STATE_LISTEN) {
            result = "LISTEN";
        } else if (state == State.STATE_CONNECTING) {
            result = "CONNECTING";
        } else if (state == State.STATE_CONNECTED) {
            result = "CONNECTED";
        } else if (state == State.STATE_DISCONNECTED) {
            result = "DISCONNECTED";
        } else if (state == State.STATE_GOT_CHARACTERISTICS) {
            result = "CONNECTED, GOT ALL CHARACTERISTICS";
        } else {
            result = "UNKNOWN";
        }
        return result;
    }
}
