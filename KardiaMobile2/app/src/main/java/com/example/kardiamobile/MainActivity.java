package com.example.kardiamobile;

// Import von benötigten Android-Bibliotheken
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

/**
 * Hauptaktivität der App. Diese Klasse verwaltet die Bluetooth-Suche,
 * sowie die Steuerung von SeekBars und Buttons zur Demonstration der Funktionalität.
 */
public class MainActivity extends AppCompatActivity {

    // Bluetooth-Adapter zum Verwalten von Bluetooth-Funktionen
    private BluetoothAdapter bluetoothAdapter;

    // Liste der gefundenen Bluetooth-Geräte
    private ArrayList<String> deviceList;

    // Adapter für die Anzeige der Geräteliste in der AlertDialog-Box
    private ArrayAdapter<String> arrayAdapter;

    // Dialog zur Anzeige der gefundenen Geräte
    private AlertDialog scanDialog;

    // UI-Elemente (SeekBars, Buttons und TextView)
    private SeekBar seekBar1;
    private SeekBar seekBar2;
    private Button lightButton, mediumButton, strongButton;
    private TextView lockStatusText;

    // Statusvariable zur Steuerung der SeekBars (gesperrt oder nicht)
    private boolean isLocked = false;

    /**
     * Diese Methode wird aufgerufen, wenn die Aktivität erstellt wird.
     * Sie initialisiert die UI-Elemente und Bluetooth-Funktionen.
     */
    @SuppressLint({"MissingSuperCall", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Aktiviert Edge-to-Edge-Funktionalität
        setContentView(R.layout.activity_main); // Legt das Layout für die Aktivität fest

        // Passt die Abstände an, um Systemleisten zu berücksichtigen
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisiert den Bluetooth-Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Überprüft, ob Bluetooth unterstützt wird
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth wird nicht unterstützt", Toast.LENGTH_LONG).show();
        }

        // UI-Elemente finden und initialisieren
        seekBar1 = findViewById(R.id.appCompatSeekBar);
        seekBar2 = findViewById(R.id.appCompatSeekBar2);
        lightButton = findViewById(R.id.Button7);
        mediumButton = findViewById(R.id.button9);
        strongButton = findViewById(R.id.button8);
        lockStatusText = findViewById(R.id.lockStatusText);

        // Setzt die Klick-Listener für die Buttons
        setupButtonListeners();
    }

    /**
     * Konfiguriert die Button-Klick-Listener zur Steuerung der SeekBars.
     */
    private void setupButtonListeners() {
        lightButton.setOnClickListener(v -> setSeekBarsProgress(10)); // Leichte Einstellung
        mediumButton.setOnClickListener(v -> setSeekBarsProgress(50)); // Mittlere Einstellung
        strongButton.setOnClickListener(v -> setSeekBarsProgress(100)); // Starke Einstellung
    }

    /**
     * Setzt den Fortschritt der SeekBars und sperrt/entsperrt sie.
     * @param progress Der Fortschrittswert, der eingestellt werden soll.
     */
    private void setSeekBarsProgress(int progress) {
        if (!isLocked) {
            seekBar1.setProgress(progress); // Fortschritt der ersten SeekBar setzen
            seekBar2.setProgress(progress); // Fortschritt der zweiten SeekBar setzen
            seekBar1.setEnabled(false); // Sperrt die erste SeekBar
            seekBar2.setEnabled(false); // Sperrt die zweite SeekBar
            isLocked = true; // Aktiviert den Sperrstatus
            lockStatusText.setText("SeekBars gesperrt. Drücke erneut, um zu entsperren.");
        } else {
            seekBar1.setEnabled(true); // Entsperrt die erste SeekBar
            seekBar2.setEnabled(true); // Entsperrt die zweite SeekBar
            isLocked = false; // Deaktiviert den Sperrstatus
            lockStatusText.setText("SeekBars entsperrt.");
        }
    }

    /**
     * BroadcastReceiver zur Behandlung von Bluetooth-Ereignissen wie
     * Starten oder Beenden der Suche und Finden von Geräten.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                System.out.println("Scan wurde gestartet");
                deviceList.clear(); // Liste der Geräte leeren
                arrayAdapter.notifyDataSetChanged(); // UI aktualisieren

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
                        deviceList.add(device.getName()); // Gerät hinzufügen
                        arrayAdapter.notifyDataSetChanged(); // UI aktualisieren
                    }
                } else {
                    System.out.println("Unbekanntes Gerät gefunden");
                }
            }
        }
    };

    /**
     * Startet die Bluetooth-Suche nach Geräten in der Nähe.
     * @param view Die Ansicht, die die Methode auslöst (Button-Klick).
     */
    public void startBluetoothScan(View view) {
        if (bluetoothAdapter == null) {
            System.out.println("Bluetooth wird nicht unterstützt");
            return;
        }

        // Überprüft, ob Bluetooth aktiviert ist
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 2000); // Fordert den Benutzer auf, Bluetooth zu aktivieren
            return;
        }

        // Fordert Berechtigungen für die Bluetooth-Suche an (je nach Android-Version)
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

        showScanDialog(); // Zeigt den Scan-Dialog an

        // Registriert den BroadcastReceiver für Bluetooth-Ereignisse
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        // Startet die Gerätesuche, falls die Berechtigungen erteilt wurden
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.startDiscovery();
        } else {
            System.out.println("Berechtigungen nicht erteilt");
        }
    }

    /**
     * Zeigt einen Dialog an, der die gefundenen Bluetooth-Geräte auflistet.
     */
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
            bluetoothAdapter.cancelDiscovery(); // Beendet die Suche
            unregisterReceiver(mReceiver); // Deregistriert den Receiver
            dialog.dismiss();
        });

        ListView listView = new ListView(this);
        listView.setAdapter(arrayAdapter);
        builder.setView(listView);

        scanDialog = builder.create();
        scanDialog.show();

        // Klick-Listener für die Auswahl eines Geräts
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String deviceName = deviceList.get(position);
            Toast.makeText(this, "Verbinde mit " + deviceName, Toast.LENGTH_SHORT).show();
            bluetoothAdapter.cancelDiscovery(); // Beendet die Suche
            scanDialog.dismiss(); // Schließt den Dialog
        });
    }

    /**
     * Callback-Methode, um auf das Ergebnis von Berechtigungsanforderungen zu reagieren.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Berechtigungen wurden erteilt");
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothAdapter.startDiscovery(); // Startet die Suche, wenn Berechtigungen erteilt wurden
                }
            } else {
                System.out.println("Berechtigungen wurden verweigert");
            }
        }
    }

    /**
     * Wird aufgerufen, wenn die Aktivität zerstört wird. Deregistriert den BroadcastReceiver.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mReceiver); // Versucht, den Receiver zu deregistrieren
        } catch (IllegalArgumentException e) {
            System.out.println("Receiver war nicht registriert");
        }
    }
}
