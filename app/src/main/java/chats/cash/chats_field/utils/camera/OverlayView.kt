package chats.cash.chats_field.utils.camera

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import chats.cash.chats_field.R

class OverlayPosition(var x: Float, var y: Float, var r: Float)

class OverlayView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint: Paint = Paint()
    private var holePaint: Paint = Paint()
    private var bitmap: Bitmap? = null
    private var layer: Canvas? = null
    private var border: Paint = Paint().also {
        it.pathEffect = DashPathEffect(floatArrayOf(5f, 10f, 15f, 20f), 0f)
    }

    // position of hole
    var holePosition: OverlayPosition = OverlayPosition(0.0f, 0.0f, 0.0f)
        set(value) {
            field = value
            // redraw
            this.invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bitmap == null) {
            configureBitmap()
        }
        val ovalWidth = width / 1.5f // twice the width of the circle
        val ovalHeight = height / 2.1f // twice the height of the circle

        // Calculate the position of the center of the oval
        val centerX = (width - ovalWidth) / 2f
        val centerY = (180f)

        // draw hole
        //  layer?.drawCircle((width / 2).toFloat(), (height / 3).toFloat(), 450f, border)
        //  layer?.drawCircle((width / 2).toFloat(), (height / 3).toFloat(), 450f, holePaint.apply { alpha = 0 })

        //  draw background
        layer?.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat(), paint)
        layer?.drawOval(centerX, centerY, centerX + ovalWidth, centerY + ovalHeight, border)
        layer?.drawOval(
            centerX,
            centerY,
            centerX + ovalWidth,
            centerY + ovalHeight,
            holePaint.apply { alpha = 0 },
        )
        // draw bitmap
        canvas.drawBitmap(bitmap!!, 0.0f, 0.0f, paint)
    }

    private fun configureBitmap() {
        // create bitmap and layer
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        layer = Canvas(bitmap!!)
    }

    init {

        val a: TypedArray =
            getContext()
                .obtainStyledAttributes(
                    attrs,
                    R.styleable.OverlayView,
                    0,
                    0,
                )

        val color = a.getColor(R.styleable.OverlayView_circleColor, Color.parseColor("#FFFFFF"))

        // do something with str

        // do something with str
        a.recycle()

        // configure background color
        val backgroundAlpha = 0.7
        paint.color = ColorUtils.setAlphaComponent(
            context?.let {
                ContextCompat.getColor(
                    it,
                    R.color.overlay,
                )
            }!!,
            (255 * backgroundAlpha).toInt(),
        )

        border.color = color
        border.strokeWidth = 30F
        border.style = Paint.Style.STROKE
        border.isAntiAlias = true
        border.isDither = true

        // configure hole color & mode
        holePaint.color = ContextCompat.getColor(context, android.R.color.transparent)

        holePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    public fun setCircleColor(color: Int) {
        border.color = color
        invalidate()
    }
}
