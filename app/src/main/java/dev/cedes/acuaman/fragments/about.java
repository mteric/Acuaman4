package dev.cedes.acuaman.fragments;

import static android.companion.CompanionDeviceManager.RESULT_OK;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import dev.cedes.acuaman.R;

public class about extends Fragment {

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice selectedDevice;
    private BluetoothSocket btSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private TextView textViewDistance;
    private EditText editTextMacAddress;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about, container, false);

        textViewDistance = view.findViewById(R.id.textViewDistance);
        editTextMacAddress = view.findViewById(R.id.editTextMacAddress);
        Button connectButton = view.findViewById(R.id.connectButton);
        Button openUrlButton = view.findViewById(R.id.openUrlButton);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Bluetooth no disponible en este dispositivo.", Toast.LENGTH_SHORT).show();
            return view;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String macAddress = editTextMacAddress.getText().toString();
                if (macAddress.isEmpty()) {
                    Toast.makeText(requireContext(), "Ingrese una dirección MAC", Toast.LENGTH_SHORT).show();
                } else {
                    selectedDevice = bluetoothAdapter.getRemoteDevice(macAddress);
                    if (selectedDevice == null) {
                        Toast.makeText(requireContext(), "Dispositivo no encontrado", Toast.LENGTH_SHORT).show();
                    } else {
                        connectToBluetooth();
                    }
                }
            }
        });

        openUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.youtube.com/watch?v=RrqhA7csols&list=RDRrqhA7csols&start_radio=1";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(requireContext(), "Bluetooth encendido correctamente.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "El usuario canceló la activación del Bluetooth.", Toast.LENGTH_SHORT).show();
            }
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

    private void connectToBluetooth() {
        new Thread(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                try {
                    btSocket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    btSocket.connect();
                    inputStream = btSocket.getInputStream();

                    // Start reading data from Bluetooth
                    startReadingDataFromBluetooth();
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast("Error al conectar al dispositivo Bluetooth.");
                }
            }
        }).start();
    }


    private void startReadingDataFromBluetooth() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int bytes;

                while (true) {
                    try {
                        bytes = inputStream.read(buffer);
                        String distanceStr = new String(buffer, 0, bytes);
                        int distance = Integer.parseInt(distanceStr.trim());
                        updateDistanceOnUI(distance);
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }).start();
    }

    private void updateDistanceOnUI(final int distance) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewDistance.setText("Distancia: " + distance + " cm");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (btSocket != null) {
                btSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
