package com.devstbryan.appproximaprueba;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Segunda_Vista extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_segunda_vista);

        // Recibe el nombre enviado desde el Panel (valida null)
        String extra = getIntent().getStringExtra("nombre");
        String nombre = extra == null ? "" : extra;
        TextView tvSaludo = findViewById(R.id.tvSaludo);
        tvSaludo.setText(R.string.cargando);

        // Thread: simula una carga en segundo plano y luego actualiza la pantalla
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) { }
            runOnUiThread(() -> tvSaludo.setText(getString(R.string.saludo, nombre)));
        }).start();

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
    }
}
