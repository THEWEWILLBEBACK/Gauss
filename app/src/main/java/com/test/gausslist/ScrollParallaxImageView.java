package com.test.gausslist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

import com.test.gausslist.parallaxstyle.VerticalHeadMovingStyle;


/**
 * Created by gjz on 25/11/2016.
 */

public class ScrollParallaxImageView extends AppCompatImageView implements ViewTreeObserver.OnScrollChangedListener {
    private int[] viewLocation = new int[2];
    private boolean enableScrollParallax = true;

    private DrawPosType mDrawPosType;
    private ParallaxStyle parallaxStyles;
    private int mParentHeight;
    private int mHeight;

    public ScrollParallaxImageView(Context context) {
        this(context, null);
    }

    public ScrollParallaxImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollParallaxImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (getParent() != null) {
                    ViewParent parent = getParent().getParent();
                    if (parent != null) {
                        mParentHeight = ((View) parent).getMeasuredHeight();
                    }
                }
                mHeight = getMeasuredHeight();
            }
        });
    }


    public void setDrawPosType(DrawPosType drawPosType) {
        mDrawPosType = drawPosType;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!enableScrollParallax || getDrawable() == null) {
            switch (mDrawPosType) {
                case END:
                    //重置一下画布
                    canvas.save();
                    canvas.restore();
                    canvas.translate(0, -(mParentHeight - mHeight) / 2);
                    break;
                case START:
                    //重置一下画布
                    canvas.save();
                    canvas.restore();
                    canvas.translate(0, (mParentHeight - mHeight) / 2);
                    break;

            }
            super.onDraw(canvas);
            return;
        }
        if (DrawPosType.SLIDE == mDrawPosType || DrawPosType.END == mDrawPosType) {
            //  notice    因為只支持centerCrop，所以手动偏移一下canvas
            canvas.translate(0, (mParentHeight - mHeight) / 2);
            if (parallaxStyles != null) {
                getLocationInWindow(viewLocation);
                if (parallaxStyles instanceof VerticalHeadMovingStyle) {
                    if (getParent() != null) {//  notice    这里取的parent是每个item的条目布局，布局修改了要变化
                        ViewParent viewParent = getParent().getParent();
                        @SuppressLint("DrawAllocation")
                        int[] outLocationItem = new int[2];
                        ((View) viewParent).getLocationInWindow(outLocationItem);
                        parallaxStyles.transform(this, canvas, outLocationItem[1], viewLocation[1]);
                    }
                } else {
                    parallaxStyles.transform(this, canvas, viewLocation[0], viewLocation[1]);
                }
            }
        }
        super.onDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnScrollChangedListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        getViewTreeObserver().removeOnScrollChangedListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onScrollChanged() {
        if (enableScrollParallax) {
            invalidate();
        }
    }

    public void setParallaxStyles(ParallaxStyle styles) {
        if (parallaxStyles != null) {
            parallaxStyles.onDetachedFromImageView(this);
        }
        parallaxStyles = styles;
        parallaxStyles.onAttachedToImageView(this);
    }

    public void setEnableScrollParallax(boolean enableScrollParallax) {
        this.enableScrollParallax = enableScrollParallax;
        if (!enableScrollParallax) {
            postInvalidate();
        }
    }

    public interface ParallaxStyle {
        void onAttachedToImageView(ScrollParallaxImageView view);

        void onDetachedFromImageView(ScrollParallaxImageView view);

        void transform(ScrollParallaxImageView view, Canvas canvas, int x, int y);
    }

    public enum DrawPosType {
        START, END, SLIDE
    }
}
