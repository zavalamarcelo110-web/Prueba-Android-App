package com.devstbryan.appproximaprueba;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.devstbryan.appproximaprueba.util.Animaciones;
import com.devstbryan.appproximaprueba.util.Pantalla;

/** Pantalla de ayuda (destino de un intent explícito). */
public class Ayuda extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pantalla.activarEdgeToEdge(this, false);
        setContentView(R.layout.activity_ayuda);
        Pantalla.aplicarInsets(findViewById(R.id.headerContenido), findViewById(R.id.contenido));

        Animaciones.flotar(findViewById(R.id.ivIconoHeader));

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());
    }
}
