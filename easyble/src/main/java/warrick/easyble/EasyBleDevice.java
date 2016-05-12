package warrick.easyble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a simple asynchronous api for communicating with a BLE device over GATT
 */
public class EasyBleDevice {
    private boolean isConnected = false;
    private boolean isServicesDiscovered = false;
    private boolean isConnecting = false;


    private BluetoothGatt gatt;
    public EasyBleDevice() {

    }



    private List<EasyGattService> services = null;

    /**
     * Returns the services that this device offers
     * @return
     */
    public @Nullable List<EasyGattService> getServices() {
        return services;
    }

    /**
     * Get a specific service
     * @param uuid The unique identifyer of the service to get
     * @return
     */
    public @Nullable EasyGattService getService(String uuid) {
        for(EasyGattService service : services) {
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
                        break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS) {
                // Mark the services as discovered
                isServicesDiscovered = true;

                // Create an EasyGattService for every service
                services = new ArrayList<>();
                for (BluetoothGattService service : gatt.getServices()) {
                    services.add(new EasyGattService(EasyBleDevice.this, gatt, service));
                }
            }
            else {
                isServicesDiscovered = false;
                services = null;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            for (DataEvents e :
                    services) {
                e.onCharacteristicRead(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            for (DataEvents e :
                    services) {
                e.onCharacteristicWrite(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            for (DataEvents e :
                    services) {
                e.onCharacteristicChanged(gatt, characteristic);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            for (DataEvents e :
                    services) {
                e.onDescriptorRead(gatt, descriptor, status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            for (DataEvents e :
                    services) {
                e.onDescriptorWrite(gatt, descriptor, status);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            for (DataEvents e :
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

    public interface DataEvents {
        void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);
        void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);
        void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);
        void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);
        void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);
        void onReliableWriteCompleted(BluetoothGatt gatt, int status);
    }
}
