package com.example.kardiamobile;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> deviceList; // Liste für gefundene Geräte
    private ArrayAdapter<String> arrayAdapter; // Adapter für die Liste
    private AlertDialog scanDialog; // Dialog-Fenster für den Scan

    @SuppressLint("MissingSuperCall")
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
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                System.out.println("Scan wurde gestartet");
                deviceList.clear(); // Liste leeren
                arrayAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                System.out.println("Scan wurde beendet");
                Toast.makeText(MainActivity.this, "Scan abgeschlossen", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null) {
                    String deviceName = device.getName();
                    System.out.println("Gefundenes Gerät: " + deviceName);
                    if (!deviceList.contains(deviceName)) {
                        deviceList.add(deviceName); // Gerät zur Liste hinzufügen
                        arrayAdapter.notifyDataSetChanged(); // Adapter aktualisieren
                    }
                }
            }
        }
    };

    public void startBluetoothScan(View view) {
        // Überprüfen, ob Bluetooth aktiviert ist
        if (!bluetoothAdapter.isEnabled()) {
            promptEnableBluetooth();
            return;
        }

        // Überprüfen, ob der Standort aktiviert ist
        if (!isLocationEnabled()) {
            promptEnableLocation();
            return;
        }

        // Berechtigungen überprüfen
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

        // Popup-Dialog für den Scan anzeigen
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

    private void promptEnableBluetooth() {
        new AlertDialog.Builder(this)
                .setTitle("Bluetooth aktivieren")
                .setMessage("Bluetooth ist deaktiviert. Möchten Sie es einschalten?")
                .setPositiveButton("Ja", (dialog, which) -> {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBtIntent);
                })
                .setNegativeButton("Nein", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void promptEnableLocation() {
        new AlertDialog.Builder(this)
                .setTitle("Standort aktivieren")
                .setMessage("Der Standort ist deaktiviert. Möchten Sie ihn einschalten?")
                .setPositiveButton("Ja", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Nein", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private boolean isLocationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return getSystemService(android.location.LocationManager.class).isLocationEnabled();
        } else {
            return Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, 0) != 0;
        }
    }

    private void showScanDialog() {
        // Liste initialisieren
        deviceList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);

        // Dialog mit ListView erstellen
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setTitle("Gefundene Geräte");
        builder.setCancelable(false);
        builder.setNegativeButton("Abbrechen", (dialog, which) -> {
            bluetoothAdapter.cancelDiscovery(); // Scan abbrechen
            unregisterReceiver(mReceiver); // Receiver abmelden
            dialog.dismiss();
        });

        ListView listView = new ListView(this);
        listView.setAdapter(arrayAdapter);
        builder.setView(listView);

        scanDialog = builder.create();
        scanDialog.show();
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
