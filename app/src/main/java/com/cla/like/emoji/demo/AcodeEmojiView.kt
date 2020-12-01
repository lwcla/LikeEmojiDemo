package com.cla.like.emoji.demo

import android.animation.*
import android.content.Context
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import java.util.*

typealias EmojiAnimatorListener = () -> Unit

/**
 * user:yangtao
 * date:2018/6/151732
 * email:yangtao@bjxmail.com
 * introduce:仿IOS今日头条点赞效果
 */
class AcodeEmojiView(context: Context, attrs: AttributeSet? = null) :
        RelativeLayout(context, attrs) {

    companion object {
        //动画执行时间
        private const val ANI_DURATION = 2000L

        //一次动画中表情数量最大值
        private const val EMOJI_NUM_MAX = 15

        //一次动画中表情数量最小值
        private const val EMOJI_NUM_MIN = 5
    }

    private var mWidth = 0
    private var mHeight = 0

    // 当前点的实际位置
    private val pos: FloatArray = FloatArray(2)

    // 当前点的tangent值,用于计算图片所需旋转的角度
    private val tan = FloatArray(2)

    //动画插值器，其实就是几种动画效果
//    private val interpolators = arrayOf<Interpolator>(AccelerateDecelerateInterpolator(), AccelerateInterpolator())
    private val interpolators = arrayOf<Interpolator>(BounceInterpolator())

    //图片
    private val icons = intArrayOf(
            R.mipmap.emoji01,
            R.mipmap.emoji02,
            R.mipmap.emoji03,
            R.mipmap.emoji04,
            R.mipmap.emoji05,
            R.mipmap.emoji06,
            R.mipmap.emoji07,
            R.mipmap.emoji08,
            R.mipmap.emoji09,
            R.mipmap.emoji10,
            R.mipmap.emoji11,
            R.mipmap.emoji12,
            R.mipmap.emoji13,
            R.mipmap.emoji14,
            R.mipmap.emoji15,
            R.mipmap.emoji16,
            R.mipmap.emoji17,
            R.mipmap.emoji18,
            R.mipmap.emoji19,
            R.mipmap.emoji20,
            R.mipmap.emoji21,
            R.mipmap.emoji22,
            R.mipmap.emoji23,
            R.mipmap.emoji24,
            R.mipmap.emoji25,
            R.mipmap.emoji26,
            R.mipmap.emoji27,
            R.mipmap.emoji28,
            R.mipmap.emoji29,
            R.mipmap.emoji30,
            R.mipmap.emoji31,
            R.mipmap.emoji32,
            R.mipmap.emoji33,
            R.mipmap.emoji34,
            R.mipmap.emoji35,
            R.mipmap.emoji36,
            R.mipmap.emoji37,
            R.mipmap.emoji38
    )

    private val random = Random()
    private val showEmojiNum: Int
        get() = (random.nextFloat() * (EMOJI_NUM_MAX - EMOJI_NUM_MIN + 1) + EMOJI_NUM_MIN).toInt()
    private val randomX: Float
        get() = random.nextFloat() * mWidth
    private val randomY: Float
        get() = random.nextFloat() * (mHeight / 3 * 2 - 200 + 1) + 200

    //起始坐标X
    private var startX = -1f

    //起始坐标Y
    private var startY = -1f

    //空闲状态下的ImageView，可以用来做动画
    private val freeViewList = mutableListOf<ImageView>()
    private val animatorList = mutableListOf<AnimatorSet>()

    private var clickCount = 0

    init {
        this.isFocusable = false
        this.isClickable = false
        this.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        (context as? LifecycleOwner?)?.let {
            it.lifecycle.addObserver(object : LifecycleObserver {

                @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                fun onPause() {
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                fun onStop() {
                    cancelAni()
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    //看一下总共创建了多少个ImageView
                    println("AcodeEmojiView.onDestroy freeViewList=${freeViewList.size}")
                    it.lifecycle.removeObserver(this)
                }
            })
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = measuredWidth
        mHeight = measuredHeight
    }

    /**
     * 取消动画
     */
    fun cancelAni() {
        animatorList.filter { set -> set.isRunning }.forEach { set ->
            try {
                if (set.isRunning) {
                    set.end()
                    set.cancel()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 添加emoji
     */
    fun addEmoji(view: View, listener: EmojiAnimatorListener) {
        clickCount++

        val location = IntArray(2) // 存储动画开始位置的x，y坐标
        view.getLocationOnScreen(location)

        startX = (location[0] + view.width / 3).toFloat()
        startY = location[1].toFloat()

        val viewList = mutableListOf<ImageView>()
        val dataSize = showEmojiNum
        val viewSize = freeViewList.size

        val beyondNum = dataSize - viewSize

        if (beyondNum > 0) {
            //view的数量还不够，需要新建
            repeat(beyondNum) {
                freeViewList.add(ImageView(context).apply {
                    this.isFocusable = false
                    this.isClickable = false
                    this.layoutParams = LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        width = 80
                        height = 80
                    }
                    setImageResource(icons[random.nextInt(icons.size)])
                })
            }
        }

        repeat(dataSize) {
            val emojiView = freeViewList[it]
            viewList.add(emojiView)
            addView(emojiView)
        }

        freeViewList.removeAll(viewList)
        // 开启动画，并且用完销毁
        val set = getEmojiAnimSet(viewList)
        animatorList.add(set)
        set.start()
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                clickCount--
                if (clickCount == 0) {
                    //所有的动画执行结束
                    listener()
                }

                viewList.forEach { removeView(it) }
                freeViewList.addAll(viewList)
            }
        })
    }

    /**
     * 动画集合
     * 缩放，透明渐变
     * 贝塞尔曲线的动画
     */
    private fun getEmojiAnimSet(ivs: List<ImageView>): AnimatorSet {
        val set = AnimatorSet()

        ivs.forEach {
            // 1.alpha动画
            val alpha = ObjectAnimator.ofFloat(it, "alpha", 1f, 0f)
            // 2.缩放动画
            val scaleX = ObjectAnimator.ofFloat(it, "scaleX", 1f, 0f)
            val scaleY = ObjectAnimator.ofFloat(it, "scaleY", 1f, 0f)
            // 动画集合，playTogether同时执行这几个动画
            set.playTogether(alpha, scaleX, scaleY)
        }

        // 贝塞尔曲线动画
        val bzier = getBzierAnimator(ivs)
        //重新声明一个动画集合
        val set2 = AnimatorSet()
        set2.play(bzier).with(set)
        set2.duration = ANI_DURATION
        return set2
    }

    private fun getBzierAnimator(ivs: List<ImageView>): ValueAnimator {

        val pathList = ivs.map { getPath() }

        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.interpolator = interpolators[random.nextInt(interpolators.size)]
        valueAnimator.addUpdateListener { value -> // 用于纪录当前的位置,取值范围[0,1]映射Path的整个长度
            val currentValue = value.animatedValue as Float

            if (currentValue == 1f) {
                pathList.forEach { it.reset() }
            }

            ivs.forEachIndexed { index, imageView ->
                setIvXY(pathList[index], imageView, currentValue)
            }
        }
        return valueAnimator
    }

    private fun getPath() = Path().apply {
        moveTo(startX, startY)

        val distanceX1 = randomX
        val distanceX2 = randomX

        val distanceY1 = randomY
        val distanceY2 = randomY

        val middleX = if (distanceX1 > distanceX2) distanceX2 else distanceX1
        val endX = if (distanceX1 > distanceX2) distanceX1 else distanceX2

        val y1 = startY - distanceY1
        val y2 = startY + distanceY2

        //往左跑还是往右跑
        val direction = random.nextInt(2)
        val left = direction % 2 == 0
        val x1 = if (left) {
            //向左
            startX - middleX
        } else {
            //向右
            startX + middleX
        }

        val x2 = if (left) {
            //向左
            startX - endX
        } else {
            //向右
            startX + endX
        }

        quadTo(x1, y1, x2, y2)
    }

    /**
     * PathMeasure  让iv跟着path走
     *
     * @param path
     * @param iv
     */
    private fun setIvXY(path: Path, iv: ImageView, currentValue: Float) {
        val measure = PathMeasure(path, false)
        measure.getPosTan(measure.length * currentValue, pos, tan)
        iv.x = pos[0]
        iv.y = pos[1]
    }
}