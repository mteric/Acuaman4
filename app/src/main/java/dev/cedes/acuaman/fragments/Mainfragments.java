package dev.cedes.acuaman.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import dev.cedes.acuaman.R;

public class Mainfragments extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        ImageView imagenConfiguracion = view.findViewById(R.id.imagenn);
        imagenConfiguracion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reemplazar el fragmento actual con el fragmento categorias
                Fragment categoriasFragment = new categorias();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, categoriasFragment);
                fragmentTransaction.addToBackStack(null); // Opcional: agrega la transacción al historial de retroceso
                fragmentTransaction.commit();
            }
        });

        return view;
    }
}
