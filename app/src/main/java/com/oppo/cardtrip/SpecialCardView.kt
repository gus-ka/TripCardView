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
import java.lang.Boolean
import java.lang.Integer.getInteger

object EnumCaster {
    inline fun <reified E : Enum<E>> fromInt(value: Int): E {
        return enumValues<E>().first { it.toString().toInt() == value }
    }
}

enum class CornerType(private val value: Int) {
    ROUNDED(0), CURVE(1), CUT(2);

    override fun toString(): String = value.toString()
    fun toInteger(): Int = value
}

class SpecialCardView : View {
    private var attributes: TypedArray? = null
    private val stripSideMargin = 0


    private var allCornerType: CornerType = CornerType.ROUNDED
    private var topLeftCornerType: CornerType = allCornerType
    private var topRightCornerType: CornerType = allCornerType
    private var bottomRightCornerType: CornerType = allCornerType
    private var bottomLeftCornerType: CornerType = allCornerType

    private var cornerValue: Float = 0.0F
    private var cornerTopLeftValue: Float? = cornerValue
    private var cornerTopRightValue: Float? = cornerValue
    private var cornerBottomRightValue: Float? = cornerValue
    private var cornerBottomLeftValue: Float? = cornerValue

    private val strokeWidthStart = 1F
    private val strokeWidthTop = 1F
    private val strokeWidthEnd = 1F
    private val strokeWidthBottom = 5F

    private var borderColor = Color.GRAY

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
    private fun init(context: Context?, attrs: AttributeSet?) {
        if (context == null || attrs == null) {
            return
        }
        try {
            attributes = context.obtainStyledAttributes(attrs, R.styleable.SpecialCardView)
            setBorderColor(attributes!!.getResourceId(R.styleable.SpecialCardView_borderSpecialCardViewColor, R.color.black))

            cornerValue = attributes!!.getDimension(R.styleable.SpecialCardView_cornerValue, 0.0F)
            cornerTopLeftValue = attributes!!.getDimension(R.styleable.SpecialCardView_cornerTopLeftValue, cornerValue)
            cornerTopRightValue = attributes!!.getDimension(R.styleable.SpecialCardView_cornerTopRightValue, cornerValue)
            cornerBottomRightValue = attributes!!.getDimension(R.styleable.SpecialCardView_cornerBottomRightValue, cornerValue)
            cornerBottomLeftValue = attributes!!.getDimension(R.styleable.SpecialCardView_cornerBottomLeftValue, cornerValue)

            allCornerType = setCornerTypeValue(R.styleable.SpecialCardView_allCornerType, 0)
            topLeftCornerType = setCornerTypeValue(R.styleable.SpecialCardView_topLeftCornerType, allCornerType.toInteger())
            topRightCornerType = setCornerTypeValue(R.styleable.SpecialCardView_topRightCornerType, allCornerType.toInteger())
            bottomRightCornerType = setCornerTypeValue(R.styleable.SpecialCardView_bottomRightCornerType, allCornerType.toInteger())
            bottomLeftCornerType = setCornerTypeValue(R.styleable.SpecialCardView_bottomLeftCornerType, allCornerType.toInteger())
        } finally {
            attributes?.recycle()
        }
    }

    private fun setBorderColor(@ColorRes borderColorResId: Int) {
        borderColor = ContextCompat.getColor(context, borderColorResId)
    }

    private fun setCornerTypeValue(cornerType: Int, value: Int): CornerType {
        return EnumCaster.fromInt(attributes!!.getInteger(cornerType, value))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val paint = Paint()
        val path = getPath()
        setLayerType(LAYER_TYPE_SOFTWARE, null) // Needed for correctly drawing the shadow under the custom canvas shape

        // Used for filling in the card with the background colour and adding the shadow
        paint.run {
            strokeWidth = 5f
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

    // Draws and returns the path used for the custom Trip Card View
    private fun getPath(): Path {
        val path = Path()
        val specialCorner = RectF()

        path.run {
            // Setup Start Point (Bottom Left)
            var mAddAdjX = cornerBottomLeftValue!!
            var mAddAdjY = if (bottomLeftCornerType == CornerType.CURVE) (strokeWidthBottom * 3) else 0.0F
            moveTo(minX + mAddAdjX, maxY + mAddAdjY) // Move to start point (bottom left)
            //Bottom left card type setting
            when (bottomLeftCornerType) {
                CornerType.ROUNDED -> {
                    quadTo(minX, maxY, minX, maxY - cornerBottomLeftValue!!) // Bottom left rounded corner to bottom left side
                    lineTo(minX, minY + cornerTopLeftValue!!) // Bottom left side
                }
                CornerType.CURVE -> {
                    specialCorner.run {
                        left = minX - cornerBottomLeftValue!! / 2
                        right = minX + cornerBottomLeftValue!! / 2
                        bottom = maxY + cornerBottomLeftValue!! / 2
                        top = maxY - cornerBottomLeftValue!! / 2
                    }
                    arcTo(specialCorner, 90F, -180F)// Draw special corner (Bottom Left)
                    lineTo(minX, maxY - (cornerBottomLeftValue!! / 2)) // Bottom Left Side
                    lineTo(minX, minY + cornerTopLeftValue!!) // Top Left Side
                }
                CornerType.CUT -> {
                    lineTo(minX, maxY - cornerBottomLeftValue!!) // Bottom left side
                    lineTo(minX, minY + cornerTopLeftValue!!) // Top Left Side
                }
            }
            //Top left card type setting
            when (topLeftCornerType) {
                CornerType.ROUNDED -> {
                    quadTo(minX, minY, minX + cornerTopLeftValue!!, minY) // Top left side rounded corner to top left
                    lineTo(maxX - cornerTopRightValue!!, minY) // Top right
                }
                CornerType.CURVE -> {
                    specialCorner.run {
                        left = minX - cornerTopLeftValue!! / 2
                        right = minX + cornerTopLeftValue!! / 2
                        bottom = minY + cornerTopLeftValue!! / 2
                        top = minY - cornerTopLeftValue!! / 2
                    }
                    arcTo(specialCorner, 90F, -180F)// Draw special corner (Top Left)

                    lineTo(minX + (cornerTopLeftValue!! + 8), minY - (strokeWidthBottom * 3)) // Top left
                    lineTo(minX + (cornerTopLeftValue!! + 8), minY - 3) // Top left
                    lineTo(maxX - (cornerTopRightValue!! + 8), minY - 3) // Top Right
                    lineTo(maxX - (cornerTopRightValue!! + 8), minY - (strokeWidthBottom * 3)) // Top Right
                }
                CornerType.CUT -> {
                    lineTo(minX + cornerTopLeftValue!!, minY) // Top Left
                    lineTo(maxX - cornerTopRightValue!!, minY) // Top Right
                }
            }
            //Top right card type setting
            when (topRightCornerType) {
                CornerType.ROUNDED -> {
                    quadTo(maxX, minY, maxX, minY + cornerTopRightValue!!) // Top right rounded corner to top right side
                    lineTo(maxX, maxY - (cornerBottomRightValue!!)) // Bottom right side
                }
                CornerType.CURVE -> {
                    specialCorner.run {
                        left = maxX - cornerTopRightValue!! / 2
                        right = maxX + cornerTopRightValue!! / 2
                        bottom = minY + cornerTopRightValue!! / 2
                        top = minY - cornerTopRightValue!! / 2
                    }
                    arcTo(specialCorner, -90F, -180F) // Draw special corner (Top Right)
                    lineTo(maxX, maxY - (cornerBottomRightValue!!)) // Bottom right side
                }
                CornerType.CUT -> {
                    lineTo(maxX, minY + cornerTopRightValue!!) // Top right side
                    lineTo(maxX, maxY - cornerBottomRightValue!!) // Bottom right side
                }
            }
            //Bottom right card type setting
            when (bottomRightCornerType) {
                CornerType.ROUNDED -> {
                    quadTo(maxX, maxY, maxX - cornerBottomRightValue!!, maxY) // Bottom right side rounded corner to bottom right
                    lineTo(maxX - cornerBottomLeftValue!!, maxY) // Bottom right
                }
                CornerType.CURVE -> {
                    specialCorner.run {
                        left = maxX - cornerBottomRightValue!! / 2
                        right = maxX + cornerBottomRightValue!! / 2
                        bottom = maxY + cornerBottomRightValue!! / 2
                        top = maxY - cornerBottomRightValue!! / 2
                    }
                    arcTo(specialCorner, -90F, -180F) // Draw special corner (Bottom Right)
                    lineTo(maxX - (cornerBottomRightValue!! + 8), maxY + (strokeWidthBottom * 3)) // Bottom Right
                    lineTo(maxX - (cornerBottomRightValue!! + 8), maxY + 3) // Bottom Right
                    lineTo(minX + (cornerBottomLeftValue!! + 8), maxY + 3) // Bottom left
                    lineTo(minX + (cornerBottomLeftValue!! + 8), maxY + (strokeWidthBottom * 3)) // Bottom left
                }
                CornerType.CUT -> {
                    lineTo(maxX - cornerBottomRightValue!!, maxY) // Bottom right
                    lineTo(minX + cornerBottomLeftValue!!, maxY) // Bottom left
                }
            }
            close() // Closes and finishes the path
        }
        return path
    }
}