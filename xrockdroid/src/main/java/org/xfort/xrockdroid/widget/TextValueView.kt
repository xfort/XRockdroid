package org.xfort.xrockdroid.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import org.xfort.xrockdroid.R


/**
 ** Created by ZhangHuaXin on 2021/4/7.
 **/
class TextValueView : androidx.appcompat.widget.AppCompatTextView {

    private val linePaint by lazy {
        Paint()
    }
    private val metaDP by lazy { resources.getDimension(R.dimen.meta) }
    private var lineColor = Color.DKGRAY
    var lineWidth = 0f
    var textValue: CharSequence? = null
        set(value) {
            field = value
            postInvalidate()
        }
    private var linePadding = 0f
    private var valuePadding = 0f

    private var textValueStyle = Typeface.DEFAULT
    private lateinit var txtPaint: Paint

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initFromAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        initFromAttrs(attrs)
    }

    fun initFromAttrs(attrs: AttributeSet?) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.TextValueView)

        lineWidth = typeArray.getDimension(R.styleable.TextValueView_line_stroke_width, metaDP / 2)
        lineColor = typeArray.getColor(R.styleable.TextValueView_line_color, Color.DKGRAY)
        textValue = typeArray.getText(R.styleable.TextValueView_value_text)
        var valueSize =
            typeArray.getDimension(R.styleable.TextValueView_value_text_size, metaDP * 7)
        linePadding = typeArray.getDimension(R.styleable.TextValueView_line_padding, 0f)
        valuePadding = typeArray.getDimension(R.styleable.TextValueView_value_padding, 0f)

        txtPaint = Paint()
        txtPaint.color = Color.BLACK
        txtPaint.textAlign = Paint.Align.RIGHT
        txtPaint.typeface = textValueStyle
        txtPaint.textSize = valueSize
    }

    private fun setTitleText(title: String) {
        text = title
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!textValue.isNullOrEmpty()) { //            val textBound = Rect()
            //            txtPaint.getTextBounds(textValue!!, 0, textValue!!.length, textBound)
            val fontMetrics = txtPaint.fontMetrics
            var y = height / 2 + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom

            canvas?.drawText(
                textValue.toString(), width.toFloat() - paddingRight - valuePadding, y, txtPaint
            )
        }
    }

    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)
        if (visibility == VISIBLE && lineWidth > 0) {
            linePaint.color = lineColor
            canvas?.drawRect(
                linePadding,
                height - lineWidth,
                width.toFloat() - linePadding,
                height.toFloat(),
                linePaint
            )
        }
    }

    fun setLine(color: Int, width: Float) {
        lineColor = color
        lineWidth = width
        postInvalidate()
    }

    //fun setTextValue(text: CharSequence) { //        txtPaint.typeface = typeface
    //    textValue = text
    //    postInvalidate()
    //}

    fun setTextValueSize(textSize: Float) {
        txtPaint.textSize = textSize
    }


}