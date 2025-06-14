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
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import dev.cedes.acuaman.fragments.about;
import dev.cedes.acuaman.fragments.cerrarsesion;

public class inter extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Constantes para SharedPreferences (capacidad del tinaco)
    public static final String PREFS_NAME = "RotoplasPrefs";
    public static final String KEY_TINACO_CAPACITY = "tinacoCapacity";

    // Constantes para SharedPreferences (Bluetooth) - DEBEN SER LAS MISMAS QUE EN MainActivity2
    public static final String PREFS_BLUETOOTH = "BluetoothPrefs";
    public static final String KEY_LAST_CONNECTED_DEVICE_MAC = "lastConnectedDeviceMac";
    public static final String KEY_LAST_CONNECTED_DEVICE_NAME = "lastConnectedDeviceName";

    // Variables UI
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    TextView tvPorcentaje;
    LineChart chartNivelAgua;
    Button btnConectarBT, btnDesconectarBT;
    Button configuracion;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    // Variables Bluetooth
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDevice bluetoothDevice = null;
    private BluetoothSocket bluetoothSocket = null;
    private ConnectedThread connectedThread;
    private Handler bluetoothHandler;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Constantes de mensajes para el Handler
    public static final int MESSAGE_READ = 0;
    public static final int MESSAGE_WRITE = 1;
    public static final int MESSAGE_TOAST = 2;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;

    // Variable para la capacidad del tinaco seleccionada
    private int tinacoCapacity = 0;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inter);

        // Inicializar UI
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationview);
        navigationView.setNavigationItemSelectedListener(this);
        tvPorcentaje = findViewById(R.id.tv_porcentaje);
        chartNivelAgua = findViewById(R.id.chart_nivel_agua);
        btnConectarBT = findViewById(R.id.btn_conectar_bt);
        btnDesconectarBT = findViewById(R.id.btn_desconectar_bt);
        configuracion = findViewById(R.id.configuracion);

        // Configurar Navigation Drawer
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        // Cargar la capacidad del tinaco al iniciar la actividad
        loadTinacoCapacity();

        // Configurar MPAndroidChart
        setupChart();

        // Configurar Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast("Bluetooth no disponible en este dispositivo.");
            return;
        }

        // Solicitar permisos de Bluetooth si son necesarios (Android 12+)
        checkBluetoothPermissions();

        // Configurar Handler para el hilo de lectura Bluetooth
        bluetoothHandler = new Handler(Looper.getMainLooper(), msg -> {
            switch (msg.what) {
                case MESSAGE_READ:
                    String readMessage = new String((byte[]) msg.obj, 0, msg.arg1);
                    try {
                        int distanciaCM = Integer.parseInt(readMessage.trim());
                        updateWaterLevel(distanciaCM);
                    } catch (NumberFormatException e) {
                        Log.e("Bluetooth", "Error al parsear distancia: '" + readMessage + "'", e);
                        showToast("Error al recibir datos del sensor. Formato incorrecto.");
                    }
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        });

        // Cargar el último dispositivo Bluetooth conectado desde SharedPreferences
        loadLastConnectedBluetoothDevice();

        // Listeners para los botones Bluetooth
        btnConectarBT.setOnClickListener(v -> conectarBluetooth());
        btnDesconectarBT.setOnClickListener(v -> desconectarBluetooth());

        // Listener para el botón "configuracion" (ahora para configurar el Tinaco)
        configuracion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(50);
                // Ir a la actividad 'tipos' para seleccionar el tinaco
                Intent intent = new Intent(inter.this, tipos.class);
                startActivity(intent);
                showToast("Abriendo configuración de tinaco...");
            }
        });
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    // --- Métodos de Bluetooth ---

    private void checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            enableBluetoothIfNecessary();
        }
    }

    @SuppressLint("MissingPermission")
    private void enableBluetoothIfNecessary() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                enableBluetoothIfNecessary();
            } else {
                showToast("Permisos de Bluetooth necesarios para la funcionalidad.");
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                showToast("Bluetooth habilitado.");
            } else {
                showToast("Bluetooth no habilitado. No se puede conectar.");
            }
        }
    }

    // Nuevo método para cargar el último dispositivo Bluetooth conectado
    @SuppressLint("MissingPermission")
    private void loadLastConnectedBluetoothDevice() {
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_BLUETOOTH, MODE_PRIVATE);
        String lastMac = sharedPrefs.getString(KEY_LAST_CONNECTED_DEVICE_MAC, null);
        String lastName = sharedPrefs.getString(KEY_LAST_CONNECTED_DEVICE_NAME, "desconocido");

        if (lastMac != null && bluetoothAdapter != null) {
            try {
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(lastMac);
                showToast("Dispositivo Bluetooth guardado: " + lastName + " (" + lastMac + ")");
                btnConectarBT.setText("CONECTAR BT (" + lastName + ")");
            } catch (IllegalArgumentException e) {
                Log.e("Bluetooth", "MAC Address inválida guardada: " + lastMac, e);
                showToast("MAC Address Bluetooth guardada inválida.");
                bluetoothDevice = null;
                btnConectarBT.setText("CONECTAR BT"); // Resetear texto
            }
        } else {
            showToast("No hay dispositivo Bluetooth guardado. Ve a Configuración.");
            btnConectarBT.setText("CONECTAR BT"); // Resetear texto
        }
    }

    private void conectarBluetooth() {
        if (bluetoothDevice == null) {
            showToast("No hay dispositivo Bluetooth para conectar. Ve a Configuración.");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            showToast("Bluetooth está apagado. Por favor, enciéndelo.");
            enableBluetoothIfNecessary();
            return;
        }

        // Si ya hay un hilo de conexión o conectado, cancelarlo primero
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            desconectarBluetooth(); // Asegurarse de cerrar la conexión existente
        }

        new ConnectThread(bluetoothDevice).start();
    }

    private void desconectarBluetooth() {
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e("Bluetooth", "Error al cerrar el socket", e);
            }
            bluetoothSocket = null;
        }
        showToast("Bluetooth desconectado.");
        btnConectarBT.setText("CONECTAR BT"); // Restablecer texto del botón
        // IMPORTANTE: NO ELIMINA LOS DATOS GUARDADOS DE SHAREDPREFERENCES AQUÍ,
        // para que se mantenga el rastro del último Bluetooth.
    }

    // Clase para el hilo de conexión Bluetooth
    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;

        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e("Bluetooth", "Socket creation failed", e);
            }
            bluetoothSocket = tmp;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                bluetoothSocket.connect();
                Log.d("Bluetooth", "Conexión Bluetooth exitosa.");
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Conectado al " + mmDevice.getName());
                Message msg = bluetoothHandler.obtainMessage(MESSAGE_TOAST);
                msg.setData(bundle);
                msg.sendToTarget();

                connectedThread = new ConnectedThread(bluetoothSocket);
                connectedThread.start();
            } catch (IOException connectException) {
                Log.e("Bluetooth", "No se pudo conectar al socket", connectException);
                try {
                    if (bluetoothSocket != null) bluetoothSocket.close();
                } catch (IOException closeException) {
                    Log.e("Bluetooth", "No se pudo cerrar el socket de cliente", closeException);
                }
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Fallo la conexión con " + mmDevice.getName());
                Message msg = bluetoothHandler.obtainMessage(MESSAGE_TOAST);
                msg.setData(bundle);
                msg.sendToTarget();
                runOnUiThread(() -> btnConectarBT.setText("RECONECTAR BT"));
            }
        }

        public void cancel() {
            try {
                if (bluetoothSocket != null) bluetoothSocket.close();
            } catch (IOException e) {
                Log.e("Bluetooth", "No se pudo cerrar el socket de conexión", e);
            }
        }
    }

    // Clase para el hilo de lectura de datos Bluetooth
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("Bluetooth", "Error al obtener streams", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes;

            while (true) {
                try {
                    numBytes = mmInStream.read(mmBuffer);
                    bluetoothHandler.obtainMessage(MESSAGE_READ, numBytes, -1, mmBuffer).sendToTarget();
                } catch (IOException e) {
                    Log.d("Bluetooth", "Input stream was disconnected", e);
                    Bundle bundle = new Bundle();
                    bundle.putString("toast", "Desconectado del Bluetooth");
                    Message msg = bluetoothHandler.obtainMessage(MESSAGE_TOAST);
                    msg.setData(bundle);
                    msg.sendToTarget();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Bluetooth", "Error al escribir en OutputStream", e);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Error al enviar datos");
                Message msg = bluetoothHandler.obtainMessage(MESSAGE_TOAST);
                msg.setData(bundle);
                msg.sendToTarget();
            }
        }

        public void cancel() {
            try {
                if (mmInStream != null) mmInStream.close();
                if (mmOutStream != null) mmOutStream.close();
            } catch (IOException e) {
                Log.e("Bluetooth", "No se pudieron cerrar los streams", e);
            }
        }
    }

    // --- Métodos de Lógica del Agua y Gráfica ---

    private void loadTinacoCapacity() {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        tinacoCapacity = sharedPref.getInt(KEY_TINACO_CAPACITY, 0);

        if (tinacoCapacity == 0) {
            showToast("No se ha seleccionado un tipo de tinaco. Por favor, configúralo.");
            // Opcional: Podrías redirigir automáticamente a 'tipos.class' aquí si es obligatorio.
            // Intent intent = new Intent(this, tipos.class);
            // startActivity(intent);
        } else {
            showToast("Tinaco seleccionado: " + tinacoCapacity + "L");
        }
    }

    private void setupChart() {
        chartNivelAgua.setTouchEnabled(true);
        chartNivelAgua.setPinchZoom(true);

        Description desc = new Description();
        desc.setText("Nivel del Agua (%)");
        desc.setTextSize(12f);
        chartNivelAgua.setDescription(desc);

        XAxis xAxis = chartNivelAgua.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = chartNivelAgua.getAxisLeft();
        leftAxis.setTextSize(10f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setLabelCount(11, true);

        chartNivelAgua.getAxisRight().setEnabled(false);

        Legend legend = chartNivelAgua.getLegend();
        legend.setEnabled(false);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);
        chartNivelAgua.setData(data);
    }

    private void addEntry(double porcentaje) {
        LineData data = chartNivelAgua.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) porcentaje), 0);

            data.notifyDataChanged();
            chartNivelAgua.notifyDataSetChanged();

            chartNivelAgua.setVisibleXRangeMaximum(20);
            chartNivelAgua.moveViewToX(data.getEntryCount());

            chartNivelAgua.invalidate();
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Nivel de Agua");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.BLUE);
        set.setCircleColor(Color.BLUE);
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setFillAlpha(65);
        set.setFillColor(Color.BLUE);
        set.setDrawValues(false);
        return set;
    }

    public void updateWaterLevel(int distanciaCM) {
        if (tinacoCapacity == 0) {
            // No se puede calcular el nivel sin la capacidad del tinaco
            return;
        }

        // Calibración de altura del tinaco y del sensor
        double alturaTinacoTotalCM = getAlturaTinaco(tinacoCapacity);
        double distanciaSensorBordeSuperiorCM = 5.0; // Distancia desde el sensor al borde superior del tinaco

        // Altura real del agua
        double alturaAguaCM = alturaTinacoTotalCM - distanciaSensorBordeSuperiorCM - distanciaCM;

        // Máxima altura posible del agua para calcular porcentaje (altura total - espacio del sensor)
        double maxAlturaAgua = alturaTinacoTotalCM - distanciaSensorBordeSuperiorCM;
        if (maxAlturaAgua <= 0) maxAlturaAgua = 1.0; // Evitar división por cero si la calibración es mala

        // Asegurar que la altura del agua no sea negativa o mayor que la máxima
        if (alturaAguaCM < 0) alturaAguaCM = 0;
        if (alturaAguaCM > maxAlturaAgua) alturaAguaCM = maxAlturaAgua;


        double porcentaje = (alturaAguaCM / maxAlturaAgua) * 100;

        tvPorcentaje.setText(String.format("PORCENTAJE: %.1f%%", porcentaje));
        addEntry(porcentaje);
    }

    private double getAlturaTinaco(int capacity) {
        // Alturas de tinacos Rotoplas (ejemplos aproximados, ajusta si es necesario)
        switch (capacity) {
            case 450: return 90.0;  // Altura aproximada en cm para un tinaco de 450L
            case 600: return 100.0; // Altura aproximada en cm para un tinaco de 600L
            case 750: return 120.0; // Altura aproximada en cm para un tinaco de 750L
            case 1100: return 140.0; // Altura aproximada en cm para un tinaco de 1100L
            case 2500: return 180.0; // Altura aproximada en cm para un tinaco de 2500L
            default: return 0.0; // Valor por defecto si no se reconoce la capacidad
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTinacoCapacity(); // Recargar capacidad del tinaco (por si se cambió en tipos.class)
        loadLastConnectedBluetoothDevice(); // Recargar el dispositivo Bluetooth guardado
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Asegurarse de cerrar la conexión Bluetooth al destruir la actividad
        if (connectedThread != null) {
            connectedThread.cancel();
        }
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e("Bluetooth", "Error al cerrar el socket en onDestroy", e);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        if (menuItem.getItemId() == R.id.home) {
            showToast("Ya estás en la pantalla principal.");
        } else if (menuItem.getItemId() == R.id.personas) {
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, new cerrarsesion());
            fragmentTransaction.commit();
        } else if (menuItem.getItemId() == R.id.categotrias) { // Opción para ir a seleccionar tinaco
            Intent intent = new Intent(this, tipos.class);
            startActivity(intent);
            // No finalizar 'inter' aquí, para poder volver
        } else if (menuItem.getItemId() == R.id.about) {
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, new about());
            fragmentTransaction.commit();
        }
        return false;
    }
}