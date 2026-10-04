package com.devstbryan.appproximaprueba.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.devstbryan.appproximaprueba.R;

import java.util.Random;

/**
 * Vista personalizada que lanza una explosión de confeti.
 * Cada partícula sigue una física simple: velocidad inicial + gravedad,
 * y gira mientras cae. Todo se dibuja con Canvas en {@link #onDraw(Canvas)}.
 */
public class ConfettiView extends View {

    private static final int CANTIDAD = 140;
    private static final long DURACION = 2800L;

    /** Una pieza de confeti. Los valores se calculan al lanzar, no en cada cuadro. */
    private static final class Particula {
        float x0, y0, vx, vy, giro, velocidadGiro, ancho, alto;
        int color;
        boolean circulo;
    }

    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Particula[] particulas = new Particula[CANTIDAD];
    private final Random azar = new Random();
    private final int[] colores;
    private final float densidad;

    private ValueAnimator animador;
    private float segundos;

    public ConfettiView(Context context) {
        this(context, null);
    }

    public ConfettiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        densidad = getResources().getDisplayMetrics().density;
        colores = new int[]{
                ContextCompat.getColor(context, R.color.rosa),
                ContextCompat.getColor(context, R.color.amarillo),
                ContextCompat.getColor(context, R.color.cian),
                ContextCompat.getColor(context, R.color.esmeralda),
                ContextCompat.getColor(context, R.color.naranja),
                ContextCompat.getColor(context, R.color.violeta_claro)
        };
        for (int i = 0; i < CANTIDAD; i++) particulas[i] = new Particula();
    }

    /** Lanza el confeti desde el centro superior de la vista. */
    public void lanzar() {
        if (getWidth() == 0 || !ValueAnimator.areAnimatorsEnabled()) return;
        float w = getWidth();
        float h = getHeight();
        for (Particula p : particulas) {
            p.x0 = w / 2f + (azar.nextFloat() - 0.5f) * w * 0.2f;
            p.y0 = h * 0.38f;
            p.vx = (azar.nextFloat() - 0.5f) * w * 1.6f;          // px por segundo
            p.vy = -(0.35f + azar.nextFloat() * 0.9f) * h;        // hacia arriba
            p.giro = azar.nextFloat() * 360f;
            p.velocidadGiro = (azar.nextFloat() - 0.5f) * 720f;
            p.ancho = (6 + azar.nextInt(6)) * densidad;
            p.alto = p.ancho * (0.4f + azar.nextFloat() * 0.4f);
            p.color = colores[azar.nextInt(colores.length)];
            p.circulo = azar.nextInt(4) == 0;
        }
        if (animador != null) animador.cancel();
        animador = ValueAnimator.ofFloat(0f, DURACION / 1000f);
        animador.setDuration(DURACION);
        animador.setInterpolator(new LinearInterpolator());
        animador.addUpdateListener(a -> {
            segundos = (float) a.getAnimatedValue();
            invalidate();
        });
        animador.start();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (animador == null || !animador.isRunning()) return;

        float gravedad = getHeight() * 1.4f;
        float t = segundos;
        float total = DURACION / 1000f;
        // Las partículas se desvanecen durante el último 30 % de la animación
        float desvanecer = Math.min(1f, (total - t) / (total * 0.3f));
        int alfa = Math.round(255 * Math.max(0f, desvanecer));

        for (Particula p : particulas) {
            float x = p.x0 + p.vx * t;
            float y = p.y0 + p.vy * t + 0.5f * gravedad * t * t;
            if (y > getHeight() + p.ancho) continue;

            pincel.setColor(p.color);
            pincel.setAlpha(alfa);

            canvas.save();
            canvas.translate(x, y);
            canvas.rotate(p.giro + p.velocidadGiro * t);
            if (p.circulo) {
                canvas.drawCircle(0f, 0f, p.alto / 2f, pincel);
            } else {
                // Efecto 3D: el papel "voltea" achicándose en un eje
                float volteo = (float) Math.abs(Math.cos(t * 6 + p.giro));
                canvas.drawRect(-p.ancho / 2f, -p.alto / 2f * volteo,
                        p.ancho / 2f, p.alto / 2f * volteo, pincel);
            }
            canvas.restore();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animador != null) animador.cancel();
        super.onDetachedFromWindow();
    }
}
