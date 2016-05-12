package warrick.easyble;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Class defining a pending write result
 */
public class PendingWriteResult {
    public PendingWriteResult(BluetoothGattCharacteristic characteristic, WriteResult callback, byte[] rawWrittenValue) {
        this.characteristic = characteristic;
        this.callback = callback;
        this.rawValue = rawWrittenValue;
    }
    BluetoothGattCharacteristic characteristic;
    WriteResult callback;
    byte[] rawValue;


    /**
     * Fire the callback as a success
     */
    public void success() {
        callback.success();
    }

    /**
     * Fire the callback as a failure
     */
    public void failure() {
        callback.failure();
    }
}