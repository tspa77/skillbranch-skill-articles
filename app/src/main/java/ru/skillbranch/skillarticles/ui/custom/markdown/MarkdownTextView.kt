package ru.skillbranch.skillarticles.ui.custom.markdown


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.text.Spannable
import android.text.Spanned
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.core.graphics.withTranslation
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue

@SuppressLint("ViewConstructor")
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
class MarkdownTextView constructor(
    context: Context,
    fontSize: Float,
    mockHelper: SearchBgHelper? = null //for mock
) : TextView(context, null, 0), IMarkdownView {

    constructor(context: Context, fontSize: Float) : this(context, fontSize, null)

    override var fontSize: Float = fontSize
        set(value) {
            textSize = value
            field = value
        }

    override val spannableContent: Spannable
        get() = text as Spannable

    private val color = context.attrValue(R.attr.colorOnBackground)
    private val focusRect = Rect()

    private val searchBgHelper = SearchBgHelper(context) { top, bottom ->
        //TODO implement me
    }


    override fun onDraw(canvas: Canvas) {
        if (text is Spanned && layout != null) {
            canvas.withTranslation(totalPaddingLeft.toFloat(), totalPaddingRight.toFloat()) {
                searchBgHelper.draw(canvas, text as Spanned, layout)
            }
        }
        super.onDraw(canvas)
    }
}