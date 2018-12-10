package com.test.gausslist.parallaxstyle;

import android.graphics.Canvas;
import android.widget.ImageView;

import com.test.gausslist.ScrollParallaxImageView;


/**
 * When the imageView is scrolling vertically, the image in imageView will be
 * also scrolling vertically if the image' height is bigger than imageView's height.
 * <p>
 * The image will not over scroll to it's view bounds.
 * <p>
 * Note: it only supports imageView with CENTER_CROP scale type.
 * <p>
 * Created by gjz on 25/11/2016.
 */

public class VerticalHeadMovingStyle implements ScrollParallaxImageView.ParallaxStyle {


//    public VerticalHeadMovingStyle() {
//    }

    @Override
    public void onAttachedToImageView(final ScrollParallaxImageView view) {
        // only supports CENTER_CROP
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    @Override
    public void onDetachedFromImageView(ScrollParallaxImageView view) {

    }

    @Override
    public void transform(ScrollParallaxImageView view, Canvas canvas, int itemY, int viewY) {
        if (view.getScaleType() != ImageView.ScaleType.CENTER_CROP) {
            return;
        }

        // image's width and height
        int iWidth = view.getDrawable().getIntrinsicWidth();
        int iHeight = view.getDrawable().getIntrinsicHeight();
        if (iWidth <= 0 || iHeight <= 0) {
            return;
        }

        // view's width and height
        int vWidth = view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();
        int vHeight = view.getHeight() - view.getPaddingTop() - view.getPaddingBottom();

        // device's height
        int dHeight = view.getResources().getDisplayMetrics().heightPixels;

        if (iWidth * vHeight < iHeight * vWidth) {
            float a = iWidth * 1.0f / vWidth;//缩放比例   截取图片的比例为1
            int scrollDistance = itemY - viewY;
            canvas.translate(0, scrollDistance / a);
        }
    }
}
