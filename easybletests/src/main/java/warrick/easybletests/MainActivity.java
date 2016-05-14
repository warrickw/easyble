package warrick.easybletests;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class MainActivity extends AppCompatActivity {

    ListView devicesList;

    LinkedHashSet<BluetoothDevice> devices = new LinkedHashSet<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devicesList = (ListView)findViewById(R.id.listView);


        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                scanner.stopScan(callback);

                startActivity(new Intent(MainActivity.this, Connect.class).putExtra("mac", devices.toArray(new BluetoothDevice[devices.size()])[i].getAddress()));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0)
                .setUseHardwareBatchingIfSupported(false).build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().build());
        scanner.startScan(filters, settings, callback);
    }

    @Override
    protected void onPause() {
        super.onPause();

        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(callback);
    }

    private void updateDevices() {
        List<String> deviceNames = new ArrayList<>();
        for(BluetoothDevice d : devices) {
            deviceNames.add(d.getName());
        }

        devicesList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceNames));
    }

    private ScanCallback callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult r) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!devices.contains(r.getDevice())) {
                        devices.add(r.getDevice());
                        updateDevices();
                    }
                }
            });
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(ScanResult r : results) {
                        if(!devices.contains(r.getDevice())) {
                            devices.add(r.getDevice());
                            updateDevices();
                        }
                    }
                }
            });
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };
}
