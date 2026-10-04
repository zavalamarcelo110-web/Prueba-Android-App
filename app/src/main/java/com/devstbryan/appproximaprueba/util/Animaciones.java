package com.devstbryan.appproximaprueba.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.devstbryan.appproximaprueba.R;

import java.util.Locale;

/**
 * Animaciones reutilizables de la app.
 * Se usan dos sistemas de Android:
 *  - View Animation (res/anim): animaciones simples definidas en XML (ej: sacudir).
 *  - Property Animation (ObjectAnimator / ValueAnimator): cambian propiedades reales de la vista.
 * Si el usuario desactiva las animaciones del sistema, ValueAnimator las salta automáticamente.
 */
public final class Animaciones {

    private Animaciones() {
    }

    /** Sacude la vista de lado a lado y hace vibrar el teléfono: indica un error. */
    public static void sacudir(View vista) {
        vista.startAnimation(AnimationUtils.loadAnimation(vista.getContext(), R.anim.sacudir));
        vista.performHapticFeedback(HapticFeedbackConstants.REJECT);
    }

    /**
     * Las animaciones infinitas guardan una referencia a la vista: si no se detienen,
     * la Activity no se libera de memoria (memory leak). Aquí se cancelan solas
     * cuando la vista se quita de la pantalla.
     */
    private static <T extends Animator> T cancelarAlSalir(View vista, T animador) {
        vista.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                animador.cancel();
                v.removeOnAttachStateChangeListener(this);
            }
        });
        return animador;
    }

    /** Movimiento suave de arriba hacia abajo, infinito (efecto "flotando"). */
    public static ObjectAnimator flotar(View vista) {
        float distancia = vista.getResources().getDimension(R.dimen.flotar);
        ObjectAnimator animador = ObjectAnimator.ofFloat(vista, View.TRANSLATION_Y, 0f, -distancia);
        animador.setDuration(2400);
        animador.setRepeatCount(ValueAnimator.INFINITE);
        animador.setRepeatMode(ValueAnimator.REVERSE);
        animador.setInterpolator(new AccelerateDecelerateInterpolator());
        animador.start();
        return cancelarAlSalir(vista, animador);
    }

    /** Hace "saltar" una vista (crece y vuelve a su tamaño con rebote). */
    public static void rebotar(View vista) {
        PropertyValuesHolder x = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.7f, 1f);
        PropertyValuesHolder y = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.7f, 1f);
        ObjectAnimator animador = ObjectAnimator.ofPropertyValuesHolder(vista, x, y);
        animador.setDuration(450);
        animador.setInterpolator(new OvershootInterpolator(3f));
        animador.start();
    }

    /** Aparece desde abajo con desvanecimiento, después de {@code retraso} milisegundos. */
    public static void aparecer(View vista, long retraso) {
        vista.setAlpha(0f);
        vista.setTranslationY(vista.getResources().getDimension(R.dimen.desplazamiento_entrada));
        vista.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(retraso)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();
    }

    /** Desvanece y oculta la vista. */
    public static void desvanecer(View vista) {
        vista.animate()
                .alpha(0f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(250)
                .withEndAction(() -> vista.setVisibility(View.GONE))
                .start();
    }

    /** Cuenta desde 0 hasta {@code valor} (efecto contador). */
    public static void contar(TextView texto, int valor) {
        ValueAnimator animador = ValueAnimator.ofInt(0, valor);
        animador.setDuration(900);
        animador.setStartDelay(300);
        animador.setInterpolator(new DecelerateInterpolator());
        animador.addUpdateListener(a ->
                texto.setText(String.format(Locale.getDefault(), "%d", (int) a.getAnimatedValue())));
        animador.start();
    }

    /** Las coordenadas "giran" como un contador hasta llegar al valor real. */
    public static void contarCoordenadas(TextView texto, String formato, double latitud, double longitud) {
        ValueAnimator animador = ValueAnimator.ofFloat(0f, 1f);
        animador.setDuration(1200);
        animador.setInterpolator(new DecelerateInterpolator(2f));
        animador.addUpdateListener(a -> {
            float f = (float) a.getAnimatedValue();
            texto.setText(String.format(Locale.getDefault(), formato, latitud * f, longitud * f));
        });
        animador.start();
    }

    /**
     * Ondas tipo radar: cada vista crece y se desvanece en bucle, desfasadas en el tiempo.
     * Devuelve el AnimatorSet para poder detenerlo con {@link #detenerPulso(Animator, View...)}.
     */
    public static AnimatorSet pulso(View... ondas) {
        AnimatorSet conjunto = new AnimatorSet();
        AnimatorSet.Builder builder = null;
        for (int i = 0; i < ondas.length; i++) {
            PropertyValuesHolder x = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.9f);
            PropertyValuesHolder y = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.9f);
            PropertyValuesHolder a = PropertyValuesHolder.ofFloat(View.ALPHA, 0.8f, 0f);
            ObjectAnimator onda = ObjectAnimator.ofPropertyValuesHolder(ondas[i], x, y, a);
            onda.setDuration(1400);
            onda.setStartDelay(i * 700L);
            onda.setRepeatCount(ValueAnimator.INFINITE);
            onda.setInterpolator(new DecelerateInterpolator());
            builder = builder == null ? conjunto.play(onda) : builder.with(onda);
        }
        conjunto.start();
        return cancelarAlSalir(ondas[0], conjunto);
    }

    /** Detiene las ondas y las deja invisibles. */
    public static void detenerPulso(Animator animador, View... ondas) {
        if (animador != null) animador.cancel();
        for (View onda : ondas) {
            onda.setAlpha(0f);
            onda.setScaleX(1f);
            onda.setScaleY(1f);
        }
    }

    /** Halo que "respira" (aumenta y baja su brillo) mientras la linterna está encendida. */
    public static ObjectAnimator respirar(View vista) {
        PropertyValuesHolder a = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0.55f);
        PropertyValuesHolder x = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.35f, 1.15f);
        PropertyValuesHolder y = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.35f, 1.15f);
        ObjectAnimator animador = ObjectAnimator.ofPropertyValuesHolder(vista, a, x, y);
        animador.setDuration(1100);
        animador.setRepeatCount(ValueAnimator.INFINITE);
        animador.setRepeatMode(ValueAnimator.REVERSE);
        animador.start();
        return cancelarAlSalir(vista, animador);
    }
}
