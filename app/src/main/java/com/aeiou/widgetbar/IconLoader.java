package com.aeiou.widgetbar;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

final class IconLoader {
    private IconLoader() {}

    static Bitmap load(Context context, String packageName, int sizePx, String fallbackText) {
        try {
            Drawable drawable = context.getPackageManager().getApplicationIcon(packageName);
            return toBitmap(drawable, sizePx);
        } catch (PackageManager.NameNotFoundException ignored) {
            return fallback(sizePx, fallbackText);
        }
    }

    private static Bitmap toBitmap(Drawable drawable, int sizePx) {
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, sizePx, sizePx);
        drawable.draw(canvas);
        return bitmap;
    }

    private static Bitmap fallback(int sizePx, String text) {
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint circle = new Paint(Paint.ANTI_ALIAS_FLAG);
        circle.setColor(Color.rgb(35, 48, 54));
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, circle);

        Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
        label.setColor(Color.WHITE);
        label.setTextAlign(Paint.Align.CENTER);
        label.setTextSize(sizePx * 0.34f);
        label.setFakeBoldText(true);
        Paint.FontMetrics metrics = label.getFontMetrics();
        float y = sizePx / 2f - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(text, sizePx / 2f, y, label);
        return bitmap;
    }
}
