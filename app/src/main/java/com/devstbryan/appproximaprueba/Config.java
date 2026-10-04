package com.devstbryan.appproximaprueba;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.devstbryan.appproximaprueba.util.Animaciones;
import com.devstbryan.appproximaprueba.util.Pantalla;
import com.devstbryan.appproximaprueba.util.Preferencias;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;

/**
 * Configuración (destino de un intent explícito):
 *  - Tema claro / oscuro / del sistema, guardado con SharedPreferences.
 *  - Estado de los permisos y acceso a los ajustes de la app (intent implícito).
 *  - Información de la app leída desde BuildConfig.
 */
public class Config extends AppCompatActivity {

    private TextView tvPermisoCamara, tvPermisoUbicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pantalla.activarEdgeToEdge(this, false);
        setContentView(R.layout.activity_config);
        Pantalla.aplicarInsets(findViewById(R.id.headerContenido), findViewById(R.id.contenido));

        Animaciones.flotar(findViewById(R.id.ivIconoHeader));
        tvPermisoCamara = findViewById(R.id.tvPermisoCamara);
        tvPermisoUbicacion = findViewById(R.id.tvPermisoUbicacion);

        configurarTema();
        mostrarInformacion();

        findViewById(R.id.btnAjustesApp).setOnClickListener(v -> abrirAjustesApp());
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se actualiza al volver, porque el usuario pudo cambiar los permisos en los ajustes
        mostrarPermiso(tvPermisoCamara, tiene(Manifest.permission.CAMERA));
        mostrarPermiso(tvPermisoUbicacion, tiene(Manifest.permission.ACCESS_FINE_LOCATION)
                || tiene(Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    private void configurarTema() {
        MaterialButtonToggleGroup grupo = findViewById(R.id.grupoTema);
        grupo.check(botonDeModo(Preferencias.leerTema(this)));

        grupo.addOnButtonCheckedListener((g, idBoton, marcado) -> {
            if (!marcado) return;
            int modo = modoDeBoton(idBoton);
            if (modo == Preferencias.leerTema(this)) return;
            Preferencias.guardarTema(this, modo);
            // Cambia el tema de toda la app; Android recrea las pantallas con los nuevos colores
            AppCompatDelegate.setDefaultNightMode(modo);
        });
    }

    private static int botonDeModo(int modo) {
        if (modo == AppCompatDelegate.MODE_NIGHT_NO) return R.id.btnTemaClaro;
        if (modo == AppCompatDelegate.MODE_NIGHT_YES) return R.id.btnTemaOscuro;
        return R.id.btnTemaSistema;
    }

    private static int modoDeBoton(int idBoton) {
        if (idBoton == R.id.btnTemaClaro) return AppCompatDelegate.MODE_NIGHT_NO;
        if (idBoton == R.id.btnTemaOscuro) return AppCompatDelegate.MODE_NIGHT_YES;
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    private void mostrarInformacion() {
        TextView tvVersion = findViewById(R.id.tvVersion);
        TextView tvPaquete = findViewById(R.id.tvPaquete);
        tvVersion.setText(BuildConfig.VERSION_NAME);
        tvPaquete.setText(BuildConfig.APPLICATION_ID);
    }

    private boolean tiene(String permiso) {
        return checkSelfPermission(permiso) == PackageManager.PERMISSION_GRANTED;
    }

    private void mostrarPermiso(TextView chip, boolean concedido) {
        chip.setText(concedido ? R.string.permiso_concedido : R.string.permiso_pendiente);
        chip.setBackgroundTintList(ColorStateList.valueOf(
                getColor(concedido ? R.color.esmeralda : R.color.naranja)));
    }

    // Intent implícito: abre la pantalla de información de esta app en los Ajustes del sistema
    private void abrirAjustesApp() {
        Intent ajustes = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        try {
            startActivity(ajustes);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(findViewById(android.R.id.content), R.string.error_app, Snackbar.LENGTH_SHORT).show();
        }
    }
}
