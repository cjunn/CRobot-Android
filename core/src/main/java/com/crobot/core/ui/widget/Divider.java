package com.crobot.core.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;

import com.crobot.core.ui.core.SimpleUISupport;

import java.util.Map;

public class Divider extends SimpleUISupport<Divider.DividerView> {
    public Divider(Context context, Map<String, String> attr) {
        super(context, attr);
    }

    @Override
    protected DividerView initView(Context context) {
        return new DividerView(context);
    }


    public static class DividerView extends androidx.appcompat.widget.AppCompatTextView {
        private Paint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private int textWidth;
        private int splitWidth;
        private int padding = 5;
        private String text = "";

        public DividerView(Context context) {
            super(context);
            this.setText("");
        }

        @Override
        public void setText(CharSequence _text, BufferType type) {
            super.setText(text, type);
            this.text = (String) _text;
            paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paint.setAntiAlias(true);
            paint.setTextSize(this.getTextSize());
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            textWidth = bounds.width(); // 获取字体的宽度
            paint.getTextBounds("=", 0, 1, bounds);
            splitWidth = bounds.width(); // 获取字体的宽度
            this.requestLayout();
        }

        private String getSplit(int length) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append("=");
            }
            return builder.toString();
        }

        private int getSplitSize(Canvas canvas) {
            int i = ((canvas.getWidth() - textWidth - 20) / (splitWidth + 2));
            return i / 2;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            String split = getSplit(getSplitSize(canvas));
            canvas.drawText(split, 0, canvas.getHeight() - padding, paint);
            float x = canvas.getWidth() - paint.measureText(split);  // 右边界位置
            canvas.drawText(split, x, canvas.getHeight() - padding, paint);
            x = (canvas.getWidth() - textWidth) / 2;
            canvas.drawText(text, x, canvas.getHeight() - padding - 2, paint);
        }
    }


}
