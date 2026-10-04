package com.devstbryan.appproximaprueba;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.devstbryan.appproximaprueba.util.Animaciones;
import com.devstbryan.appproximaprueba.util.Pantalla;
import com.devstbryan.appproximaprueba.util.Validaciones;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Pantalla principal (LAUNCHER). Reúne:
 *  - 3 intents explícitos  → Segunda_Vista (con putExtra), Ayuda y Config.
 *  - 2 funciones           → linterna (CameraManager) y ubicación (LocationManager).
 *  - 5 intents implícitos  → mapa, web, marcador telefónico, correo y ajustes de Wi-Fi.
 */
public class Panel extends AppCompatActivity {

    // Claves para guardar el estado cuando la Activity se recrea (por ejemplo, al rotar)
    private static final String ESTADO_LATITUD = "latitud";
    private static final String ESTADO_LONGITUD = "longitud";
    private static final String ESTADO_UBICACION = "ubicacion_obtenida";

    private static final String PERMISO_CAMARA = Manifest.permission.CAMERA;
    private static final String PERMISO_UBICACION_FINA = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String PERMISO_UBICACION_APROX = Manifest.permission.ACCESS_COARSE_LOCATION;

    // Componentes de la interfaz
    private TextInputLayout tilNombre, tilTelefono;
    private EditText etNombre, etTelefono;
    private TextView tvUbicacion, tvEstadoLinterna, tvEstadoUbicacion;
    private ImageView ivLinterna;
    private MaterialCardView cardLinterna;
    private View vBrillo, vOnda1, vOnda2;

    // Linterna
    private CameraManager cameraManager;
    private String idCamara;
    private boolean linternaEncendida = false;

    // Ubicación
    private double latitud = 0, longitud = 0;
    private boolean ubicacionObtenida = false;
    private CancellationSignal cancelarUbicacion;

    // Animaciones que hay que detener al salir
    private Animator animacionPulso;
    private ObjectAnimator animacionBrillo;
    private ObjectAnimator animacionFlotar;

    // ===== PERMISOS: Activity Result API (reemplaza a onRequestPermissionsResult) =====

    private final ActivityResultLauncher<String> solicitarCamara = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) alternarLinterna();
                else permisoDenegado();
            });

    // Desde Android 12 se piden ubicación precisa y aproximada juntas; el usuario elige
    private final ActivityResultLauncher<String[]> solicitarUbicacion = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                if (Boolean.TRUE.equals(resultado.get(PERMISO_UBICACION_FINA))
                        || Boolean.TRUE.equals(resultado.get(PERMISO_UBICACION_APROX))) {
                    obtenerUbicacion();
                } else {
                    permisoDenegado();
                }
            });

    // Mantiene el botón sincronizado si la linterna se apaga desde fuera (por ejemplo, el panel rápido)
    private final CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
        @Override
        public void onTorchModeChanged(@NonNull String cameraId, boolean encendida) {
            if (cameraId.equals(idCamara) && encendida != linternaEncendida) {
                linternaEncendida = encendida;
                actualizarLinterna();
            }
        }
    };

    // ===================== CICLO DE VIDA =====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pantalla.activarEdgeToEdge(this, false);
        setContentView(R.layout.activity_panel);
        Pantalla.aplicarInsets(findViewById(R.id.headerContenido), findViewById(R.id.contenido));
        Pantalla.iconosSegunDesplazamiento(this, findViewById(R.id.scroll), findViewById(R.id.header));

        enlazarVistas();
        if (savedInstanceState != null) restaurarEstado(savedInstanceState);

        configurarIntentsExplicitos();
        configurarFunciones();
        configurarIntentsImplicitos();
        animarEntrada();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (idCamara != null) cameraManager.registerTorchCallback(torchCallback, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (idCamara != null) cameraManager.unregisterTorchCallback(torchCallback);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(ESTADO_LATITUD, latitud);
        outState.putDouble(ESTADO_LONGITUD, longitud);
        outState.putBoolean(ESTADO_UBICACION, ubicacionObtenida);
    }

    @Override
    protected void onDestroy() {
        if (cancelarUbicacion != null) cancelarUbicacion.cancel();
        if (animacionPulso != null) animacionPulso.cancel();
        if (animacionBrillo != null) animacionBrillo.cancel();
        if (animacionFlotar != null) animacionFlotar.cancel();
        super.onDestroy();
    }

    // Conexión con el XML
    private void enlazarVistas() {
        tilNombre = findViewById(R.id.tilNombre);
        tilTelefono = findViewById(R.id.tilTelefono);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        tvUbicacion = findViewById(R.id.tvUbicacion);
        tvEstadoLinterna = findViewById(R.id.tvEstadoLinterna);
        tvEstadoUbicacion = findViewById(R.id.tvEstadoUbicacion);
        ivLinterna = findViewById(R.id.ivLinterna);
        cardLinterna = findViewById(R.id.btnLinterna);
        vBrillo = findViewById(R.id.vBrillo);
        vOnda1 = findViewById(R.id.vOnda1);
        vOnda2 = findViewById(R.id.vOnda2);
    }

    private void restaurarEstado(Bundle estado) {
        latitud = estado.getDouble(ESTADO_LATITUD);
        longitud = estado.getDouble(ESTADO_LONGITUD);
        ubicacionObtenida = estado.getBoolean(ESTADO_UBICACION);
        if (ubicacionObtenida) {
            tvUbicacion.setText(getString(R.string.ubicacion_texto, latitud, longitud));
            tvEstadoUbicacion.setText(R.string.ubicacion_lista);
        }
    }

    private void animarEntrada() {
        animacionFlotar = Animaciones.flotar(findViewById(R.id.ivHero));
        Animaciones.contar(findViewById(R.id.tvStatExplicitos), 3);
        Animaciones.contar(findViewById(R.id.tvStatImplicitos), 5);
        Animaciones.contar(findViewById(R.id.tvStatFunciones), 2);
    }

    // ===================== INTENTS EXPLÍCITOS =====================

    private void configurarIntentsExplicitos() {
        // 1. Segunda Ventana enviando el nombre (putExtra)
        findViewById(R.id.btnSegundaVentana).setOnClickListener(v -> abrirSegundaVentana());
        etNombre.setOnEditorActionListener((v, accion, evento) -> {
            if (accion != EditorInfo.IME_ACTION_GO) return false;
            abrirSegundaVentana();
            return true;
        });
        limpiarErrorAlEscribir(etNombre, tilNombre);

        // 2. Ayuda
        findViewById(R.id.btnAyuda).setOnClickListener(v ->
                startActivity(new Intent(this, Ayuda.class)));

        // 3. Configuración
        findViewById(R.id.btnConfig).setOnClickListener(v ->
                startActivity(new Intent(this, Config.class)));
    }

    private void abrirSegundaVentana() {
        String nombre = etNombre.getText().toString().trim();
        if (!Validaciones.esNombreValido(nombre)) {
            mostrarError(tilNombre, R.string.error_nombre);
            return;
        }
        Intent segunda = new Intent(Panel.this, Segunda_Vista.class);
        segunda.putExtra(Segunda_Vista.EXTRA_NOMBRE, nombre);
        startActivity(segunda);
    }

    // ===================== FUNCIONES =====================

    private void configurarFunciones() {
        // Preparación de la linterna: se busca una cámara que tenga flash
        cameraManager = getSystemService(CameraManager.class);
        idCamara = buscarCamaraConFlash();

        // Linterna: valida permiso y cámara antes de encender
        cardLinterna.setOnClickListener(v -> {
            if (tienePermiso(PERMISO_CAMARA)) {
                alternarLinterna();
            } else {
                pedirPermiso(PERMISO_CAMARA, R.string.permiso_titulo_camara,
                        R.string.permiso_motivo_camara, () -> solicitarCamara.launch(PERMISO_CAMARA));
            }
        });

        // Ubicación: valida permiso y obtiene la posición actual
        findViewById(R.id.btnUbicacion).setOnClickListener(v -> {
            if (tienePermiso(PERMISO_UBICACION_FINA) || tienePermiso(PERMISO_UBICACION_APROX)) {
                obtenerUbicacion();
            } else {
                pedirPermiso(PERMISO_UBICACION_FINA, R.string.permiso_titulo_ubicacion,
                        R.string.permiso_motivo_ubicacion, () -> solicitarUbicacion.launch(
                                new String[]{PERMISO_UBICACION_FINA, PERMISO_UBICACION_APROX}));
            }
        });
    }

    @Nullable
    private String buscarCamaraConFlash() {
        try {
            for (String id : cameraManager.getCameraIdList()) {
                Boolean tieneFlash = cameraManager.getCameraCharacteristics(id)
                        .get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (Boolean.TRUE.equals(tieneFlash)) return id;
            }
        } catch (CameraAccessException e) {
            return null;
        }
        return null;
    }

    private void alternarLinterna() {
        if (idCamara == null) {
            mensaje(R.string.error_camara);
            return;
        }
        try {
            cameraManager.setTorchMode(idCamara, !linternaEncendida);
            linternaEncendida = !linternaEncendida;
            actualizarLinterna();
            Animaciones.rebotar(ivLinterna);
            cardLinterna.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            mensaje(linternaEncendida ? R.string.linterna_on : R.string.linterna_off);
        } catch (CameraAccessException e) {
            mensaje(R.string.error_camara_uso);
        }
    }

    // Cambia ícono, colores y halo de luz según el estado de la linterna
    private void actualizarLinterna() {
        boolean on = linternaEncendida;
        ivLinterna.setImageResource(on ? R.drawable.ic_linterna_on : R.drawable.ic_linterna_off);
        ivLinterna.setBackgroundResource(on ? R.drawable.bg_icono_linterna_on : R.drawable.bg_icono_apagado);
        ivLinterna.setImageTintList(ColorStateList.valueOf(
                getColor(on ? R.color.white : R.color.texto_secundario)));
        cardLinterna.setStrokeColor(getColor(on ? R.color.amarillo : R.color.borde));
        tvEstadoLinterna.setText(on ? R.string.linterna_estado_on : R.string.linterna_estado_off);

        if (animacionBrillo != null) animacionBrillo.cancel();
        if (on) {
            animacionBrillo = Animaciones.respirar(vBrillo);
        } else {
            vBrillo.animate().alpha(0f).setDuration(300).start();
        }
    }

    private void obtenerUbicacion() {
        LocationManager lm = getSystemService(LocationManager.class);
        if (!lm.isLocationEnabled()) {
            // Intent implícito extra: abre los ajustes de ubicación del sistema
            Snackbar.make(raiz(), R.string.error_gps, Snackbar.LENGTH_LONG)
                    .setAction(R.string.accion_activar, v ->
                            abrir(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .show();
            return;
        }
        boolean precisa = checkSelfPermission(PERMISO_UBICACION_FINA) == PackageManager.PERMISSION_GRANTED;
        boolean aproximada = checkSelfPermission(PERMISO_UBICACION_APROX) == PackageManager.PERMISSION_GRANTED;
        if (!precisa && !aproximada) return;

        // "fused" combina GPS, Wi-Fi y red celular (Android 12+); si no existe, se elige según el permiso
        String proveedor = lm.hasProvider(LocationManager.FUSED_PROVIDER) ? LocationManager.FUSED_PROVIDER
                : precisa ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;

        if (cancelarUbicacion != null) cancelarUbicacion.cancel();
        cancelarUbicacion = new CancellationSignal();
        tvUbicacion.setText(R.string.buscando);
        tvEstadoUbicacion.setText(R.string.buscando);
        if (animacionPulso != null) animacionPulso.cancel();
        animacionPulso = Animaciones.pulso(vOnda1, vOnda2);

        lm.getCurrentLocation(proveedor, cancelarUbicacion, getMainExecutor(), this::mostrarUbicacion);
    }

    private void mostrarUbicacion(@Nullable Location ubicacion) {
        Animaciones.detenerPulso(animacionPulso, vOnda1, vOnda2);
        if (ubicacion == null) {
            tvUbicacion.setText(R.string.ubicacion_vacia);
            tvEstadoUbicacion.setText(R.string.ubicacion_estado);
            return;
        }
        latitud = ubicacion.getLatitude();
        longitud = ubicacion.getLongitude();
        ubicacionObtenida = true;
        tvEstadoUbicacion.setText(R.string.ubicacion_lista);
        Animaciones.contarCoordenadas(tvUbicacion, getString(R.string.ubicacion_texto), latitud, longitud);
        tvUbicacion.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
    }

    // ===================== INTENTS IMPLÍCITOS =====================

    private void configurarIntentsImplicitos() {
        // 1. Ver ubicación en Google Maps (o cualquier app que entienda "geo:")
        findViewById(R.id.btnMap).setOnClickListener(v -> {
            if (!ubicacionObtenida) {
                mensaje(R.string.error_ubicacion);
                Animaciones.sacudir(findViewById(R.id.btnUbicacion));
                return;
            }
            abrir(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:" + latitud + "," + longitud + "?q=" + latitud + "," + longitud)));
        });

        // 2. Abrir página web
        findViewById(R.id.btnWeb).setOnClickListener(v ->
                abrir(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_web)))));

        // 3. Marcador telefónico (ACTION_DIAL no requiere el permiso CALL_PHONE)
        findViewById(R.id.btnLlamar).setOnClickListener(v -> llamar());
        etTelefono.setOnEditorActionListener((v, accion, evento) -> {
            if (accion != EditorInfo.IME_ACTION_DONE) return false;
            llamar();
            return true;
        });
        limpiarErrorAlEscribir(etTelefono, tilTelefono);

        // 4. Enviar correo con asunto y cuerpo
        findViewById(R.id.btnCorreo).setOnClickListener(v -> {
            Intent correo = new Intent(Intent.ACTION_SENDTO,
                    Uri.parse("mailto:" + getString(R.string.correo_destino)));
            correo.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.correo_asunto));
            correo.putExtra(Intent.EXTRA_TEXT, getString(R.string.correo_cuerpo));
            abrir(correo);
        });

        // 5. Ajustes de Wi-Fi
        findViewById(R.id.btnWifi).setOnClickListener(v ->
                abrir(new Intent(Settings.ACTION_WIFI_SETTINGS)));
    }

    private void llamar() {
        String telefono = etTelefono.getText().toString().trim();
        if (!Validaciones.esTelefonoValido(telefono)) {
            mostrarError(tilTelefono, R.string.error_telefono);
            return;
        }
        abrir(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono)));
    }

    // ===================== AYUDAS =====================

    // Abre un intent validando que exista una app que lo reciba
    private void abrir(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            mensaje(R.string.error_app);
        }
    }

    private boolean tienePermiso(String permiso) {
        return checkSelfPermission(permiso) == PackageManager.PERMISSION_GRANTED;
    }

    // Si el usuario ya rechazó el permiso una vez, primero se le explica para qué sirve
    private void pedirPermiso(String permiso, @StringRes int titulo, @StringRes int motivo, Runnable solicitar) {
        if (shouldShowRequestPermissionRationale(permiso)) {
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.ic_escudo)
                    .setTitle(titulo)
                    .setMessage(motivo)
                    .setPositiveButton(R.string.accion_continuar, (dialogo, boton) -> solicitar.run())
                    .setNegativeButton(R.string.accion_cancelar, null)
                    .show();
        } else {
            solicitar.run();
        }
    }

    // Permiso rechazado: se ofrece ir a los ajustes de la app (intent implícito)
    private void permisoDenegado() {
        Snackbar.make(raiz(), R.string.permiso_denegado, Snackbar.LENGTH_LONG)
                .setAction(R.string.accion_ajustes, v -> abrir(new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getPackageName(), null))))
                .show();
    }

    private void mostrarError(TextInputLayout campo, @StringRes int error) {
        campo.setError(getString(error));
        campo.requestFocus();
        Animaciones.sacudir(campo);
    }

    // Quita el mensaje de error apenas el usuario vuelve a escribir
    private static void limpiarErrorAlEscribir(EditText texto, TextInputLayout campo) {
        texto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                campo.setError(null);
            }
        });
    }

    private View raiz() {
        return findViewById(android.R.id.content);
    }

    // Mensaje corto con Snackbar (componente de Material Design, alternativa moderna al Toast)
    private void mensaje(@StringRes int texto) {
        Snackbar.make(raiz(), texto, Snackbar.LENGTH_SHORT).show();
    }
}
