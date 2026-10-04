package com.devstbryan.appproximaprueba.util;

import android.graphics.Color;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Ayudas para dibujar la app "de borde a borde" (edge-to-edge):
 * el contenido se dibuja detrás de la barra de estado y de navegación,
 * y luego se agregan márgenes internos (insets) para que nada quede tapado.
 */
public final class Pantalla {

    private Pantalla() {
    }

    /**
     * Activa edge-to-edge. Todas las pantallas tienen un encabezado oscuro arriba,
     * así que los íconos de la barra de estado siempre van en blanco.
     */
    public static void activarEdgeToEdge(ComponentActivity activity, boolean fondoOscuro) {
        SystemBarStyle barraNavegacion = fondoOscuro
                ? SystemBarStyle.dark(Color.TRANSPARENT)
                : SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT);
        EdgeToEdge.enable(activity, SystemBarStyle.dark(Color.TRANSPARENT), barraNavegacion);
    }

    /**
     * Suma el alto de la barra de estado al padding superior de {@code arriba}
     * y el de la barra de navegación (o del teclado) al padding inferior de {@code abajo}.
     * Pueden ser la misma vista.
     */
    public static void aplicarInsets(View arriba, View abajo) {
        if (arriba == abajo) {
            aplicar(arriba, true, true);
        } else {
            aplicar(arriba, true, false);
            aplicar(abajo, false, true);
        }
    }

    // Cada vista tiene un solo listener: si se registra otro, reemplaza al anterior
    private static void aplicar(View vista, boolean superior, boolean inferior) {
        final int paddingArriba = vista.getPaddingTop();
        final int paddingAbajo = vista.getPaddingBottom();
        final int tipos = WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();

        ViewCompat.setOnApplyWindowInsetsListener(vista, (v, insets) -> {
            Insets barras = insets.getInsets(tipos);
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            int top = superior ? paddingArriba + barras.top : v.getPaddingTop();
            int bottom = inferior ? paddingAbajo + Math.max(barras.bottom, teclado.bottom) : v.getPaddingBottom();
            v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), bottom);
            return insets;
        });
    }
}
