package warrick.easybletests;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import warrick.easyble.EasyBleDevice;
import warrick.easyble.EasyBleService;
import warrick.easyble.WriteResult;
import warrick.easyble.services.GattBatteryService;
import warrick.easyble.values.Int64GattValue;

public class Connect extends AppCompatActivity {

    EasyBleDevice device;
    GattBatteryService batteryService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        String mac = getIntent().getStringExtra("mac");

        device = new EasyBleDevice(this, mac);

        device.addStateChangeCallback(new EasyBleDevice.DeviceStateChange() {
            @Override
            public void ready() {
                final EasyBleService service = device.getService("00002654-0000-1000-8000-00805f9b34fb");

                batteryService = GattBatteryService.openForDevice(device);
                batteryService.getPercent(getCallback);

//                service.read("00001923-0000-1000-8000-00805f9b34fb", new ReadResult<Int64GattValue>() {
//                    @Override
//                    public void success(Int64GattValue value) {
//                        Log.i("BLE", "Success");
//                    }
//
//                    @Override
//                    public void failure() {
//                        Log.i("BLE", "Success");
//                    }
//                }, Int64GattValue.class);

//                  service.write("00001923-0000-1000-8000-00805f9b34fb", new Int64GattValue(System.currentTimeMillis()), new WriteResult() {
//                            @Override
//                            public void success() {
//                                Log.i("BLE", "Success");
//                            }
//
//                            @Override
//                            public void failure() {
//                                Log.i("BLE", "Failure");
//                            }
//                   });

                new Handler(getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {



                    }
                }, 1000);
            }

            @Override
            public void disconnected() {

            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        device.dispose();
    }

    GattBatteryService.Get getCallback = new GattBatteryService.Get() {
        @Override
        public void successs(int percent) {
            Toast.makeText(Connect.this, "Battery: " + percent, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void failure() {
            Toast.makeText(Connect.this, "Failed battery", Toast.LENGTH_SHORT).show();
        }
    };
}
