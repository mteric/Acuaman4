package dev.cedes.acuaman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    TextInputEditText correo, contra;
    Button sendData, sendData2;
    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DrawerLayout drawableLayout;

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
            startActivity(new Intent(MainActivity.this, inter.class));
            finish();
        }

        sendData2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, doble.class));
                finish();
            }
        });

        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(50);

                String email = correo.getText().toString().trim();
                String password = contra.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Enter Usuario and Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    saveCredentials(email, password);
                                    startActivity(new Intent(MainActivity.this, inter.class));
                                    finish();
                                } else {
                                    Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }
}
