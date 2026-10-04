package com.devstbryan.appproximaprueba.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.devstbryan.appproximaprueba.R;

/**
 * Vista personalizada (Custom View) que dibuja "manchas de luz" difuminadas
 * que se mueven lentamente, como una aurora boreal.
 *
 * Conceptos que muestra:
 *  - Extender {@link View} y dibujar en {@link #onDraw(Canvas)} con Canvas y Paint.
 *  - Atributos personalizados declarados en res/values/attrs.xml.
 *  - Animación con {@link ValueAnimator} que llama a invalidate() en cada cuadro.
 *  - Buenas prácticas: los objetos se crean fuera de onDraw y la animación se pausa
 *    cuando la vista no está visible, para ahorrar batería.
 */
public class AuroraView extends View {

    /** Una mancha de luz: posición base, amplitud y velocidad de su movimiento. */
    private static final class Mancha {
        final int color;
        final float baseX, baseY, amplitudX, amplitudY, velocidad, fase, escala;
        Shader shader;
        float radio;

        Mancha(int color, float baseX, float baseY, float amplitudX, float amplitudY,
               float velocidad, float fase, float escala) {
            this.color = color;
            this.baseX = baseX;
            this.baseY = baseY;
            this.amplitudX = amplitudX;
            this.amplitudY = amplitudY;
            this.velocidad = velocidad;
            this.fase = fase;
            this.escala = escala;
        }
    }

    private static final long DURACION_CICLO = 24_000L;

    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path recorte = new Path();
    private final RectF limites = new RectF();
    private final Mancha[] manchas;
    private final float radioInferior;
    private final float intensidad;

    private ValueAnimator animador;
    private float tiempo;

    public AuroraView(Context context) {
        this(context, null);
    }

    public AuroraView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Lectura de los atributos personalizados (app:radioInferior y app:intensidad)
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AuroraView);
        radioInferior = a.getDimension(R.styleable.AuroraView_radioInferior, 0f);
        intensidad = a.getFloat(R.styleable.AuroraView_intensidad, 0.55f);
        a.recycle();

        manchas = new Mancha[]{
                new Mancha(color(R.color.rosa), 0.15f, 0.20f, 0.20f, 0.15f, 1f, 0f, 0.65f),
                new Mancha(color(R.color.cian), 0.85f, 0.30f, 0.15f, 0.20f, 0.8f, 2f, 0.55f),
                new Mancha(color(R.color.amarillo), 0.55f, 0.95f, 0.25f, 0.10f, 1.2f, 4f, 0.45f),
                new Mancha(color(R.color.violeta_claro), 0.40f, 0.50f, 0.30f, 0.25f, 0.6f, 1f, 0.70f)
        };

        // Es solo decoración: los lectores de pantalla la ignoran
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    private int color(int recurso) {
        return ContextCompat.getColor(getContext(), recurso);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == 0 || h == 0) return;
        float lado = Math.max(w, h);
        int alfa = Math.round(255 * Math.max(0f, Math.min(1f, intensidad)));
        // Los degradados se crean una sola vez por tamaño, nunca dentro de onDraw
        for (Mancha m : manchas) {
            m.radio = lado * m.escala;
            int centro = Color.argb(alfa, Color.red(m.color), Color.green(m.color), Color.blue(m.color));
            m.shader = new RadialGradient(0f, 0f, m.radio,
                    centro, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        }
        // Zona de dibujo con las esquinas inferiores redondeadas (igual que el fondo del encabezado)
        limites.set(0, 0, w, h);
        float r = radioInferior;
        recorte.reset();
        recorte.addRoundRect(limites, new float[]{0, 0, 0, 0, r, r, r, r}, Path.Direction.CW);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0 || manchas[0].shader == null) return;
        canvas.save();
        if (radioInferior > 0) canvas.clipPath(recorte);
        for (Mancha m : manchas) {
            double angulo = tiempo * m.velocidad + m.fase;
            float x = w * (m.baseX + m.amplitudX * (float) Math.sin(angulo));
            float y = h * (m.baseY + m.amplitudY * (float) Math.cos(angulo * 0.8));
            pincel.setShader(m.shader);
            canvas.save();
            canvas.translate(x, y);
            canvas.drawCircle(0f, 0f, m.radio, pincel);
            canvas.restore();
        }
        canvas.restore();
    }

    // ===== Ciclo de vida de la animación =====

    @Override
    public void onVisibilityAggregated(boolean visible) {
        super.onVisibilityAggregated(visible);
        if (visible) iniciar();
        else detener();
    }

    @Override
    protected void onDetachedFromWindow() {
        detener();
        super.onDetachedFromWindow();
    }

    private void iniciar() {
        // Respeta la opción de accesibilidad "Quitar animaciones": se dibuja estático
        if (!ValueAnimator.areAnimatorsEnabled()) return;
        if (animador == null) {
            animador = ValueAnimator.ofFloat(0f, (float) (2 * Math.PI));
            animador.setDuration(DURACION_CICLO);
            animador.setRepeatCount(ValueAnimator.INFINITE);
            animador.setInterpolator(new LinearInterpolator());
            animador.addUpdateListener(a -> {
                tiempo = (float) a.getAnimatedValue();
                invalidate();
            });
            animador.start();
        } else if (animador.isPaused()) {
            animador.resume();
        }
    }

    private void detener() {
        if (animador != null && animador.isRunning()) animador.pause();
    }
}
