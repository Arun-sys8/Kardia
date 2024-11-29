package com.example.kardiamobile;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> deviceList;
    private ArrayAdapter<String> arrayAdapter;
    private AlertDialog scanDialog;

    private SeekBar seekBar1; // SeekBar 1
    private SeekBar seekBar2; // SeekBar 2
    private Button lightButton, mediumButton, strongButton; // Buttons
    private TextView lockStatusText; // Statusanzeige für die Sperre

    private boolean isLocked = false; // Status für SeekBar-Sperre

    @SuppressLint({"MissingSuperCall", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth wird nicht unterstützt", Toast.LENGTH_LONG).show();
        }

        // Find Views
        seekBar1 = findViewById(R.id.appCompatSeekBar);
        seekBar2 = findViewById(R.id.appCompatSeekBar2);
        lightButton = findViewById(R.id.Button7);
        mediumButton = findViewById(R.id.button9);
        strongButton = findViewById(R.id.button8);
        lockStatusText = findViewById(R.id.lockStatusText); // Statusanzeige

        // Set Button Click Listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        lightButton.setOnClickListener(v -> setSeekBarsProgress(10));
        mediumButton.setOnClickListener(v -> setSeekBarsProgress(50));
        strongButton.setOnClickListener(v -> setSeekBarsProgress(100));
    }

    private void setSeekBarsProgress(int progress) {
        if (!isLocked) {
            seekBar1.setProgress(progress);
            seekBar2.setProgress(progress);
            seekBar1.setEnabled(false); // Sperrt die SeekBar
            seekBar2.setEnabled(false); // Sperrt die SeekBar
            isLocked = true; // Sperren aktivieren
            lockStatusText.setText("SeekBars gesperrt. Drücke erneut, um zu entsperren.");
        } else {
            seekBar1.setEnabled(true); // Entsperrt die SeekBar
            seekBar2.setEnabled(true); // Entsperrt die SeekBar
            isLocked = false; // Sperren deaktivieren
            lockStatusText.setText("SeekBars entsperrt.");
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                System.out.println("Scan wurde gestartet");
                deviceList.clear();
                arrayAdapter.notifyDataSetChanged();

                // Gekoppelte Geräte hinzufügen
                @SuppressLint("MissingPermission")
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : pairedDevices) {
                    if (!deviceList.contains(device.getName())) {
                        deviceList.add("Gekoppelt: " + device.getName());
                    }
                }
                arrayAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                System.out.println("Scan wurde beendet");
                Toast.makeText(MainActivity.this, "Scan abgeschlossen", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null) {
                    if (!deviceList.contains(device.getName())) {
                        deviceList.add(device.getName()); // Nur den Namen des Geräts hinzufügen
                        arrayAdapter.notifyDataSetChanged();
                    }
                } else {
                    System.out.println("Unbekanntes Gerät gefunden");
                }
            }
        }
    };

    public void startBluetoothScan(View view) {
        if (bluetoothAdapter == null) {
            System.out.println("Bluetooth wird nicht unterstützt");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 2000);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(new String[]{
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 1000);
        } else {
            requestPermissions(new String[]{
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 1000);
        }

        showScanDialog();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.startDiscovery();
        } else {
            System.out.println("Berechtigungen nicht erteilt");
        }
    }

    private void showScanDialog() {
        deviceList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bluetooth-Geräte in der Nähe");
        builder.setCancelable(false);
        builder.setNegativeButton("Abbrechen", (dialog, which) -> {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothAdapter.cancelDiscovery();
            unregisterReceiver(mReceiver);
            dialog.dismiss();
        });

        ListView listView = new ListView(this);
        listView.setAdapter(arrayAdapter);
        builder.setView(listView);

        scanDialog = builder.create();
        scanDialog.show();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String deviceName = deviceList.get(position);
            Toast.makeText(this, "Verbinde mit " + deviceName, Toast.LENGTH_SHORT).show();
            bluetoothAdapter.cancelDiscovery();
            scanDialog.dismiss();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Berechtigungen wurden erteilt");
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothAdapter.startDiscovery();
                }
            } else {
                System.out.println("Berechtigungen wurden verweigert");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            System.out.println("Receiver war nicht registriert");
        }
    }
}
