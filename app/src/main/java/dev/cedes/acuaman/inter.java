package dev.cedes.acuaman;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import dev.cedes.acuaman.fragments.Mainfragments;
import dev.cedes.acuaman.fragments.about;
import dev.cedes.acuaman.fragments.categorias;
import dev.cedes.acuaman.fragments.cerrarsesion;


public class inter extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    //variables
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "mypref";
    Button cerrarsesion;
    //variable para cargar fragment principal


    private DrawerLayout drawableLayout;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inter);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationview);
        //establecer evento onclick
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();
        //cargar fragment principal

        fragmentManager=getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container, new Mainfragments());
        fragmentTransaction.commit();

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME,MODE_PRIVATE);

        //establece evento onclick al navigation










    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        if (menuItem.getItemId() == R.id.home){

            fragmentManager=getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, new Mainfragments());
            fragmentTransaction.commit();

        }

        if (menuItem.getItemId() == R.id.personas){
            fragmentManager=getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, new cerrarsesion());
            fragmentTransaction.commit();

        }
        if (menuItem.getItemId() == R.id.categotrias){
            fragmentManager=getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, new categorias());
            fragmentTransaction.commit();

        }
        if (menuItem.getItemId() == R.id.about){
            fragmentManager=getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, new about());
            fragmentTransaction.commit();

        }


        return false;
    }


}