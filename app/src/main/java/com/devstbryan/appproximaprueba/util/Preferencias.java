package com.devstbryan.appproximaprueba.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Guarda las preferencias del usuario con SharedPreferences (almacenamiento clave-valor).
 * Los datos se mantienen aunque se cierre la app.
 */
public final class Preferencias {

    private static final String ARCHIVO = "preferencias";
    private static final String CLAVE_TEMA = "tema";

    private Preferencias() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    /** Modo de tema guardado (por defecto: seguir el tema del sistema). */
    public static int leerTema(Context context) {
        return prefs(context).getInt(CLAVE_TEMA, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    /** Guarda el modo de tema. apply() escribe en segundo plano, sin bloquear la interfaz. */
    public static void guardarTema(Context context, int modo) {
        prefs(context).edit().putInt(CLAVE_TEMA, modo).apply();
    }

    /** Aplica a toda la app el tema guardado. */
    public static void aplicarTema(Context context) {
        AppCompatDelegate.setDefaultNightMode(leerTema(context));
    }
}
