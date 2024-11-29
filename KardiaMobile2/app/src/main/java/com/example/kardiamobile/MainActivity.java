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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // BluetoothAdapter ist die zentrale API zur Steuerung der Bluetooth-Funktionen.
    private BluetoothAdapter bluetoothAdapter;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Aktiviert ein modernes Edge-to-Edge-Layout-Design.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Passt die Ansicht an die Systemleisten (z. B. Status- und Navigationsleiste) an.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisiert den Bluetooth-Adapter.
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Überprüft, ob Bluetooth auf dem Gerät unterstützt wird.
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth wird nicht unterstützt", Toast.LENGTH_LONG).show();
        }
    }

    // BroadcastReceiver verarbeitet Bluetooth-Ereignisse.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                System.out.println("Scann wurde gestartet");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                System.out.println("Scann wurde beendet");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Gerätedetails extrahieren, wenn ein Gerät gefunden wird.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    System.out.println("Gefundenes Gerät: " + device.getName() + " - " + device.getAddress());
                } else {
                    System.out.println("Unbekanntes Gerät gefunden");
                }
            }
        }
    };

    // Startet die Bluetooth-Gerätesuche.
    public void startBluetoothScan(View view) {
        // Prüft, ob Bluetooth unterstützt wird.
        if (bluetoothAdapter == null) {
            System.out.println("Bluetooth wird nicht unterstützt");
            return;
        }

        // Falls Bluetooth deaktiviert ist, fordert die Aktivität den Benutzer auf, es zu aktivieren.
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 2000);
            return;
        }

        // Fordert Berechtigungen basierend auf der Android-Version an.
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

        // Registriert den BroadcastReceiver, um Bluetooth-Ereignisse zu empfangen.
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        // Startet die Suche nach Bluetooth-Geräten, wenn die Berechtigungen erteilt wurden.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.startDiscovery();
        } else {
            System.out.println("Berechtigungen nicht erteilt");
        }
    }

    // Verarbeitet die Ergebnisse der Berechtigungsanfragen.
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

    // Freigeben von Ressourcen beim Beenden der Aktivität.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // Entfernt den BroadcastReceiver, um Ressourcenlecks zu vermeiden.
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            System.out.println("Receiver war nicht registriert");
        }
    }
}
