package com.dgunia.calllogpicker.list;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/**
 * Draws an arrow for incoming/outgoing calls.
 */
public class CallArrowDrawable extends Drawable {
    private final boolean incoming;
    private final boolean missed;

    public CallArrowDrawable(boolean incoming, boolean missed) {
        this.incoming = incoming;
        this.missed = missed;
    }

    @Override
    public void draw(Canvas canvas) {
        RectF rect = new RectF(getBounds());
        rect.inset(5, 5);

        Paint paint = new Paint();
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);

        paint.setColor(missed ? Color.RED : incoming ? Color.GREEN : Color.BLUE);
        canvas.drawLine(rect.right, rect.top, rect.left, rect.bottom, paint);

        if (incoming) {
            Path path = new Path();
            path.moveTo(rect.left, rect.top + rect.height() * 0.5f);
            path.lineTo(rect.left, rect.bottom);
            path.lineTo(rect.left + rect.width() * 0.5f, rect.bottom);
            canvas.drawPath(path, paint);
        } else {
            Path path = new Path();
            path.moveTo(rect.left + rect.width() * 0.5f, rect.top);
            path.lineTo(rect.right, rect.top);
            path.lineTo(rect.right, rect.top + rect.height() * 0.5f);
            canvas.drawPath(path, paint);
        }
    }

    @Override
    public void setAlpha(int i) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
