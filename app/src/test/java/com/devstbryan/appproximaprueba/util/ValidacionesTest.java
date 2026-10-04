package com.devstbryan.appproximaprueba.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Pruebas unitarias locales (se ejecutan en la JVM del computador, sin emulador).
 * Ejecutar con: ./gradlew test
 */
public class ValidacionesTest {

    @Test
    public void nombre_conTexto_esValido() {
        assertTrue(Validaciones.esNombreValido("Bryan"));
        assertTrue(Validaciones.esNombreValido("  Ana  "));
    }

    @Test
    public void nombre_vacioNuloOSoloEspacios_noEsValido() {
        assertFalse(Validaciones.esNombreValido(null));
        assertFalse(Validaciones.esNombreValido(""));
        assertFalse(Validaciones.esNombreValido("    "));
    }

    @Test
    public void telefono_conNueveDigitos_esValido() {
        assertTrue(Validaciones.esTelefonoValido("912345678"));
    }

    @Test
    public void telefono_conLargoIncorrecto_noEsValido() {
        assertFalse(Validaciones.esTelefonoValido("91234567"));
        assertFalse(Validaciones.esTelefonoValido("9123456789"));
        assertFalse(Validaciones.esTelefonoValido(""));
        assertFalse(Validaciones.esTelefonoValido(null));
    }

    @Test
    public void telefono_conSimbolosOLetras_noEsValido() {
        assertFalse(Validaciones.esTelefonoValido("+56912345"));
        assertFalse(Validaciones.esTelefonoValido("9 1234567"));
        assertFalse(Validaciones.esTelefonoValido("91234567a"));
    }

    @Test
    public void inicial_devuelvePrimeraLetraEnMayuscula() {
        assertEquals("B", Validaciones.inicial("bryan"));
        assertEquals("Á", Validaciones.inicial("  álvaro"));
    }

    @Test
    public void inicial_sinNombre_devuelveSignoDePregunta() {
        assertEquals("?", Validaciones.inicial(""));
        assertEquals("?", Validaciones.inicial(null));
    }
}
