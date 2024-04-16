package dev.cedes.acuaman.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dev.cedes.acuaman.R;

public class about extends Fragment {
    private static final String SHARED_PREF_NAME = "mypref";
    SharedPreferences sharedPreferences;
   TextView enlace;
    Button cerrarSesion, siguiente;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about, container, false);

        enlace = view.findViewById(R.id.textViewEnlace);
        sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        enlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir el enlace en un navegador web
                String url = "https://www.youtube.com/watch?v=RrqhA7csols&list=RDRrqhA7csols&start_radio=1";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });




        return view;
    }
}
