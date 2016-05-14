package warrick.easyble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Provides a simple interface for interacting with a BLE GATT service and the attributes within a service.
 */
public class EasyBleService {
    private BluetoothGatt gatt;
    private EasyBleDevice easyBleDevice;
    public BluetoothGattService gattService;

    EasyBleService(EasyBleDevice easyBleDevice, BluetoothGatt gatt, BluetoothGattService gattService) {
        this.easyBleDevice = easyBleDevice;
        this.gatt = gatt;
        this.gattService = gattService;
    }

    protected UUID getUUID() {
        return gattService.getUuid();
    }

    // List of callbacks that specify pending read results
    private List<PendingReadResult> pendingReadResults = new ArrayList<>();

    private List<PendingWriteResult> pendingWriteResults = new ArrayList<>();

    private List<NotificationListener> notificationListeners = new ArrayList<>();

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

    public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
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
        });
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


            if(!gatt.writeCharacteristic(characteristic)) {
                // The write failed,
                pendingWriteResults.remove(pendingWriteResult);

                // Fire the callback as a failure
                pendingWriteResult.failure();
            }
        }
    }

     void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for(PendingWriteResult writeResult : pendingWriteResults) {
                    if(writeResult.characteristic == characteristic) {
                        // If the characteristic value is correct according to the target value, fire the pending
                        // write response as a success
                        if(characteristic.getValue() == writeResult.rawValue && status == BluetoothGatt.GATT_SUCCESS) {
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
        });
    }

    public <T extends GattValue> void registerNotification(String uuid, ReadResult<T> callback, Class valueClass) {
        // Get the characteristic to read
        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString(uuid));

        // Count the number of attributes already receiving ble notify events
        int count = 0;
        for (NotificationListener listener :
                notificationListeners) {
            // If this listener is for the characteristic that the callback was deregistered for, count it
            if (listener.characteristic == characteristic)
                count++;
        }
        // If there were no pre-existing listers for this attribute, then ble notifications need enabled for the attribute
        // to allow the peripheral to start sending updates.
        if(count == 0) {
            easyBleDevice.enableNotifications(characteristic);
        }


        // Add the callback to the list of notification listeners
        NotificationListener notificationListener = new NotificationListener(characteristic, callback, valueClass);
        notificationListeners.add(notificationListener);
    }

    /**
     * Stops sending charicteristic changed events to the specified callback. This will also fully disable
     * ble attribute notificaitons if there are no other listeners using the same attribute
     * @param callback
     */
    public void unregisterNotification(ReadResult callback) {
        BluetoothGattCharacteristic characteristic = null;

        // Find the callback and remove it from the list
        for (NotificationListener listener :
                notificationListeners) {
            if (listener.callback == callback) {
                // Store the characteristic that we are removing notifications for
                characteristic = listener.characteristic;
                notificationListeners.remove(callback);
            }
        }

        // If a callback was succesfully removed, count the number of other callbacks that are for the same characteristic.
        // This is so we can fully disable notificaitons if there are no listeners on the attribute
        if(characteristic != null) {
            // Count the number of listeners registered for notifications for the characteristic
            int count = 0;
            for (NotificationListener listener :
                    notificationListeners) {
                // If this listener is for the characteristic that the callback was deregistered for, count it
                if (listener.characteristic == characteristic)
                    count++;
            }

            // Since there are now no listers for this attribute, disable the ble notify events completely
            if(count == 0){
                easyBleDevice.disableNotifications(characteristic);
            }
        }
    }

    public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // Iterate all registered notifications and send events if they are for this characteristic
                for (NotificationListener listener :
                        notificationListeners) {
                    if (listener.characteristic == characteristic) {
                        listener.changed(characteristic.getValue());
                    }
                }
            }
        });
    }

    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

    }

    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

    }

    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {

    }
}
