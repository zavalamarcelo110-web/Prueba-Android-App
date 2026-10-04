package com.devstbryan.appproximaprueba;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.devstbryan.appproximaprueba.util.Animaciones;
import com.devstbryan.appproximaprueba.util.Pantalla;
import com.devstbryan.appproximaprueba.util.Validaciones;
import com.devstbryan.appproximaprueba.widget.ConfettiView;

/**
 * Destino del intent explícito con datos: recibe el nombre con getStringExtra()
 * y usa un Thread para simular una carga en segundo plano antes de mostrar el saludo.
 */
public class Segunda_Vista extends AppCompatActivity {

    /** Clave del dato enviado. Es una constante pública para que el Panel use exactamente la misma. */
    public static final String EXTRA_NOMBRE = "nombre";

    private static final long TIEMPO_CARGA_MS = 1500;

    private View grupoCarga, grupoSaludo;
    private TextView tvAvatar, tvSaludo, tvDetalle, tvCodigo;
    private ConfettiView confeti;
    private Thread hiloCarga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pantalla.activarEdgeToEdge(this, true);
        setContentView(R.layout.activity_segunda_vista);
        View contenido = findViewById(R.id.contenidoSegunda);
        Pantalla.aplicarInsets(contenido, contenido);

        grupoCarga = findViewById(R.id.grupoCarga);
        grupoSaludo = findViewById(R.id.grupoSaludo);
        tvAvatar = findViewById(R.id.tvAvatar);
        tvSaludo = findViewById(R.id.tvSaludo);
        tvDetalle = findViewById(R.id.tvDetalle);
        tvCodigo = findViewById(R.id.tvCodigo);
        confeti = findViewById(R.id.confeti);

        // Recibe el nombre enviado desde el Panel (valida null)
        String extra = getIntent().getStringExtra(EXTRA_NOMBRE);
        String nombre = extra == null ? "" : extra;

        if (savedInstanceState != null) {
            // Si la pantalla se recrea (por ejemplo, al rotar) no se repite la carga
            mostrarSaludo(nombre, false);
        } else {
            iniciarCarga(nombre);
        }

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
        findViewById(R.id.btnAtras).setOnClickListener(v -> finish());
    }

    // Thread: simula una carga en segundo plano y luego actualiza la pantalla
    private void iniciarCarga(String nombre) {
        hiloCarga = new Thread(() -> {
            try {
                Thread.sleep(TIEMPO_CARGA_MS);
            } catch (InterruptedException e) {
                return; // La pantalla se cerró antes de terminar: no se hace nada más
            }
            // Solo el hilo principal (UI thread) puede modificar las vistas
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) mostrarSaludo(nombre, true);
            });
        }, "hilo-carga");
        hiloCarga.start();
    }

    private void mostrarSaludo(String nombre, boolean animar) {
        tvAvatar.setText(Validaciones.inicial(nombre));
        tvSaludo.setText(getString(R.string.saludo, nombre));
        tvCodigo.setText(getString(R.string.codigo_extra, nombre));
        grupoSaludo.setVisibility(View.VISIBLE);

        if (!animar) {
            grupoCarga.setVisibility(View.GONE);
            return;
        }
        Animaciones.desvanecer(grupoCarga);
        Animaciones.rebotar(tvAvatar);
        Animaciones.aparecer(tvSaludo, 120);
        Animaciones.aparecer(tvDetalle, 220);
        Animaciones.aparecer(tvCodigo, 320);
        confeti.lanzar();
        confeti.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
    }

    @Override
    protected void onDestroy() {
        // Detiene el hilo si el usuario sale antes de que termine (evita trabajo innecesario)
        if (hiloCarga != null) hiloCarga.interrupt();
        super.onDestroy();
    }
}
