package com.example.blaylist;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * An ImageView whose picture is framed inside a pointed desert arch with a golden border.
 */
public class ArchImageView extends AppCompatImageView {

    private final Path arch_path = new Path();
    private final Paint stroke_paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ArchImageView(@NonNull Context context) {
        this(context, null);
    }

    public ArchImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArchImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArchImageView, defStyleAttr, 0);
        stroke_paint.setStyle(Paint.Style.STROKE);
        stroke_paint.setStrokeJoin(Paint.Join.ROUND);
        stroke_paint.setColor(a.getColor(R.styleable.ArchImageView_archStrokeColor, Color.TRANSPARENT));
        stroke_paint.setStrokeWidth(a.getDimension(R.styleable.ArchImageView_archStrokeWidth, 0f));
        a.recycle();

        // the shadow (elevation) follows the arch instead of a rectangle
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (android.os.Build.VERSION.SDK_INT >= 30) {
                    outline.setPath(arch_path);
                } else {
                    float radius = view.getWidth() / 2f;
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight() + (int) radius, radius);
                }
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float inset = stroke_paint.getStrokeWidth() / 2f;
        float left = inset, top = inset, right = w - inset, bottom = h - inset;
        float width = right - left;
        float shoulder = top + Math.min((bottom - top) * 0.45f, width * 0.62f);
        float mid = left + width / 2f;

        arch_path.reset();
        arch_path.moveTo(left, bottom);
        arch_path.lineTo(left, shoulder);
        arch_path.cubicTo(left, top + (shoulder - top) * 0.45f, mid - width * 0.22f, top + (shoulder - top) * 0.12f, mid, top);
        arch_path.cubicTo(mid + width * 0.22f, top + (shoulder - top) * 0.12f, right, top + (shoulder - top) * 0.45f, right, shoulder);
        arch_path.lineTo(right, bottom);
        arch_path.close();
        invalidateOutline();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(arch_path);
        super.onDraw(canvas);
        canvas.restoreToCount(save);
        if (stroke_paint.getStrokeWidth() > 0) {
            canvas.drawPath(arch_path, stroke_paint);
        }
    }
}
