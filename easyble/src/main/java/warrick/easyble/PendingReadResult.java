package warrick.easyble;

import android.bluetooth.BluetoothGattCharacteristic;

import java.lang.reflect.Constructor;

/**
 * Class defining a pending read result
 */
public class PendingReadResult {
    public PendingReadResult(BluetoothGattCharacteristic characteristic, ReadResult callback, Class callbackClass) {
        this.characteristic = characteristic;
        this.callback = callback;
        this.callbackClass = callbackClass;
    }
    BluetoothGattCharacteristic characteristic;
    ReadResult callback;
    Class callbackClass;

    /**
     * Fire the callback as a success
     * @param data The data to provide to the read result
     */
    public void success(byte[] data) {
        try {
            Constructor constructor = callbackClass.getConstructor();
            GattValue response = (GattValue)constructor.newInstance();
            response.deserialize(data);

            callback.success(response);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fire the callback as a failure
     */
    public void failure() {
        callback.failure();
    }
}