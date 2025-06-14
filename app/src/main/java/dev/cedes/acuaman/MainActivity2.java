package dev.cedes.acuaman;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import dev.cedes.acuaman.fragments.about;
import dev.cedes.acuaman.fragments.cerrarsesion;

public class MainActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    private static final String TAG = "MainActivity2";
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 3;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 2;
    private static final int REQUEST_BLUETOOTH_SCAN_PERMISSION = 4;

    private BluetoothAdapter mBtAdapter;
    private BluetoothSocket btSocket;
    private BluetoothDevice DispositivoSeleccionado;
    private ConnectedThread MyConexionBT;
    private ArrayList<String> mNameDevices = new ArrayList<>();
    private ArrayAdapter<String> deviceAdapter;
    Button IdBtnBuscar, IdBtnConectar, IdBtnDesconectar;
    Spinner IdDisEncontrados;
    TextView hola;

    public static final String PREFS_BLUETOOTH = "BluetoothPrefs";
    public static final String KEY_LAST_CONNECTED_DEVICE_MAC = "lastConnectedDeviceMac";
    public static final String KEY_LAST_CONNECTED_DEVICE_NAME = "lastConnectedDeviceName";

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationview);
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            showToast("Bluetooth no disponible en este dispositivo.");
            finish();
            return;
        }

        // --- NUEVA LÓGICA: Redirigir a inter.class si ya hay un dispositivo Bluetooth guardado ---
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_BLUETOOTH, MODE_PRIVATE);
        String lastMac = sharedPrefs.getString(KEY_LAST_CONNECTED_DEVICE_MAC, null);

        if (lastMac != null && mBtAdapter.isEnabled()) {
            // Si el Bluetooth ya está encendido y hay un dispositivo guardado, ir directamente a inter.class
            Intent intent = new Intent(MainActivity2.this, inter.class);
            startActivity(intent);
            finish(); // Cierra MainActivity2 para que no se pueda volver atrás
            return; // Sale de onCreate para no ejecutar el resto del código de MainActivity2
        }
        // --- FIN NUEVA LÓGICA ---

        requestBluetoothPermissions();

        // Inicialización de Vistas
        IdBtnBuscar = findViewById(R.id.IdBtnBuscar);
        IdBtnConectar = findViewById(R.id.IdBtnConectar);
        hola = findViewById(R.id.texto);
        IdBtnDesconectar = findViewById(R.id.IdBtnDesconectar);
        IdDisEncontrados = findViewById(R.id.IdDisEncontrados);

        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mNameDevices);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        IdDisEncontrados.setAdapter(deviceAdapter);

        IdBtnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(50);
                DispositivosVinculados();
            }
        });

        IdBtnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(50);
                ConectarDispBT();
            }
        });

        IdBtnDesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(50);
                desconectarBT();
            }
        });

        loadLastConnectedDevice();
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_BLUETOOTH_SCAN_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION || requestCode == REQUEST_BLUETOOTH_SCAN_PERMISSION || requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!mBtAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            } else {
                showToast("Permisos de Bluetooth necesarios para la funcionalidad.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                showToast("Bluetooth encendido correctamente.");
                DispositivosVinculados();
            } else {
                showToast("El usuario canceló la activación del Bluetooth.");
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void DispositivosVinculados() {
        if (mBtAdapter == null) {
            showToast("Bluetooth no disponible en este dispositivo.");
            finish();
            return;
        }

        if (!mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        mNameDevices.clear();
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mNameDevices.add(device.getName() + "\n" + device.getAddress());
            }
            deviceAdapter.notifyDataSetChanged();

            IdDisEncontrados.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    String deviceInfo = mNameDevices.get(position);
                    String macAddress = deviceInfo.substring(deviceInfo.indexOf("\n") + 1);
                    DispositivoSeleccionado = mBtAdapter.getRemoteDevice(macAddress);
                    showToast("Seleccionado: " + DispositivoSeleccionado.getName() + " (" + DispositivoSeleccionado.getAddress() + ")");
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    DispositivoSeleccionado = null;
                }
            });
        } else {
            showToast("No hay dispositivos Bluetooth emparejados. Por favor, empareja tu HC-06 primero.");
        }
    }

    @SuppressLint("MissingPermission")
    private void ConectarDispBT() {
        if (DispositivoSeleccionado == null) {
            showToast("Selecciona un dispositivo Bluetooth de la lista o enciende tu Bluetooth.");
            return;
        }

        if (MyConexionBT != null) {
            MyConexionBT.cancel();
            MyConexionBT = null;
        }
        if (btSocket != null && btSocket.isConnected()) {
            desconectarBT();
        }

        try {
            btSocket = DispositivoSeleccionado.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
            btSocket.connect();
            MyConexionBT = new ConnectedThread(btSocket);
            MyConexionBT.start();

            // Guardar MAC y Nombre del dispositivo conectado en SharedPreferences
            SharedPreferences sharedPrefs = getSharedPreferences(PREFS_BLUETOOTH, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(KEY_LAST_CONNECTED_DEVICE_MAC, DispositivoSeleccionado.getAddress());
            editor.putString(KEY_LAST_CONNECTED_DEVICE_NAME, DispositivoSeleccionado.getName());
            editor.apply();

            showToast("Conexión exitosa con " + DispositivoSeleccionado.getName() + ".");
            // Una vez conectado, redirige a la actividad principal de inter
            Intent intent = new Intent(MainActivity2.this, inter.class);
            startActivity(intent);
            finish(); // Cierra esta actividad para que no se pueda volver atrás con el botón "atrás"
        } catch (IOException e) {
            showToast("Error al conectar con el dispositivo: " + e.getMessage());
            Log.e(TAG, "Error al conectar con BT", e);
            try {
                if (btSocket != null) btSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Error al cerrar el socket después de fallo de conexión", closeException);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void desconectarBT() {
        if (MyConexionBT != null) {
            MyConexionBT.cancel();
            MyConexionBT = null;
        }
        if (btSocket != null) {
            try {
                btSocket.close();
                showToast("Bluetooth desconectado.");
            } catch (IOException e) {
                showToast("Error al cerrar la conexión Bluetooth.");
                Log.e(TAG, "Error al cerrar el socket", e);
            }
            btSocket = null;
        }
        // --- MODIFICACIÓN: Ya no borra los datos guardados al desconectar desde MainActivity2 ---
        // SharedPreferences sharedPrefs = getSharedPreferences(PREFS_BLUETOOTH, MODE_PRIVATE);
        // SharedPreferences.Editor editor = sharedPrefs.edit();
        // editor.remove(KEY_LAST_CONNECTED_DEVICE_MAC);
        // editor.remove(KEY_LAST_CONNECTED_DEVICE_NAME);
        // editor.apply();
        // --- FIN MODIFICACIÓN ---

        // Restaurar la visibilidad de los botones si es necesario
        IdBtnBuscar.setVisibility(View.VISIBLE);
        IdBtnConectar.setVisibility(View.VISIBLE);
        IdBtnDesconectar.setVisibility(View.GONE);
    }

    @SuppressLint("MissingPermission")
    private void loadLastConnectedDevice() {
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_BLUETOOTH, MODE_PRIVATE);
        String lastMac = sharedPrefs.getString(KEY_LAST_CONNECTED_DEVICE_MAC, null);
        String lastName = sharedPrefs.getString(KEY_LAST_CONNECTED_DEVICE_NAME, null);

        if (lastMac != null && mBtAdapter != null) {
            mNameDevices.clear(); // Limpiar antes de añadir
            mNameDevices.add(lastName + "\n" + lastMac);
            deviceAdapter.notifyDataSetChanged();
            IdDisEncontrados.setSelection(0); // Seleccionar el elemento en el spinner
            DispositivoSeleccionado = mBtAdapter.getRemoteDevice(lastMac);
            showToast("Último dispositivo conectado guardado: " + lastName);
        } else {
            showToast("No hay dispositivo Bluetooth guardado. Por favor, selecciona uno.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Nota: Si MainActivity2 se cierra porque se redirigió a inter,
        // no es necesario desconectar aquí ya que inter.class gestionará su propia conexión.
        // Si MainActivity2 se cierra por alguna otra razón (ej. botón Atrás sin haber conectado),
        // asegurar la desconexión si hay una conexión activa.
        if (btSocket != null && btSocket.isConnected()) {
            desconectarBT();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        if (menuItem.getItemId() == R.id.home){
            // Si el home está en inter, redirige allí.
            Intent intent = new Intent(this, inter.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.personas){
            fragmentManager=getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, new cerrarsesion());
            fragmentTransaction.commit();
        } else if (menuItem.getItemId() == R.id.categotrias){
            // Asumiendo que 'categorias' ahora lleva a 'tipos' para seleccionar el tinaco
            Intent intent = new Intent(this, tipos.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.about){
            fragmentManager=getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, new about());
            fragmentTransaction.commit();
        }
        return false;
    }

    // Clase interna ConnectedThread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                showToast("Error al crear el flujo de datos.");
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String data = new String(buffer, 0, bytes);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hola.setText(data); // Muestra los datos aquí para depuración en MainActivity2
                        }
                    });
                } catch (IOException e) {
                    showToast("Conexión Bluetooth perdida.");
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            } catch (IOException e) {
                showToast("Error al enviar datos.");
                Log.e(TAG, "Error al escribir en OutputStream", e);
            }
        }

        public void cancel() {
            try {
                mmInStream.close();
                mmOutStream.close();
                if (btSocket != null) btSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error al cerrar streams/socket", e);
            }
        }
    }
}