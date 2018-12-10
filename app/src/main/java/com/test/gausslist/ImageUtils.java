package com.test.gausslist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

/**
 * Created by Xiejq on 2018/5/4.
 */

public class ImageUtils {

    public static Bitmap gaussBlur(Context context, Bitmap source, @IntRange(from = 0, to = 25) int radius) {
        Bitmap inputBmp = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight());
        RenderScript renderScript = RenderScript.create(context);
        // Allocate memory for Renderscript to work with
        final Allocation input = Allocation.createFromBitmap(renderScript, inputBmp);
        final Allocation output = Allocation.createTyped(renderScript, input.getType());
        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        scriptIntrinsicBlur.setInput(input);
        // Set the blur radius
        scriptIntrinsicBlur.setRadius(radius);
        // Start the ScriptIntrinisicBlur
        scriptIntrinsicBlur.forEach(output);
        // Copy the output to the blurred bitmap
        output.copyTo(inputBmp);
        renderScript.destroy();
        return inputBmp;
    }

    /**
     * 在bitmap上铺上一层0xcc000000的bitmap
     *
     * @param background
     * @return
     */
    public static Bitmap toConformBitmap(Bitmap background, String color) {
        if (background == null) {
            return null;
        }
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        Bitmap foreground = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        foreground.eraseColor(Color.parseColor(color));//填充颜色
        // 创建一个新的和SRC长度宽度一样的位图
        Bitmap newbmp = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newbmp);
        //draw bg into
        cv.drawBitmap(background, 0, 0, null);
        // 在 0，0坐标开始画入bg
        // draw fg into
        cv.drawBitmap(foreground, 0, 0, null);
        // 在 0，0坐标开始画入fg ，可以从任意位置画入
        // save all clip
        cv.save(Canvas.ALL_SAVE_FLAG);
        // 保存store
        cv.restore();
        // 存储
        recycleBitmap(background);
        recycleBitmap(foreground);
        return newbmp;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }


    public static void recycleBitmap(@NonNull Bitmap source){
        if (!source.isRecycled()) {
            source.recycle();
            source = null;
            System.gc();
        }
    }


}
