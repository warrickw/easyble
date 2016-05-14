package warrick.easyble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Provides a simple asynchronous api for communicating with a BLE device over GATT
 */
public class EasyBleDevice {
    private boolean isConnected = false;
    private boolean isServicesDiscovered = false;
    private boolean isConnecting = false;

    BluetoothGatt gatt;
    BluetoothDevice bluetoothDevice;
    public EasyBleDevice(Context context, String address) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothDevice = bluetoothManager.getAdapter().getRemoteDevice(address);
        gatt = bluetoothDevice.connectGatt(context, false, gattCallback);
    }

    public void dispose() {
        gatt.close();
    }

    private List<EasyBleService> services = null;

    /**
     * Returns the services that this device offers
     * @return
     */
    public @Nullable List<EasyBleService> getServices() {
        return services;
    }

    /**
     * Get a specific service
     * @param uuid The unique identifyer of the service to get
     * @return
     */
    public @Nullable
    EasyBleService getService(String uuid) {
        for(EasyBleService service : services) {
            // If the service's id is the service we are trying to find, return it
            if(service.getUUID().toString().equals(uuid)) {
                return service;
            }
        }
        return null;
    }

    /**
     * Internal gatt callback to receive all gatt events
     */
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState) {
                case BluetoothGatt.STATE_CONNECTING:
                    isConnected = false;
                    isConnecting = true;
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    isConnected = true;
                    isConnecting = false;
                    // If services havn't yet been discovered, discover them
                    if(!isServicesDiscovered)
                        gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTING:
                    isConnecting = false;
                    break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        isConnected = false;
                        // Fire the disconnected callback
                        for (DeviceStateChange c :
                                stateChangeCallbacks) {
                            c.disconnected();
                        }
                        break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS) {
                // Mark the services as discovered
                isServicesDiscovered = true;

                // Create an EasyBleService for every service
                services = new ArrayList<>();
                for (BluetoothGattService service : gatt.getServices()) {
                    services.add(new EasyBleService(EasyBleDevice.this, gatt, service));
                }

                // This device is now ready since it has discovered all the services
                for (DeviceStateChange c :
                        stateChangeCallbacks) {
                    c.ready();
                }
            }
            else {
                isServicesDiscovered = false;
                services = null;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            for (EasyBleService e :
                    services) {
                e.onCharacteristicRead(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            for (EasyBleService e :
                    services) {
                e.onCharacteristicWrite(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            for (EasyBleService e :
                    services) {
                e.onCharacteristicChanged(gatt, characteristic);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            for (EasyBleService e :
                    services) {
                e.onDescriptorRead(gatt, descriptor, status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            for (EasyBleService e :
                    services) {
                e.onDescriptorWrite(gatt, descriptor, status);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            for (EasyBleService e :
                    services) {
                e.onReliableWriteCompleted(gatt, status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {

        }
    };

    public void enableNotifications(BluetoothGattCharacteristic characteristic) {
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));

        if(descriptor != null) {
            gatt.setCharacteristicNotification(characteristic, true);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
    }

    public void disableNotifications(BluetoothGattCharacteristic characteristic) {
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));

        if(descriptor != null) {
            gatt.setCharacteristicNotification(characteristic, false);
            descriptor.setValue(new byte[] { 0x00, 0x00 });
            gatt.writeDescriptor(descriptor);

        }
    }

    private List<DeviceStateChange> stateChangeCallbacks = new ArrayList<>();
    public void addStateChangeCallback(DeviceStateChange callback) {
        stateChangeCallbacks.add(callback);
    }

    public interface DeviceStateChange {
         void ready();
        void disconnected();
    }
}