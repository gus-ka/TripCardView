package com.oppo.cardtrip
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
class TripCardContentView : View {
    private val cornerValue: Float = 40.0F
    private val arcRadius = 40F

    private val strokeWidthStart = 0F
    private val strokeWidthTop = 0F
    private val strokeWidthEnd = 0F
    private val strokeWidthBottom = 5F

    private var borderColor = Color.parseColor("#E0E5E5")
    private var newHeight = 0
    private val minY: Float
        get() {
            return strokeWidthTop
        }
    private val maxY: Float
        get() {
            return height - strokeWidthBottom
        }
    private val maxX: Float
        get() {
            return width - strokeWidthEnd
        }
    private val minX: Float
        get() {
            return strokeWidthStart
        }
    constructor(context: Context?) : super(context) { init(context, null) }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {init(context,attrs)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    fun init(context: Context?, attrs: AttributeSet?) {
        if (context == null || attrs == null) {
            return
        }
        var attributes: TypedArray? = null
        try {
            attributes = context.obtainStyledAttributes(attrs, R.styleable.TripCardHeaderView)
            setBorderColor(attributes.getResourceId(R.styleable.TripCardHeaderView_borderColor, R.color.tripcardheaderview_border))
        } finally {
            attributes?.recycle()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val paint = Paint()
        val path = getPath()
        setLayerType(LAYER_TYPE_SOFTWARE, null) // Needed for correctly drawing the shadow under the custom canvas shape

        // Used for filling in the card with the background colour and adding the shadow
        paint.run {
            strokeWidth = 1f
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
            setShadowLayer(.01F, 0F, 5F, borderColor)
        }
        canvas?.drawPath(path, paint) ?: return

        // Used for adding the border to the card
        paint.run {
            color = borderColor
            style = Paint.Style.STROKE
            setShadowLayer(0F, 0F, 0F, borderColor)
        }
        canvas.drawPath(path, paint)
    }
    fun setBorderColor(@ColorRes borderColorResId: Int) {
        borderColor = ContextCompat.getColor(context, borderColorResId)
    }
    // Draws and returns the path used for the custom Trip Card View
    private fun getPath(): Path {
        val path = Path()
        val specialCorner = RectF()
        path.run {
            moveTo(minX + cornerValue, maxY) // Move to start point
            quadTo(minX, maxY, minX, maxY - cornerValue) // Bottom left rounded corner to bottom left side
            lineTo(minX, minY + cornerValue) // Bottom left side
            specialCorner.run {
                left = minX - arcRadius / 2
                right = minX + arcRadius / 2
                bottom = minY + arcRadius / 2
                top = minY - arcRadius / 2
            }
            arcTo(specialCorner, 90F, -180F)// Draw special corner (Top Left)
            lineTo(minX + (arcRadius + 8), minY - (strokeWidthBottom * 3)) // Top left
            lineTo(minX + (arcRadius + 8), minY - 3) // Top left
            lineTo(maxX - (arcRadius + 8), minY - 3) // Top Right
            lineTo(maxX - (arcRadius + 8), minY - (strokeWidthBottom * 3)) // Top Right
            specialCorner.run {
                left = maxX - arcRadius / 2
                right = maxX + arcRadius / 2
                bottom = minY + arcRadius / 2
                top = minY - arcRadius / 2
            }
            arcTo(specialCorner, -90F, -180F) // Draw special corner (Top Right)
            lineTo(maxX, maxY - (arcRadius)) // Bottom right side
            quadTo(maxX, maxY, maxX - cornerValue, maxY) // Bottom right side rounded corner to bottom right
            lineTo(maxX - arcRadius, maxY) // Bottom right
            close() // Closes and finishes the path
        }
        return path
    }

    // Gets the x position and the startY position for the cutouts
    private fun getXYStart(): Pair<Float, Float> {
        return Pair(maxX, minY)
    }

    private fun forceHeight(newHeight: Int) {
        this.newHeight = newHeight
        val params = layoutParams
        params.height = newHeight
        layoutParams = params
    }
}