package dev.cedes.acuaman.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import dev.cedes.acuaman.R;

public class categorias extends Fragment {

    private static final String TAG = "MainActivity";
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 3;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 2;
    private BluetoothAdapter mBtAdapter;
    private BluetoothSocket btSocket;
    private BluetoothDevice DispositivoSeleccionado;
    private ConnectedThread MyConexionBT;
    private ArrayList<String> mNameDevices = new ArrayList<>();
    private ArrayAdapter<String> deviceAdapter;
    Button IdBtnBuscar, IdBtnConectar, IdBtnLuz1on, IdBtnLuz3ON, IdBtnLuz2on, IdBtnLuz4ON, IdBtnLuzALLON, IdBtnDesconectar;
    Spinner IdDisEncontrados; //se ingresa el SPINNER
    TextView textViewDistance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categorias, container, false);
        textViewDistance = view.findViewById(R.id.textViewDistance);
        IdBtnBuscar = view.findViewById(R.id.IdBtnBuscar);
        IdBtnConectar = view.findViewById(R.id.IdBtnConectar);
        IdBtnDesconectar = view.findViewById(R.id.IdBtnDesconectar);
        IdDisEncontrados = view.findViewById(R.id.IdDisEncontrados); ///SE LE COLOCA SU ID

        deviceAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mNameDevices);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        IdDisEncontrados.setAdapter(deviceAdapter);
        if (btSocket != null && btSocket.isConnected()) {
            IdDisEncontrados.setVisibility(View.VISIBLE);
        } else {
            IdDisEncontrados.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast("Bluetooth no disponible en este dispositivo.");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            someActivityResultLauncher.launch(enableBtIntent);
        }

        requestBluetoothConnectPermission();
        requestLocationPermission();

        IdBtnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DispositivosVinculados();
                IdDisEncontrados.setVisibility(View.VISIBLE);
            }
        });

        IdBtnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConectarDispBT();
            }
        });

        IdBtnDesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btSocket != null) {
                    try {
                        btSocket.close();
                        showToast("Dispositivo desconectado");
                        IdDisEncontrados.setVisibility(View.GONE);
                    } catch (IOException e) {
                        showToast("Error al cerrar la conexión Bluetooth");
                    }
                }
            }
        });

        ConectarDispBT();
    }

    private void desconectarBluetooth() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                showToast("Error al cerrar la conexión Bluetooth.");
            }
        }
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == categorias.REQUEST_ENABLE_BT) {
                        Log.d(TAG, "ACTIVIDAD REGISTRADA");
                    }
                }
            });

    public void DispositivosVinculados() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            showToast("Bluetooth no disponible en este dispositivo.");
            return;
        }

        if (!mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            someActivityResultLauncher.launch(enableBtIntent);
        }

        IdDisEncontrados.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                DispositivoSeleccionado = getBluetoothDeviceByName(mNameDevices.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                DispositivoSeleccionado = null;
            }
        });

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mNameDevices.add(device.getName());
            }
            deviceAdapter.notifyDataSetChanged();
        } else {
            showToast("No hay dispositivos Bluetooth emparejados.");
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
    }

    private void requestBluetoothConnectPermission() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                showToast("Bluetooth encendido correctamente.");
            } else {
                showToast("El usuario canceló la activación del Bluetooth.");
            }
        }
    }

    private BluetoothDevice getBluetoothDeviceByName(String name) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " ----->>>>> ActivityCompat.checkSelfPermission");
        }
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(name)) {
                return device;
            }
        }
        return null;
    }

    private void ConectarDispBT() {
        if (DispositivoSeleccionado == null) {
            showToast("Selecciona un dispositivo Bluetooth, o encianda su Bluetooth");
            return;
        }

        try {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            btSocket = DispositivoSeleccionado.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
            btSocket.connect();
            MyConexionBT = new ConnectedThread(btSocket);
            MyConexionBT.start();

            showToast("Conexión exitosa.");


        } catch (IOException e) {
            showToast("Error al conectar con el dispositivo.");
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                showToast("Error al crear el flujo de entrada de datos.");
            }

            mmInStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String receivedData = new String(buffer, 0, bytes);

                    if (receivedData.startsWith("D") && receivedData.endsWith("E")) {
                        String distanceString = receivedData.substring(1, receivedData.length() - 1);
                        double distance = Double.parseDouble(distanceString);
                        updateDistanceTextView(distance);
                    }
                } catch (IOException e) {
                    showToast("Error al leer del flujo de entrada de datos Bluetooth.");
                    break;
                }
            }
        }

        private void updateDistanceTextView(final double distance) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewDistance.setText("Distancia: " + distance + " cm");
                }
            });
        }
    }

    private void showToast(final String message) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
