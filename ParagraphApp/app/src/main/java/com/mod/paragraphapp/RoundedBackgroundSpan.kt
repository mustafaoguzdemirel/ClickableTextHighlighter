package com.mod.paragraphapp

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan

class RoundedBackgroundSpan(
    private val bgColor: Int,
    private val textColor: Int,
    private val cornerRadius: Float = 16f,
    private val paddingHorizontal: Float = 12f,
    private val paddingVertical: Float = 4f
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
        val textToDraw = text.subSequence(start, end).toString()
        val textWidth = paint.measureText(textToDraw)
        val textHeight = paint.descent() - paint.ascent()

        val rect = RectF(
            x,
            y + paint.ascent() - paddingVertical,
            x + textWidth + 2 * paddingHorizontal,
            y + paint.descent() + paddingVertical
        )

        // Draw background
        val bgPaint = Paint(paint).apply {
            color = bgColor
            isAntiAlias = true
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

        // Draw text
        val textPaint = Paint(paint).apply {
            color = textColor
            isAntiAlias = true
        }
        canvas.drawText(textToDraw, x + paddingHorizontal, y.toFloat(), textPaint)
    }
}
