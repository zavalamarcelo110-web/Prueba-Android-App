package com.devstbryan.appproximaprueba;

import android.app.Application;

import com.devstbryan.appproximaprueba.util.Preferencias;

/**
 * Clase Application: se crea una sola vez, antes que cualquier Activity.
 * Aquí se aplica el tema guardado para que todas las pantallas lo usen desde el inicio.
 */
public class AppProximaPrueba extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Preferencias.aplicarTema(this);
    }
}
