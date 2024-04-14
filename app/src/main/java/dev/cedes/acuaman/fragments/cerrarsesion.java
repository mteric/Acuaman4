package dev.cedes.acuaman.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dev.cedes.acuaman.MainActivity;
import dev.cedes.acuaman.R;
import dev.cedes.acuaman.inter;

public class cerrarsesion extends Fragment {
    private static final String SHARED_PREF_NAME = "mypref";
    SharedPreferences sharedPreferences;
    Button cerrarSesion, siguiente;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.personas_fragment, container, false);
        cerrarSesion = view.findViewById(R.id.button6);
        siguiente = view.findViewById(R.id.button7);
        sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        cerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear().apply(); // apply() es preferible a commit()

                Toast.makeText(requireContext(), "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(requireContext(), MainActivity.class);
                startActivity(intent);

                requireActivity().finish();
            }
        });

        siguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), inter.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
