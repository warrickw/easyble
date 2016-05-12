package warrick.easyble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Provides a simple interface for interacting with a BLE GATT service and the attributes within a service.
 */
public class EasyGattService implements EasyBleDevice.DataEvents{
    private BluetoothGatt gatt;
    private EasyBleDevice easyBleDevice;
    public BluetoothGattService gattService;

    EasyGattService(EasyBleDevice easyBleDevice, BluetoothGatt gatt, BluetoothGattService gattService) {
        this.gatt = gatt;
        this.gattService = gattService;
    }

    protected UUID getUUID() {
        return gattService.getUuid();
    }

    // List of callbacks that specify pending read results
    private List<PendingReadResult> pendingReadResults = new ArrayList<>();

    private List<PendingWriteResult> pendingWriteResults = new ArrayList<>();

    /**
     * Asyncronously reads a characteristic
     * @param result
     * @param responseClass
     * @param <T>
     */
    public <T extends GattValue> void read(String uuid, ReadResult<T> result, Class<T> responseClass) {
        // Get the characteristic to read
        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString(uuid));

        // Create a pending responos
        PendingReadResult pendingResponse = new PendingReadResult(characteristic, result, responseClass);
        // Add the callback to the pending callbacks
        pendingReadResults.add(pendingResponse);

        if(!gatt.readCharacteristic(characteristic)) {
            // Failed to read the characteristic, so tell the callback that it failed strait away
            pendingResponse.callback.failure();
            // Remove the callback from the pending list
            pendingReadResults.remove(pendingResponse);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        // Iterate all the pending read callbacks
        for(PendingReadResult pendingResponse : pendingReadResults) {
            // Check if this pendin response is for this characteristic
            if(pendingResponse.characteristic == characteristic) {
                if(status == BluetoothGatt.GATT_SUCCESS)
                    // Fire the response as a success
                    pendingResponse.success(characteristic.getValue());
                else
                    pendingResponse.failure();

                // Remove the callback since it has been fufilled
                pendingReadResults.remove(pendingResponse);
            }
        }
    }

    /**
     * Write a value to a characteristic in this service
     * @param value
     * @param result
     */
    public void write(String uuid, GattValue value, WriteResult result) {
        // Get the characteristic to read
        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString(uuid));

        byte[] rawValue = value.serialize();
        // Serialize the gatt value and set it as the vale of the characteristic
        characteristic.setValue(rawValue);

        // If no result callback was specified, write the characteristic unreliably
        if(result == null) {
            gatt.writeCharacteristic(characteristic);
        }
        else {
            // Create a new pending write result
            PendingWriteResult pendingWriteResult = new PendingWriteResult(characteristic, result, rawValue);
            pendingWriteResults.add(pendingWriteResult);

            // Initiate a reliable write
            gatt.beginReliableWrite();

            if(!gatt.writeCharacteristic(characteristic)) {
                // The write failed,
                pendingWriteResults.remove(pendingWriteResult);

                // Fire the callback as a failure
                pendingWriteResult.failure();
            }

            // Execute the reliable write
            gatt.executeReliableWrite();
        }
    }


    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        for(PendingWriteResult writeResult : pendingWriteResults) {
            if(writeResult.characteristic == characteristic) {
                // If the characteristic value is correct according to the target value, fire the pending
                // write response as a success
                if(characteristic.getValue() == writeResult.rawValue) {
                    writeResult.success();
                    pendingWriteResults.remove(writeResult);
                }
                else {
                    writeResult.failure();
                    pendingWriteResults.remove(writeResult);
                }
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {

    }
}
