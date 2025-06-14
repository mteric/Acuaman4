package dev.cedes.acuaman;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    TextInputEditText correo, contra;
    Button sendData, sendData2;
    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 3;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        correo = findViewById(R.id.txt_name5);
        contra = findViewById(R.id.txt_name6);
        sendData = findViewById(R.id.btn_send_data);
        sendData2 = findViewById(R.id.btn_send_dataa);
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        String email = sharedPreferences.getString(KEY_EMAIL, null);
        if (email != null) {
            startActivity(new Intent(MainActivity.this, MainActivity2.class));
            finish();
        }

        sendData2.setOnClickListener(view -> {
            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(50);
            startActivity(new Intent(MainActivity.this, doble.class));
            finish();
        });

        sendData.setOnClickListener(view -> {
            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(50);

            String email1 = correo.getText().toString().trim();
            String password = contra.getText().toString().trim();

            if (TextUtils.isEmpty(email1) || TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, "Enter Usuario and Password", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email1, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "EXITOSO", Toast.LENGTH_SHORT).show();
                            saveCredentials(email1, password);
                            startActivity(new Intent(MainActivity.this, MainActivity2.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "CORREO O CONTRASEÑA INCORRECTO", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Solicitar permisos de Bluetooth después de un breve retraso
        new Handler(Looper.getMainLooper()).postDelayed(this::requestBluetoothPermissions, 1000);
    }

    private void showToast(final String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void requestBluetoothPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Permiso de Bluetooth concedido.");
            } else {
                showToast("Permiso de Bluetooth necesario para la aplicación. Por favor, concédelo.");
                // Volver a solicitar el permiso
                requestBluetoothPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                showToast("Bluetooth encendido correctamente.");
            } else {
                showToast("El usuario canceló la activación del Bluetooth.");
            }
        }
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }
}
