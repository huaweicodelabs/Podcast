/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.podcast.java.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;

import com.huawei.podcast.R;

public class CircleImageView extends AppCompatImageView {
    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = Constants.REQUEST_CODE_TWO;

    private static final int DEFAULT_BORDER_WIDTH = Constants.ZERO;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;
    private static final int DEFAULT_CIRCLE_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final int DEFAULT_IMAGE_ALPHA = Constants.DEFAULT_IMAGE_ALPHA;
    private static final boolean DEFAULT_BORDER_OVERLAY = false;

    private final RectF mDrawableRect = new RectF();
    private final RectF mBorderRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();
    private final Paint mCircleBackgroundPaint = new Paint();

    private int mBorderColor = DEFAULT_BORDER_COLOR;
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;
    private int mCircleBackgroundColor = DEFAULT_CIRCLE_BACKGROUND_COLOR;
    private int mImageAlpha = DEFAULT_IMAGE_ALPHA;

    private Bitmap mBitmap;
    private Canvas mBitmapCanvas;

    private float mDrawableRadius;
    private float mBorderRadius;

    private ColorFilter mColorFilter;

    private boolean mInitialized;
    private boolean mRebuildShader;
    private boolean mDrawableDirty;

    private boolean mBorderOverlay;
    private boolean mDisableCircularTransformation;

    public CircleImageView(Context context) {
        super(context);

        init();
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, Constants.ZERO);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, Constants.ZERO);

        mBorderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_strokeWidth, DEFAULT_BORDER_WIDTH);
        mBorderColor = a.getColor(R.styleable.CircleImageView_strokeColor, DEFAULT_BORDER_COLOR);
        mBorderOverlay = a.getBoolean(R.styleable.CircleImageView_highlightEnable, DEFAULT_BORDER_OVERLAY);
        mCircleBackgroundColor =
                a.getColor(R.styleable.CircleImageView_highlightColor, DEFAULT_CIRCLE_BACKGROUND_COLOR);

        a.recycle();

        init();
    }

    private void init() {
        mInitialized = true;

        super.setScaleType(SCALE_TYPE);

        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setDither(true);
        mBitmapPaint.setFilterBitmap(true);
        mBitmapPaint.setAlpha(mImageAlpha);
        mBitmapPaint.setColorFilter(mColorFilter);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        mCircleBackgroundPaint.setStyle(Paint.Style.FILL);
        mCircleBackgroundPaint.setAntiAlias(true);
        mCircleBackgroundPaint.setColor(mCircleBackgroundColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new OutlineProvider());
        }
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format(Constants.SCALE_FORMAT, scaleType));
        }
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (adjustViewBounds) {
            throw new IllegalArgumentException(Constants.EXCEPTION);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDisableCircularTransformation) {
            super.onDraw(canvas);
            return;
        }

        if (mCircleBackgroundColor != Color.TRANSPARENT) {
            canvas.drawCircle(
                    mDrawableRect.centerX(), mDrawableRect.centerY(), mDrawableRadius, mCircleBackgroundPaint);
        }

        if (mBitmap != null) {
            if (mDrawableDirty && mBitmapCanvas != null) {
                mDrawableDirty = false;
                Drawable drawable = getDrawable();
                drawable.setBounds(Constants.ZERO, Constants.ZERO, mBitmapCanvas.getWidth(), mBitmapCanvas.getHeight());
                drawable.draw(mBitmapCanvas);
            }

            if (mRebuildShader) {
                mRebuildShader = false;

                BitmapShader bitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                bitmapShader.setLocalMatrix(mShaderMatrix);

                mBitmapPaint.setShader(bitmapShader);
            }

            canvas.drawCircle(mDrawableRect.centerX(), mDrawableRect.centerY(), mDrawableRadius, mBitmapPaint);
        }

        if (mBorderWidth > Constants.ZERO) {
            canvas.drawCircle(mBorderRect.centerX(), mBorderRect.centerY(), mBorderRadius, mBorderPaint);
        }
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable dr) {
        mDrawableDirty = true;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateDimensions();
        invalidate();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        updateDimensions();
        invalidate();
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
        updateDimensions();
        invalidate();
    }

    public void setCircleBackgroundColor(@ColorInt int circleBackgroundColor) {
        if (circleBackgroundColor == mCircleBackgroundColor) {
            return;
        }

        mCircleBackgroundColor = circleBackgroundColor;
        mCircleBackgroundPaint.setColor(circleBackgroundColor);
        invalidate();
    }

    /**
     * @deprecated Use {@link #setCircleBackgroundColor(int)} instead
     */
    @Deprecated
    public void setCircleBackgroundColorResource(@ColorRes int circleBackgroundRes) {
        setCircleBackgroundColor(getContext().getResources().getColor(circleBackgroundRes));
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        initializeBitmap();
        invalidate();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        initializeBitmap();
        invalidate();
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        initializeBitmap();
        invalidate();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        initializeBitmap();
        invalidate();
    }

    @Override
    public void setImageAlpha(int alpha) {
        alpha &= 0xFF;

        if (alpha == mImageAlpha) {
            return;
        }

        mImageAlpha = alpha;

        // This might be called during ImageView construction before
        // member initialization has finished on API level >= 16.
        if (mInitialized) {
            mBitmapPaint.setAlpha(alpha);
            invalidate();
        }
    }

    @Override
    public int getImageAlpha() {
        return mImageAlpha;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (cf == mColorFilter) {
            return;
        }

        mColorFilter = cf;

        // This might be called during ImageView construction before
        // member initialization has finished on API level <= 19.
        if (mInitialized) {
            mBitmapPaint.setColorFilter(cf);
            invalidate();
        }
    }

    @Override
    public ColorFilter getColorFilter() {
        return mColorFilter;
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap =
                        Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(Constants.ZERO, Constants.ZERO, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initializeBitmap() {
        mBitmap = getBitmapFromDrawable(getDrawable());

        if (mBitmap != null && mBitmap.isMutable()) {
            mBitmapCanvas = new Canvas(mBitmap);
        } else {
            mBitmapCanvas = null;
        }

        if (!mInitialized) {
            return;
        }

        if (mBitmap != null) {
            updateShaderMatrix();
        } else {
            mBitmapPaint.setShader(null);
        }
    }

    private void updateDimensions() {
        mBorderRect.set(calculateBounds());
        mBorderRadius =
                Math.min((mBorderRect.height() - mBorderWidth) / 2.0f, (mBorderRect.width() - mBorderWidth) / 2.0f);

        mDrawableRect.set(mBorderRect);
        if (!mBorderOverlay && mBorderWidth > Constants.ZERO) {
            mDrawableRect.inset(mBorderWidth - 1.0f, mBorderWidth - 1.0f);
        }
        mDrawableRadius = Math.min(mDrawableRect.height() / 2.0f, mDrawableRect.width() / 2.0f);

        updateShaderMatrix();
    }

    private RectF calculateBounds() {
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        int sideLength = Math.min(availableWidth, availableHeight);

        float left = getPaddingLeft() + (availableWidth - sideLength) / 2f;
        float top = getPaddingTop() + (availableHeight - sideLength) / 2f;

        return new RectF(left, top, left + sideLength, top + sideLength);
    }

    private void updateShaderMatrix() {
        if (mBitmap == null) {
            return;
        }

        float scale;
        float dx = Constants.ZERO;
        float dy = Constants.ZERO;

        mShaderMatrix.set(null);

        int bitmapHeight = mBitmap.getHeight();
        int bitmapWidth = mBitmap.getWidth();

        if (bitmapWidth * mDrawableRect.height() > mDrawableRect.width() * bitmapHeight) {
            scale = mDrawableRect.height() / (float) bitmapHeight;
            dx = (mDrawableRect.width() - bitmapWidth * scale) * 0.5f;
        } else {
            scale = mDrawableRect.width() / (float) bitmapWidth;
            dy = (mDrawableRect.height() - bitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + mDrawableRect.left, (int) (dy + 0.5f) + mDrawableRect.top);

        mRebuildShader = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDisableCircularTransformation) {
            return super.onTouchEvent(event);
        }

        return inTouchableArea(event.getX(), event.getY()) && super.onTouchEvent(event);
    }

    private boolean inTouchableArea(float x, float y) {
        if (mBorderRect.isEmpty()) {
            return true;
        }

        return Math.pow(x - mBorderRect.centerX(), 2) + Math.pow(y - mBorderRect.centerY(), 2)
                <= Math.pow(mBorderRadius, 2);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class OutlineProvider extends ViewOutlineProvider {
        @Override
        public void getOutline(View view, Outline outline) {
            if (mDisableCircularTransformation) {
                ViewOutlineProvider.BACKGROUND.getOutline(view, outline);
            } else {
                Rect bounds = new Rect();
                mBorderRect.roundOut(bounds);
                outline.setRoundRect(bounds, bounds.width() / 2.0f);
            }
        }
    }
}
