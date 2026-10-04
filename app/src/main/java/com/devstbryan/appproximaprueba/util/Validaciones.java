package com.devstbryan.appproximaprueba.util;

import java.util.Locale;

/**
 * Reglas de validación de los formularios.
 * Es Java puro (no usa clases de Android), por eso se puede probar con pruebas unitarias JUnit.
 */
public final class Validaciones {

    /** Largo de un teléfono móvil chileno sin el código de país (ej: 912345678). */
    public static final int LARGO_TELEFONO = 9;

    private Validaciones() {
        // Clase de utilidades: no se instancia
    }

    /** El nombre es válido si tiene al menos un carácter que no sea espacio. */
    public static boolean esNombreValido(String nombre) {
        return nombre != null && !nombre.trim().isEmpty();
    }

    /** El teléfono es válido si tiene exactamente 9 dígitos (sin espacios, guiones ni "+"). */
    public static boolean esTelefonoValido(String telefono) {
        return telefono != null && telefono.matches("\\d{" + LARGO_TELEFONO + "}");
    }

    /** Devuelve la primera letra del nombre en mayúscula, o "?" si no hay nombre. */
    public static String inicial(String nombre) {
        if (!esNombreValido(nombre)) return "?";
        String limpio = nombre.trim();
        int primera = limpio.codePointAt(0);
        return new String(Character.toChars(primera)).toUpperCase(Locale.getDefault());
    }
}
