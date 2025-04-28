package com.mod.paragraphapp

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

class RoundedBackgroundReplacementSpan(
    private val bgColor: Int,
    private val textColor: Int,
    private val cornerRadius: Float = 24f,
    private val paddingHorizontal: Float = 16f,
    private val paddingVertical: Float = 8f
) : ReplacementSpan() {

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val textWidth = paint.measureText(text, start, end)
        return (textWidth + 2 * paddingHorizontal).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val originalColor = paint.color

        val textWidth = paint.measureText(text, start, end)
        val textHeight = paint.descent() - paint.ascent()

        val rectTop = y + paint.ascent() - paddingVertical
        val rectBottom = y + paint.descent() + paddingVertical

        val rectLeft = x
        val rectRight = x + textWidth + 2 * paddingHorizontal

        // Draw background
        paint.color = bgColor
        canvas.drawRoundRect(
            rectLeft,
            rectTop,
            rectRight,
            rectBottom,
            cornerRadius,
            cornerRadius,
            paint
        )

        // Draw text
        paint.color = textColor
        canvas.drawText(text, start, end, x + paddingHorizontal, y.toFloat(), paint)

        paint.color = originalColor
    }
}
