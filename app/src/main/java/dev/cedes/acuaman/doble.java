package dev.cedes.acuaman;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class doble extends AppCompatActivity {

    TextInputEditText email, contra, user, confi;

    Button sendData3;
    ImageView salir;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doble);
        user = findViewById(R.id.txt_namee);
        confi = findViewById(R.id.txt_name3);

        email = findViewById(R.id.txt_name);
        contra = findViewById(R.id.txt_name2);
        sendData3 = findViewById(R.id.btn_send_dataaa);
        salir = findViewById(R.id.salir);


        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(50);

                Intent i = new Intent(doble.this, MainActivity.class);
                startActivity(i);
                finish();

            }
        });

        sendData3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(50);

                Intent i = new Intent(doble.this, inter.class);
                startActivity(i);
                finish();

            }
        });

        sendData3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo, password, usuario, password1;
                correo = String.valueOf(email.getText());
                password = String.valueOf(contra.getText());
                usuario = String.valueOf(user.getText());
                password1 = String.valueOf(confi.getText());

                if (TextUtils.isEmpty(correo)){
                    Toast.makeText(doble.this,"Enter Correo", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(doble.this, "ENTER PASSWORD", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password1)){
                    Toast.makeText(doble.this, "ENTER PASSWORD Confirt", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(usuario)){
                    Toast.makeText(doble.this, "ENTER Usuario", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(password1)) {
                    Toast.makeText(doble.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verificar si el correo ya está registrado
                firestore.collection("usuarios").document(correo).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // El correo ya está asociado a una cuenta
                                Toast.makeText(doble.this, "El correo ya está asociado a una cuenta", Toast.LENGTH_SHORT).show();
                            } else {
                                // El correo no está asociado a ninguna cuenta, se puede registrar
                                firebaseAuth.createUserWithEmailAndPassword(correo ,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(doble.this, "registrado bien", Toast.LENGTH_SHORT).show();
                                            guardarDatosUsuarioFirestore(usuario, correo);
                                            Intent intent = new Intent(doble.this,inter.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                        else {
                                            Toast.makeText(doble.this, " autentificacion fail", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            // Error al verificar el correo en Firestore
                            Toast.makeText(doble.this, "Error al verificar el correo en Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void guardarDatosUsuarioFirestore(String usuario, String correo) {
        // Crear un mapa con los datos del usuario
        Map<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("usuario", usuario);
        usuarioMap.put("correo", correo);

        // Agregar los datos del usuario a Firestore
        firestore.collection("usuarios").document(correo)
                .set(usuarioMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(doble.this, "Datos de usuario guardados en Firestore", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(doble.this, "Error al guardar datos de usuario en Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
