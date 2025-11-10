package com.achllzvr.mockkarbono.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.achllzvr.mockkarbono.R;

public class CircularProgressView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF rectF;
    private float progress = 0f; // 0.0 to 1.0

    public CircularProgressView(Context context) {
        super(context);
        init();
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Background ring
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(20f);
        backgroundPaint.setColor(getResources().getColor(R.color.colorSurfaceVariant, null));
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        // Progress ring
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20f);
        progressPaint.setColor(getResources().getColor(R.color.colorAccent, null));
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int padding = 40;

        // Setup rect for arcs
        rectF.set(
            padding,
            padding,
            width - padding,
            height - padding
        );

        // Draw background ring (full circle)
        canvas.drawArc(rectF, -90, 360, false, backgroundPaint);

        // Draw progress ring
        float sweepAngle = 360 * progress;

        // Change color if over safe range
        if (progress > 1.0f) {
            progressPaint.setColor(getResources().getColor(android.R.color.holo_red_light, null));
        } else if (progress > 0.8f) {
            progressPaint.setColor(getResources().getColor(android.R.color.holo_orange_light, null));
        } else {
            progressPaint.setColor(getResources().getColor(R.color.colorAccent, null));
        }

        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
    }

    /**
     * Set progress value
     * @param progress 0.0 to 1.0 (can exceed 1.0 for over-budget)
     */
    public void setProgress(float progress) {
        this.progress = progress;
        invalidate(); // Redraw
    }

    public float getProgress() {
        return progress;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        );
        setMeasuredDimension(size, size);
    }
}

