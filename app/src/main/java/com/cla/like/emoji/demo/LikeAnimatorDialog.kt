package com.cla.like.emoji.demo

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout

/**
 * 这样做可以保证一个activity公用一个LikeAnimatorDialog
 * 避免同一个activity会有多个点赞的控件，就会生成多个dialog对象
 *
 * @param likeView 点赞的控件
 */
internal fun Context.likeAnimatorDialogInstance(likeView: View): LikeAnimatorDialog {

    return (this as? Activity?)?.run {
        val d = window.decorView.getTag(R.id.like_dialog) as? LikeAnimatorDialog?
                ?: LikeAnimatorDialog(this)
        window.decorView.setTag(R.id.like_dialog, d)

        //如果当前点击的控件和上一次点击的控件不是同一个，那么就取消之前的动画和弹窗
        val lastView = window.decorView.getTag(R.id.last_like_view) as? View?
        if (lastView != likeView) {
            d.cancelAni()
        }
        window.decorView.setTag(R.id.last_like_view, likeView)

        d
    } ?: LikeAnimatorDialog(this)
}

/**
 * 执行点赞动画的弹窗
 */
class LikeAnimatorDialog(private val mContext: Context) : Dialog(mContext, R.style.CartDialog) {

    private val animatorView by lazy { AcodeEmojiView(mContext) }

    // R.style.CartDialog取消了进入跟退出动画
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWindow()
    }

    private fun initWindow() {
        window?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            // 设置Dialog不处理触摸事件
            this.addFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            initView()
            this.setGravity(Gravity.BOTTOM)
            this.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            this.setBackgroundDrawable(ColorDrawable(0x00000000))
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
    }

    private fun initView() {
        val animLayout = LinearLayout(context)
        animLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        setContentView(animLayout)
        animLayout.addView(animatorView)
    }

    fun addEmoji(view: View) {

        if (!isShowing) {
            try {
                show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        animatorView.apply {
            //这里要延迟一点时间，否则第一次显示动画的时候，会有问题
            postDelayed({
                addEmoji(view) { dismiss() }
            }, 50)
        }
    }

    fun cancelAni() {
        animatorView.cancelAni()
    }
}