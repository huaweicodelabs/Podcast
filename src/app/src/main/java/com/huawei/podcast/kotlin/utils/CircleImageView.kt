/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.podcast.kotlin.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import com.huawei.podcast.R

internal class CircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    private var mBitmapShader: Shader? = null
    private val mShaderMatrix: Matrix
    private val mBitmapDrawBounds: RectF
    private val mStrokeBounds: RectF
    private var mBitmap: Bitmap? = null
    private val mBitmapPaint: Paint
    private val mStrokePaint: Paint
    private val mPressedPaint: Paint
    private val mInitialized: Boolean
    private var mPressed = false
    private var mHighlightEnable: Boolean
    override fun setImageResource(@DrawableRes resId: Int) {
        super.setImageResource(resId)
        setupBitmap()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        setupBitmap()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        setupBitmap()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        setupBitmap()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val halfStrokeWidth = mStrokePaint.strokeWidth / 2f
        updateCircleDrawBounds(mBitmapDrawBounds)
        mStrokeBounds.set(mBitmapDrawBounds)
        mStrokeBounds.inset(halfStrokeWidth, halfStrokeWidth)
        updateBitmapSize()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var processed = false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!isInCircle(event.x, event.y)) {
                    return false
                }
                processed = true
                mPressed = true
                invalidate()
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                processed = true
                mPressed = false
                invalidate()
                if (!isInCircle(event.x, event.y)) {
                    return false
                }
            }
        }
        return super.onTouchEvent(event) || processed
    }

    override fun onDraw(canvas: Canvas) {
        drawBitmap(canvas)
        drawStroke(canvas)
        drawHighlight(canvas)
    }

    var isHighlightEnable: Boolean
        get() = mHighlightEnable
        set(enable) {
            mHighlightEnable = enable
            invalidate()
        }

    @get:ColorInt
    var highlightColor: Int
        get() = mPressedPaint.color
        set(color) {
            mPressedPaint.color = color
            invalidate()
        }

    @get:ColorInt
    var strokeColor: Int
        get() = mStrokePaint.color
        set(color) {
            mStrokePaint.color = color
            invalidate()
        }

    @get:Dimension
    var strokeWidth: Float
        get() = mStrokePaint.strokeWidth
        set(width) {
            mStrokePaint.strokeWidth = width
            invalidate()
        }

    private fun drawHighlight(canvas: Canvas) {
        if (mHighlightEnable && mPressed) {
            canvas.drawOval(mBitmapDrawBounds, mPressedPaint)
        }
    }

    private fun drawStroke(canvas: Canvas) {
        if (mStrokePaint.strokeWidth > 0f) {
            canvas.drawOval(mStrokeBounds, mStrokePaint)
        }
    }

    private fun drawBitmap(canvas: Canvas) {
        canvas.drawOval(mBitmapDrawBounds, mBitmapPaint)
    }

    private fun updateCircleDrawBounds(bounds: RectF) {
        val contentWidth = width - paddingLeft - paddingRight.toFloat()
        val contentHeight =
            height - paddingTop - paddingBottom.toFloat()
        var left = paddingLeft.toFloat()
        var top = paddingTop.toFloat()
        if (contentWidth > contentHeight) {
            left += (contentWidth - contentHeight) / 2f
        } else {
            top += (contentHeight - contentWidth) / 2f
        }
        val diameter = Math.min(contentWidth, contentHeight)
        bounds[left, top, left + diameter] = top + diameter
    }

    private fun setupBitmap() {
        if (!mInitialized) {
            return
        }
        mBitmap = getBitmapFromDrawable(drawable)
        if (mBitmap == null) {
            return
        }
        mBitmapShader = BitmapShader(
            mBitmap!!,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP
        )
        mBitmapPaint.shader = mBitmapShader
        updateBitmapSize()
    }

    private fun updateBitmapSize() {
        if (mBitmap == null) return
        val dx: Float
        val dy: Float
        val scale: Float

        // scale up/down with respect to this view size and maintain aspect ratio
        // translate bitmap position with dx/dy to the center of the image
        if (mBitmap!!.width < mBitmap!!.height) {
            scale = mBitmapDrawBounds.width() / mBitmap!!.width.toFloat()
            dx = mBitmapDrawBounds.left
            dy =
                mBitmapDrawBounds.top - mBitmap!!.height * scale / 2f + mBitmapDrawBounds.width() / 2f
        } else {
            scale = mBitmapDrawBounds.height() / mBitmap!!.height.toFloat()
            dx =
                mBitmapDrawBounds.left - mBitmap!!.width * scale / 2f + mBitmapDrawBounds.width() / 2f
            dy = mBitmapDrawBounds.top
        }
        mShaderMatrix.setScale(scale, scale)
        mShaderMatrix.postTranslate(dx, dy)
        mBitmapShader!!.setLocalMatrix(mShaderMatrix)
    }

    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun isInCircle(x: Float, y: Float): Boolean {
        // find the distance between center of the view and x,y point
        val distance = Math.sqrt(
            Math.pow(
                mBitmapDrawBounds.centerX() - x.toDouble(),
                2.0
            ) + Math.pow(mBitmapDrawBounds.centerY() - y.toDouble(), 2.0)
        )
        return distance <= mBitmapDrawBounds.width() / 2
    }

    companion object {
        private const val DEF_PRESS_HIGHLIGHT_COLOR = 0x32000000
    }

    init {
        var strokeColor = Color.TRANSPARENT
        var strokeWidth = 0f
        var highlightEnable = true
        var highlightColor = DEF_PRESS_HIGHLIGHT_COLOR
        if (attrs != null) {
            val a = context.obtainStyledAttributes(
                attrs,
                R.styleable.CircleImageView,
                0,
                0
            )
            strokeColor = a.getColor(
                R.styleable.CircleImageView_strokeColor,
                Color.TRANSPARENT
            )
            strokeWidth = a.getDimensionPixelSize(
                R.styleable.CircleImageView_strokeWidth,
                0
            ).toFloat()
            highlightEnable =
                a.getBoolean(R.styleable.CircleImageView_highlightEnable, true)
            highlightColor = a.getColor(
                R.styleable.CircleImageView_highlightColor,
                DEF_PRESS_HIGHLIGHT_COLOR
            )
            a.recycle()
        }
        mShaderMatrix = Matrix()
        mBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mStrokeBounds = RectF()
        mBitmapDrawBounds = RectF()
        mStrokePaint.color = strokeColor
        mStrokePaint.style = Paint.Style.STROKE
        mStrokePaint.strokeWidth = strokeWidth
        mPressedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPressedPaint.color = highlightColor
        mPressedPaint.style = Paint.Style.FILL
        mHighlightEnable = highlightEnable
        mInitialized = true
        setupBitmap()
    }
}
