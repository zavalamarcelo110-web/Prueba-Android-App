package com.devstbryan.appproximaprueba;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class Panel extends AppCompatActivity {

    // Componentes
    EditText etNombre, etTelefono;
    TextView tvUbicacion;

    // Linterna
    CameraManager cameraManager;
    String idCamara;
    boolean linternaEncendida = false;

    // Ubicación
    double latitud = 0, longitud = 0;
    boolean ubicacionObtenida = false;

    // Códigos de permisos
    final int PERMISO_UBICACION = 100;
    final int PERMISO_CAMARA = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_panel);

        // Conexión con el XML
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        tvUbicacion = findViewById(R.id.tvUbicacion);

        // ===== INTENTS EXPLÍCITOS =====

        // 1. Segunda Ventana enviando el nombre (putExtra)
        boton(R.id.btnSegundaVentana).setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty()) { etNombre.setError(getString(R.string.error_nombre)); return; }
            Intent segunda = new Intent(Panel.this, Segunda_Vista.class);
            segunda.putExtra("nombre", nombre);
            startActivity(segunda);
        });

        // 2. Ayuda
        boton(R.id.btnAyuda).setOnClickListener(v -> startActivity(new Intent(this, Ayuda.class)));

        // 3. Configuración
        boton(R.id.btnConfig).setOnClickListener(v -> startActivity(new Intent(this, Config.class)));

        // ===== FUNCIONES =====

        // Preparación de la linterna
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String[] camaras = cameraManager.getCameraIdList();
            if (camaras.length > 0) idCamara = camaras[0];
        } catch (CameraAccessException e) {
            mensaje(R.string.error_camara);
        }

        // Linterna: valida permiso y cámara antes de encender
        boton(R.id.btnLinterna).setOnClickListener(v -> {
            if (!tienePermiso(Manifest.permission.CAMERA, PERMISO_CAMARA)) return;
            if (idCamara == null) { mensaje(R.string.error_camara); return; }
            try {
                linternaEncendida = !linternaEncendida;
                cameraManager.setTorchMode(idCamara, linternaEncendida);
                mensaje(linternaEncendida ? R.string.linterna_on : R.string.linterna_off);
            } catch (CameraAccessException e) {
                mensaje(R.string.error_camara);
            }
        });

        // Ubicación: valida permiso y obtiene la posición actual
        boton(R.id.btnUbicacion).setOnClickListener(v -> {
            if (!tienePermiso(Manifest.permission.ACCESS_FINE_LOCATION, PERMISO_UBICACION)) return;
            tvUbicacion.setText(R.string.buscando);
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            lm.getCurrentLocation(LocationManager.GPS_PROVIDER, null, getMainExecutor(), ubicacion -> {
                if (ubicacion == null) { tvUbicacion.setText(R.string.ubicacion_vacia); return; }
                latitud = ubicacion.getLatitude();
                longitud = ubicacion.getLongitude();
                ubicacionObtenida = true;
                tvUbicacion.setText(getString(R.string.ubicacion_texto, latitud, longitud));
            });
        });

        // ===== INTENTS IMPLÍCITOS =====

        // 1. Ver ubicación en Google Maps
        boton(R.id.btnMap).setOnClickListener(v -> {
            if (!ubicacionObtenida) { mensaje(R.string.error_ubicacion); return; }
            abrir(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:" + latitud + "," + longitud + "?q=" + latitud + "," + longitud)));
        });

        // 2. Abrir página web
        boton(R.id.btnWeb).setOnClickListener(v ->
                abrir(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_web)))));

        // 3. Marcador telefónico (no requiere permiso CALL_PHONE)
        boton(R.id.btnLlamar).setOnClickListener(v -> {
            String telefono = etTelefono.getText().toString().trim();
            if (telefono.length() != 9) { etTelefono.setError(getString(R.string.error_telefono)); return; }
            abrir(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono)));
        });

        // 4. Enviar correo con asunto y cuerpo
        boton(R.id.btnCorreo).setOnClickListener(v -> {
            Intent correo = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + getString(R.string.correo_destino)));
            correo.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.correo_asunto));
            correo.putExtra(Intent.EXTRA_TEXT, getString(R.string.correo_cuerpo));
            abrir(correo);
        });

        // 5. Ajustes de Wi-Fi
        boton(R.id.btnWifi).setOnClickListener(v -> abrir(new Intent(Settings.ACTION_WIFI_SETTINGS)));
    }

    // Abre un intent validando que exista una app que lo reciba
    void abrir(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            mensaje(R.string.error_app);
        }
    }

    // Revisa el permiso y si no está lo solicita
    boolean tienePermiso(String permiso, int codigo) {
        if (ActivityCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED) return true;
        ActivityCompat.requestPermissions(this, new String[]{permiso}, codigo);
        return false;
    }

    // Resultado de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            mensaje(R.string.permiso_denegado);
        }
    }

    Button boton(int id) { return findViewById(id); }

    void mensaje(int texto) { Toast.makeText(this, texto, Toast.LENGTH_SHORT).show(); }
}
