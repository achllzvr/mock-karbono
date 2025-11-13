package com.achllzvr.mockkarbono.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.achllzvr.mockkarbono.R;

/**
 * Custom view for displaying daily progress as a circle (pedometer/fitness tracker style)
 * Shows progress as a filled circle with different states:
 * - Empty (gray circle) - no data or above safe range
 * - Partial fill (green arc) - within safe range, proportional to usage
 * - Full (solid green) - day completed within safe range
 */
public class DayProgressCircle extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF rectF;

    private float progress = 0f; // 0.0 to 1.0
    private boolean isComplete = false; // Full green ring (not filled)
    private boolean isToday = false;
    private String dateText = ""; // Date number to display (e.g., "16", "17")

    public DayProgressCircle(Context context) {
        super(context);
        init();
    }

    public DayProgressCircle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DayProgressCircle(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Background circle (empty state)
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(4f); // Will be updated in onDraw
        backgroundPaint.setColor(getResources().getColor(R.color.colorSurfaceVariant, null));

        // Progress arc (partial or complete ring)
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(4f); // Will be updated in onDraw
        progressPaint.setColor(getResources().getColor(R.color.colorAccent, null));
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        // Text paint for date number
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(getResources().getColor(R.color.colorOnBackground, null));
        textPaint.setTextSize(20f); // Will be updated in onDraw
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);

        // Dynamic stroke width based on size (roughly 7-8% of circle diameter)
        float strokeWidth = size * 0.075f;
        float padding = strokeWidth / 2f + 2;

        // Update paint stroke widths dynamically
        backgroundPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeWidth(strokeWidth);

        // Dynamic text size (roughly 35-40% of circle diameter)
        float textSize = size * 0.38f;
        textPaint.setTextSize(textSize);

        // Calculate center and radius
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = (size - padding * 2) / 2f;

        // Draw outer ring if today
        if (isToday) {
            Paint todayRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            todayRingPaint.setStyle(Paint.Style.STROKE);
            todayRingPaint.setStrokeWidth(strokeWidth * 0.5f); // Thinner ring
            todayRingPaint.setColor(getResources().getColor(R.color.colorAccent, null));
            todayRingPaint.setAlpha(100);
            canvas.drawCircle(centerX, centerY, radius + strokeWidth * 0.6f, todayRingPaint);
        }

        // Always draw background circle first
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        // Draw progress ring or complete ring
        if (isComplete) {
            // Draw complete ring (full 360° stroke in green)
            rectF.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            );
            canvas.drawArc(rectF, -90, 360, false, progressPaint);
        } else if (progress > 0) {
            // Draw progress arc (partial stroke)
            rectF.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            );

            float sweepAngle = 360 * progress;

            // Change color based on progress
            if (progress >= 1.0f) {
                // Exceeded safe range - use red
                progressPaint.setColor(getResources().getColor(android.R.color.holo_red_light, null));
            } else {
                // Within safe range - use green
                progressPaint.setColor(getResources().getColor(R.color.colorAccent, null));
            }

            canvas.drawArc(rectF, -90, Math.min(sweepAngle, 360), false, progressPaint);
        }

        // Draw date number in center
        if (!dateText.isEmpty()) {
            // Always use normal text color since we're using rings, not filled circles
            textPaint.setColor(getResources().getColor(R.color.colorOnBackground, null));

            // Calculate text position (centered vertically)
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float textHeight = fontMetrics.descent - fontMetrics.ascent;
            float textOffset = (textHeight / 2) - fontMetrics.descent;

            canvas.drawText(dateText, centerX, centerY + textOffset, textPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Get the width (will be determined by parent layout weight)
        int width = MeasureSpec.getSize(widthMeasureSpec);

        // Make the view square (height = width) for a perfect circle
        // This ensures circles are always round regardless of screen size
        setMeasuredDimension(width, width);
    }

    /**
     * Set progress value
     * @param progress 0.0 to 1.0 (percentage of safe range used)
     */
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1.0f, progress));
        this.isComplete = false;
        invalidate();
    }

    /**
     * Mark day as complete (solid green circle)
     */
    public void setComplete(boolean complete) {
        this.isComplete = complete;
        invalidate();
    }

    /**
     * Mark this circle as representing today
     */
    public void setToday(boolean today) {
        this.isToday = today;
        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    public boolean isComplete() {
        return isComplete;
    }

    /**
     * Set the date number to display in the circle
     * @param dateText Date number (e.g., "16", "17", "21")
     */
    public void setDateText(String dateText) {
        this.dateText = dateText;
        invalidate();
    }

    public String getDateText() {
        return dateText;
    }
}

