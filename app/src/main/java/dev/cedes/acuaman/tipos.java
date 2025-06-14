package dev.cedes.acuaman;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class tipos extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;

    ImageView roto450, roto600, roto750, roto1100, roto2500;
    Button btnGuardar;

    public static final String PREFS_NAME = "RotoplasPrefs";
    public static final String KEY_TINACO_CAPACITY = "tinacoCapacity";
    private int selectedCapacity = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tipos);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        roto450 = findViewById(R.id.roto450);
        roto600 = findViewById(R.id.roto600);
        roto750 = findViewById(R.id.roto750);
        roto1100 = findViewById(R.id.roto1100);
        roto2500 = findViewById(R.id.roto2500);

        btnGuardar = findViewById(R.id.btn_guardar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationview);
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        roto450.setOnClickListener(v -> selectTinaco(450));
        roto600.setOnClickListener(v -> selectTinaco(600));
        roto750.setOnClickListener(v -> selectTinaco(750));
        roto1100.setOnClickListener(v -> selectTinaco(1100));
        roto2500.setOnClickListener(v -> selectTinaco(2500));

        btnGuardar.setOnClickListener(v -> saveTinacoSelection());

        loadSelectedTinaco(); // Cargar la selección previa al iniciar
    }

    private void selectTinaco(int capacity) {
        selectedCapacity = capacity;
        resetImageBorders(); // Quita el borde de las imágenes anteriores

        // Añade el borde a la imagen seleccionada
        switch (capacity) {
            case 450:
                roto450.setBackgroundResource(R.drawable.border_selected);
                break;
            case 600:
                roto600.setBackgroundResource(R.drawable.border_selected);
                break;
            case 750:
                roto750.setBackgroundResource(R.drawable.border_selected);
                break;
            case 1100:
                roto1100.setBackgroundResource(R.drawable.border_selected);
                break;
            case 2500:
                roto2500.setBackgroundResource(R.drawable.border_selected);
                break;
        }
        Toast.makeText(this, "Tinaco " + capacity + "L seleccionado", Toast.LENGTH_SHORT).show();
    }

    private void resetImageBorders() {
        roto450.setBackgroundResource(0); // 0 o 'null' quita el fondo/borde
        roto600.setBackgroundResource(0);
        roto750.setBackgroundResource(0);
        roto1100.setBackgroundResource(0);
        roto2500.setBackgroundResource(0);
    }

    private void saveTinacoSelection() {
        if (selectedCapacity == 0) {
            Toast.makeText(this, "Por favor, selecciona un tipo de tinaco primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(KEY_TINACO_CAPACITY, selectedCapacity);
        editor.apply();

        Toast.makeText(this, "Capacidad de tinaco " + selectedCapacity + "L guardada.", Toast.LENGTH_SHORT).show();

        // Redirige a inter.class después de guardar
        Intent intent = new Intent(tipos.this, inter.class);
        startActivity(intent);
        // No llamas finish() aquí, para que el usuario pueda volver a esta pantalla
        // si usa el botón de atrás de Android, en caso de querer cambiar el tinaco.
    }

    private void loadSelectedTinaco() {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int storedCapacity = sharedPref.getInt(KEY_TINACO_CAPACITY, 0);
        if (storedCapacity != 0) {
            selectedCapacity = storedCapacity;
            selectTinaco(selectedCapacity); // Vuelve a seleccionar visualmente el tinaco
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home) {
            Toast.makeText(this, "Regresando al inicio...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, inter.class);
            startActivity(intent);
            // No llamas finish() aquí por la misma razón que en saveTinacoSelection
            return true;
        }
        // Cierra el cajón de navegación si se selecciona cualquier otra opción (o si no se maneja aquí)
        drawerLayout.closeDrawers();
        return false;
    }
}