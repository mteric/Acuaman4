package dev.cedes.acuaman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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

public class doble extends AppCompatActivity {


    TextInputEditText email, contra, user, confi;

    Button sendData3, salir;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

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


                firebaseAuth.createUserWithEmailAndPassword(correo ,password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(doble.this, "registrado bien", Toast.LENGTH_SHORT).show();
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
        });
    }
}