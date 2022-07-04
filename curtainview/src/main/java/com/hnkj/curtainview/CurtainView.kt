package com.hnkj.curtainview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toRect
import kotlin.math.roundToInt

/**
 * @author: zhuw
 * Created by zhuwang 2021-06-05-星期六-下午4:37
 * Email zhuwang999@foxmail.com
 */
class CurtainView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    private var mMin: Int = 0
    private var mMax: Int = 100

    /**
     * 0为透明
     */
    private var mRodColor: Int = 0
    private var mProgressColor: Int = 0
    private var mThumbDrawable: Drawable? = null
    private var mLeftProgressDrawable: Drawable? = null
    private var mRightProgressDrawable: Drawable? = null
    private var mDuration: Int = 2500
    private var mProgress: Int = 100

    //窗帘杆的高度
    private var rodHeight = 80f

    //最小的进度,两边不能小于这个距离
    private var minProgress = 83f

    //当前View的中线
    private var middleLine = 400

    private var mWidth = 988f
    private var mHeight = 506f
    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var rodPath: Path? = null

    private var leftRect: RectF? = null
    private var rightRect: RectF? = null
    private var leftPath: Path? = null
    private var rightPath: Path? = null
    private val radius = 20f

    private var thumbRect: Rect? = null
    private var thumbHeightHalf: Int = 82
    private var moved = false
    private var valueAnimator: ValueAnimator? = null

    //是否按在滑块上面的标志
    private var isTouch = false

    private var isDoubled = true

    interface OnProgressChangeListener {
        /**
         * 进度发生变化
         *
         * @param seekBar  拖动条
         * @param progress 当前进度数值
         * @param isUser   是否是用户操作, true 表示用户拖动, false 表示通过代码设置
         */
        fun onProgressChanged(seekBar: CurtainView?, progress: Int, isUser: Boolean)

        /**
         * 用户开始拖动
         *
         * @param seekBar 拖动条
         */
        fun onStartTrackingTouch(seekBar: CurtainView?)

        /**
         * 用户结束拖动
         *
         * @param seekBar 拖动条
         */
        fun onStopTrackingTouch(seekBar: CurtainView?)
    }

    private var mOnProgressChangeListener: OnProgressChangeListener? = null

    fun setOnProgressChangeListener(onProgressChangeListener: OnProgressChangeListener?) {
        mOnProgressChangeListener = onProgressChangeListener
    }

    init {
        context.withStyledAttributes(attributeSet, R.styleable.CurtainView) {
            setMin(getInt(R.styleable.CurtainView_min, mMin))
            setMin(getInt(R.styleable.CurtainView_min, mMin))
            setMax(getInt(R.styleable.CurtainView_max, mMax))
            setDurtain(getInt(R.styleable.CurtainView_duration, mDuration))
            setProgress(getInt(R.styleable.CurtainView_progress, mProgress))
            mLeftProgressDrawable =
                getDrawable(R.styleable.CurtainView_curtain_leaves_left_drawable)
            mRightProgressDrawable =
                getDrawable(R.styleable.CurtainView_curtain_leaves_right_drawable)
            setMinProgress(
                getFloat(
                    R.styleable.CurtainView_min_progress,
                    minProgress
                )
            )
            setProgressColor(
                getInt(
                    R.styleable.CurtainView_curtain_leaves_color,
                    mProgressColor
                )
            )
            setRodColor(
                getInt(
                    R.styleable.CurtainView_curtain_rod_color,
                    mRodColor
                )
            )
            setRodHeight(
                getDimension(
                    R.styleable.CurtainView_curtain_rod_height,
                    rodHeight
                )
            )
            setThumb(getDrawable(R.styleable.CurtainView_curtain_thumb))
            setDouble(
                getBoolean(
                    R.styleable.CurtainView_curtain_type,
                    isDoubled
                )
            )
        }
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.style = Paint.Style.FILL
        if (mRodColor != 0) {
            rodPath = Path()
        }

        leftRect = RectF()
        rightRect = RectF()
        if (mThumbDrawable != null) {
            thumbRect = Rect()
        }
        if (mProgressColor != 0) {
            leftPath = Path()
            rightPath = Path()
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //不是透明就画窗帘杆
        if (mRodColor != 0) {
            mPaint.color = mRodColor
            canvas.drawPath(rodPath!!, mPaint)
        }
        //不是透明就画窗帘叶子
        if (mProgressColor != 0) {
            mPaint.color = mProgressColor
            canvas.drawPath(leftPath!!, mPaint)
            if (isDoubled) {
                canvas.drawPath(rightPath!!, mPaint)
            }
        }
        //画窗子叶子的drawable 实际上也可以使用ColorDrawable 只不过是正方形的叶子
        mLeftProgressDrawable?.run {
            bounds = leftRect!!.toRect()
            draw(canvas)
        }
        mRightProgressDrawable?.run {
            if (isDoubled) {
                bounds = rightRect!!.toRect()
                draw(canvas)
            }
        }
        mThumbDrawable?.apply {
            bounds = thumbRect!!
            draw(canvas)
        }
    }

    private fun doRefreshProgress(oldProgress: Int) {
        if (valueAnimator != null && valueAnimator!!.isRunning) {
            valueAnimator!!.cancel()
        }
        valueAnimator = ValueAnimator.ofInt(oldProgress, mProgress)
            .apply {
                addUpdateListener { animation: ValueAnimator ->
                    mProgress = animation.animatedValue as Int
                    if (mProgress in mMin..mMax) {
                        computeProgressRect()
                        invalidate()
                    }
                }
            }
        valueAnimator!!.duration = mDuration.toLong()
        valueAnimator!!.start()
    }

    fun stopRefreshProgress() {
        valueAnimator?.let {
            if (it.isRunning) {
                it.cancel()
            }
        }
    }

    /**
     * 矩形的大小根据设置的 minProgress,mMax,mProgress 的三个值来决定
     */
    private fun computeProgressRect() {

        var rectMargin = if (!isDoubled) {
            (mWidth - minProgress) / (mMax - mMin) * mProgress + minProgress
        } else {
            (middleLine - minProgress) / (mMax - mMin) * mProgress + minProgress
        }
        if (isDoubled) {
            //矩形边距不能超过中线
            if (rectMargin > middleLine) {
                rectMargin = middleLine.toFloat()
            }
        }
        leftRect?.let {
            it.set(
                0F, rodHeight,
                rectMargin, mHeight
            )
            leftPath?.apply {
                //每次设置路径前都需要先重置,否则不会生效
                reset()
                addRoundRect(
                    it,
                    floatArrayOf(0F, 0F, 0F, 0F, radius, radius, radius, radius),
                    Path.Direction.CCW
                )
            }
        }
        if (isDoubled) {
            rectMargin =
                mWidth - minProgress - (mWidth - minProgress - middleLine) / (mMax - mMin) * mProgress
            if (rectMargin < middleLine) {
                rectMargin = middleLine.toFloat()
            }

            rightRect?.let {
                it.set(
                    rectMargin,
                    rodHeight,
                    mWidth,
                    mHeight
                )
                rightPath?.apply {
                    //每次设置路径前都需要先重置,否则不会生效
                    reset()
                    addRoundRect(
                        it,
                        floatArrayOf(0F, 0F, 0F, 0F, radius, radius, radius, radius),
                        Path.Direction.CCW
                    )
                }
            }
        }

        if (isDoubled) {
            thumbRect?.set(
                (leftRect!!.right - thumbHeightHalf).roundToInt(),
                ((mHeight - rodHeight) / 2 - thumbHeightHalf + rodHeight).toInt(),
                (leftRect!!.right + thumbHeightHalf).roundToInt(),
                ((mHeight - rodHeight) / 2 + thumbHeightHalf + rodHeight).toInt()
            )
        } else {
            //单开帘时需要判断滑块是否超出view的宽度
            thumbRect?.set(
                if (leftRect!!.right > mWidth - thumbHeightHalf) {
                    (leftRect!!.right - thumbHeightHalf * 2).roundToInt()
                } else {
                    (leftRect!!.right - thumbHeightHalf).roundToInt()
                },
                ((mHeight - rodHeight) / 2 - thumbHeightHalf + rodHeight).toInt(),
                if (leftRect!!.right > mWidth - thumbHeightHalf) {
                    (leftRect!!.right).roundToInt()
                } else {
                    (leftRect!!.right + thumbHeightHalf).roundToInt()
                },
                ((mHeight - rodHeight) / 2 + thumbHeightHalf + rodHeight).toInt()
            )
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                moved = false
                //只有手指在滑块中才能滑动
                mThumbDrawable?.let {
                    if (it.bounds.contains(event.x.toInt(), event.y.toInt())) {
                        isTouch = true
                        mOnProgressChangeListener?.onStartTrackingTouch(this)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (null != mOnProgressChangeListener && moved) {
                    isTouch = false
                    mOnProgressChangeListener!!.onStopTrackingTouch(this)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                var x = event.x
                if (x > minProgress && isTouch) {
                    if (isDoubled) {
                        if (x > middleLine) {
                            x = middleLine.toFloat()
                        }
                    } else {
                        if (x > mWidth) {
                            x = mWidth
                        }
                    }
                    moved = true
                    val ctrlProgress = if (isDoubled) {
                        ((x - minProgress) * (mMax - mMin) / (middleLine - minProgress)).toInt()
                    } else {
                        ((x - minProgress) * (mMax - mMin) / (mWidth - minProgress)).toInt()
                    }
                    if (ctrlProgress != mProgress) {
                        mOnProgressChangeListener?.onProgressChanged(this, ctrlProgress, true)
                        mProgress = ctrlProgress
                        computeProgressRect()
                        postInvalidate()
                    }
                }
            }
        }
        return true
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = (w + paddingLeft + paddingRight).toFloat()
        mHeight = (h + paddingTop + paddingBottom).toFloat()
        middleLine = (mWidth / 2).toInt()
        thumbHeightHalf = mThumbDrawable?.intrinsicHeight?.div(2) ?: thumbHeightHalf
        //顺时针方向添加圆角
        rodPath?.addRoundRect(
            RectF(0F, 0F, mWidth, rodHeight),
            floatArrayOf(radius, radius, radius, radius, 0F, 0F, 0F, 0F),
            Path.Direction.CCW
        )
        computeProgressRect()
    }


    fun setMin(percent: Int) {
        if (percent < 0) {
            return
        }
        mMin = percent
    }

    fun getMin() = mMin

    fun setMax(percent: Int) {
        if (percent > 100 || percent <= 0) {
            return
        }
        mMax = percent
    }

    fun getMax() = mMax

    fun setDurtain(duration: Int) {
        mDuration = duration
    }

    fun getProgress() = mProgress

    fun setProgress(percent: Int) {
        var lastPercent: Int
        lastPercent = percent
        if (lastPercent < mMin) {
            lastPercent = mMin
        }
        if (lastPercent > mMax) {
            lastPercent = mMax
        }
        if (mProgress == lastPercent) {
            return
        }
        val temp = mProgress
        mProgress = lastPercent
        doRefreshProgress(temp)
    }

    /**
     * 窗帘叶子的颜色
     */
    fun setProgressColor(color: Int) {
        mProgressColor = color
    }

    /**
     * 窗帘杆的颜色
     */
    fun setRodColor(color: Int) {
        mRodColor = color
        mPaint.color = color
    }

    /**
     * 设置滑块
     */
    fun setThumb(drawable: Drawable?) {
        mThumbDrawable = drawable
    }

    /**
     * 设置窗帘叶子最小距离
     */
    fun setMinProgress(percent: Float) {
        minProgress = percent
    }

    /**
     * 窗帘杆的高度
     */
    fun setRodHeight(height: Float) {
        rodHeight = height
    }

    private fun setDouble(isDouble: Boolean) {
        isDoubled = isDouble
    }

    /**
     * 设置是双开帘还是单开帘
     */
    fun setCurtainType(isDouble: Boolean) {
        isDoubled = isDouble
        computeProgressRect()
        postInvalidate()
    }
}