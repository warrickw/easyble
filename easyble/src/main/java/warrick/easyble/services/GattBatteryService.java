package warrick.easyble.services;

import android.support.annotation.Nullable;

import warrick.easyble.EasyBleDevice;
import warrick.easyble.EasyBleService;
import warrick.easyble.GattValue;
import warrick.easyble.ReadResult;
import warrick.easyble.values.ByteGattValue;

/**
 * Defines a generic ble gatt battery service that adheres to the specification (well, ish)
 */
public class GattBatteryService {
    /**
     * Attempts to open a battery service on the given device, or returns null if the service is not available
     * @param easyBleDevice
     * @return
     */
    public static @Nullable GattBatteryService openForDevice(EasyBleDevice easyBleDevice) {
        EasyBleService service = easyBleDevice.getService("0000180f-0000-1000-8000-00805f9b34fb");

        if(service == null)
            return null;

        GattBatteryService batteryService = new GattBatteryService(service);

        return batteryService;
    }

    private EasyBleService service;
    GattBatteryService(EasyBleService service) {
        this.service = service;
    }

    private int percent = 0;
    private long updateTime = 0;

    /**
     * Returns the raw cached percent
     * @return
     */
    public int getPercent() {
        return percent;
    }

    /**
     * Get the value asyncronously, requesting it from the ble peer
     * @param callback
     */
    public void getPercent(final Get callback) {
        service.read("00002a19-0000-1000-8000-00805f9b34fb", new ReadResult<ByteGattValue>() {
            @Override
            public void success(ByteGattValue value) {
                percent = value.getValue() & 0xFF;
                updateTime = System.currentTimeMillis();

                callback.successs(percent);
            }

            @Override
            public void failure() {
                callback.failure();
            }
        }, ByteGattValue.class);
    }

    /**
     * Returns whether the battery percent has been read from the peer yet
     * @return
     */
    public boolean hasValue() {
        return updateTime != 0;
    }

    /**
     * Returns the last time that the value was updated from the peer
     * @return
     */
    public long getUpdateTime() {
        return updateTime;
    }

    /**
     * Defines an interface to provide callbacks when the battery level is updated by the peer
     */
    public interface Get {
        void successs(int percent);
        void failure();
    }
}
