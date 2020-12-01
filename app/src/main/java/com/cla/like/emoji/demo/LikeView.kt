package com.cla.like.emoji.demo

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.acode.emoji.toDrawable

class LikeView(context: Context, attr: AttributeSet? = null) : AppCompatTextView(context, attr) {

    private val likeAnimatorDialog by lazy { context.likeAnimatorDialogInstance(this) }

    private var likeBean: LikeBean? = null
    private var like: Boolean = false

    init {

        val typedArray = context.obtainStyledAttributes(attr, R.styleable.LikeView, 0, 0)

        val textSize = typedArray.getDimension(
            R.styleable.LikeView_textSize,
            60.toFloat()
        )
        typedArray.recycle()

        this.setTextColor(ContextCompat.getColor(context, R.color.c6))
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        this.gravity = Gravity.CENTER
        this.compoundDrawablePadding = 8

        this.setOnClickListener {
            request()
        }
    }

    fun bind(likeBean: LikeBean) {
        this.likeBean = likeBean
        like = likeBean.like
        val num = if (like) {
            1
        } else {
            0
        }
        setLikeNum(num)
        setLikeMode()
    }

    private fun request() {
        val toLike = !like

        if (like) {
            //点赞状态下，在点赞的动画结束之前，这个时候只需要再继续做动画就行，不要取消点赞
            if (likeAnimatorDialog.isShowing) {
                likeAnimatorDialog.addEmoji(this)
                return
            }
        }

        val num = if (toLike) {
            likeAnimatorDialog.addEmoji(this)
            1
        } else {
            0
        }

        like = toLike
        likeBean?.like = like
        setLikeNum(num)
        setLikeMode()
    }

    private fun setLikeNum(num: Int) {
        text = num.toString()

        setTextColor(
            if (like) {
                ContextCompat.getColor(context, R.color.c1)
            } else {
                ContextCompat.getColor(context, R.color.c5)
            }
        )
    }

    private fun setLikeMode() {
        if (like) {
            likeMode()
        } else {
            unLikeMode()
        }
    }

    private fun likeMode() {
        setLeftDrawable(R.drawable.connector_icon_svg_like_active.toDrawable(context, R.color.c1))
    }

    private fun unLikeMode() {
        setLeftDrawable(R.drawable.connector_icon_svg_like_inactive.toDrawable(context))
    }

    private fun setLeftDrawable(likeDrawable: Drawable?) {
        if (likeDrawable == null) {
            return
        }
        post {
            try {
                val size = height / 3 * 2
                likeDrawable.bounds = Rect(0, 0, size, size)
                this.setCompoundDrawables(likeDrawable, null, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}