package com.crobot.core.drive;

import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.PorterDuff;

import com.crobot.core.infra.tool.DisplayBitmap;
import com.crobot.core.infra.tool.DisplayCanvas;
import com.crobot.core.infra.tool.ScreenMetrics;
import com.crobot.runtime.engine.Context;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.Varargs;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

public class CanvasInitiator implements Initiator {
    private DisplayCanvas displayCanvas;
    private ScreenMetrics screenMetrics;

    public CanvasInitiator(DisplayCanvas displayCanvas, ScreenMetrics screenMetrics) {
        this.displayCanvas = displayCanvas;
        this.screenMetrics = screenMetrics;
    }


    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Canvas", new ObjApt() {
            @Caller("create")
            public DisplayBitmapApt create(Varargs varargs) {
                Number w = varargs.getValue(0, Number.class, () -> screenMetrics.getWidth());
                Number h = varargs.getValue(1, Number.class, () -> screenMetrics.getHeight());
                return new DisplayBitmapApt(displayCanvas.newCanvas(w.intValue(), h.intValue()));
            }
        });
    }

    public class DisplayBitmapApt extends ObjApt {
        private DisplayBitmap displayBitmap;

        public DisplayBitmapApt(DisplayBitmap displayBitmap) {
            this.displayBitmap = displayBitmap;
        }

        // 绘制颜色相关
        @Caller("drawRGB")
        public void drawRGB(Number r, Number g, Number b) {
            displayBitmap.drawRGB(r.intValue(), g.intValue(), b.intValue());
        }

        @Caller("drawARGB")
        public void drawARGB(Number a, Number r, Number g, Number b) {
            displayBitmap.drawARGB(a.intValue(), r.intValue(), g.intValue(), b.intValue());
        }

        @Caller("drawColor")
        public void drawColor(Number color) {
            displayBitmap.drawColor(color.intValue());
        }

        @Caller("drawColorWithMode")
        public void drawColorWithMode(Number color, Number mode) {
            displayBitmap.drawColor(color.intValue(), PorterDuff.Mode.values()[mode.intValue()]);
        }

        @Caller("drawPaint")
        public void drawPaint() {
            displayBitmap.drawPaint();
        }

        // 绘制几何图形
        @Caller("drawPoint")
        public void drawPoint(Number x, Number y) {
            displayBitmap.drawPoint(x.floatValue(), y.floatValue());
        }

        @Caller("drawPoints")
        public void drawPoints(float[] pts) {
            displayBitmap.drawPoints(pts);
        }

        @Caller("drawLine")
        public void drawLine(Number startX, Number startY, Number stopX, Number stopY) {
            displayBitmap.drawLine(startX.floatValue(), startY.floatValue(), stopX.floatValue(), stopY.floatValue());
        }

        @Caller("drawText")
        public void drawText(Number startX, Number startY, String text) {
            displayBitmap.drawText(startX.floatValue(), startY.floatValue(), text);
        }


        @Caller("drawLines")
        public void drawLines(float[] pts) {
            displayBitmap.drawLines(pts);
        }

        @Caller("drawRect")
        public void drawRect(Number left, Number top, Number right, Number bottom) {
            displayBitmap.drawRect(left.floatValue(), top.floatValue(), right.floatValue(), bottom.floatValue());
        }

        @Caller("drawRotateRect")
        public void drawRotateRect(Number centerX, Number centerY, Number width, Number height, Number degree) {
            displayBitmap.drawRotateRect(centerX.floatValue(), centerY.floatValue(), width.floatValue(), height.floatValue(), degree.floatValue());
        }


        @Caller("drawOval")
        public void drawOval(Number left, Number top, Number right, Number bottom) {
            displayBitmap.drawOval(left.floatValue(), top.floatValue(), right.floatValue(), bottom.floatValue());
        }

        @Caller("drawCircle")
        public void drawCircle(Number cx, Number cy, Number radius) {
            displayBitmap.drawCircle(cx.floatValue(), cy.floatValue(), radius.floatValue());
        }

        @Caller("drawArc")
        public void drawArc(Number left, Number top, Number right, Number bottom, Number startAngle, Number sweepAngle, Boolean useCenter) {
            displayBitmap.drawArc(left.floatValue(), top.floatValue(), right.floatValue(), bottom.floatValue(),
                    startAngle.floatValue(), sweepAngle.floatValue(), useCenter);
        }

        @Caller("drawRoundRect")
        public void drawRoundRect(Number left, Number top, Number right, Number bottom, Number rx, Number ry) {
            displayBitmap.drawRoundRect(left.floatValue(), top.floatValue(), right.floatValue(), bottom.floatValue(),
                    rx.floatValue(), ry.floatValue());
        }

        // 绘制路径
        @Caller("drawPath")
        public void drawPath(Object pathObj) {
            // Path对象需要从外部传入，这里假设传入的是Path对象
            if (pathObj instanceof Path) {
                displayBitmap.drawPath((Path) pathObj);
            }
        }

        // 绘制文字
        @Caller("drawText")
        public void drawText(String text, Number x, Number y) {
            displayBitmap.drawText(text, x.floatValue(), y.floatValue());
        }

        // 绘制图片
        @Caller("drawBitmap")
        public void drawBitmap(Object bitmapObj, Number left, Number top) {
            if (bitmapObj instanceof Bitmap) {
                displayBitmap.drawBitmap((Bitmap) bitmapObj, left.floatValue(), top.floatValue());
            }
        }

        // 获取画布大小
        @Caller("getWidth")
        public Number getWidth() {
            return displayBitmap.getWidth();
        }

        @Caller("getHeight")
        public Number getHeight() {
            return displayBitmap.getHeight();
        }

        // 矩阵变换
        @Caller("translate")
        public void translate(Number dx, Number dy) {
            displayBitmap.translate(dx.floatValue(), dy.floatValue());
        }

        @Caller("scale")
        public void scale(Number sx, Number sy) {
            displayBitmap.scale(sx.floatValue(), sy.floatValue());
        }

        @Caller("scaleWithPivot")
        public void scaleWithPivot(Number sx, Number sy, Number px, Number py) {
            displayBitmap.scale(sx.floatValue(), sy.floatValue(), px.floatValue(), py.floatValue());
        }

        @Caller("rotate")
        public void rotate(Number degrees) {
            displayBitmap.rotate(degrees.floatValue());
        }

        @Caller("rotateWithPivot")
        public void rotateWithPivot(Number degrees, Number px, Number py) {
            displayBitmap.rotate(degrees.floatValue(), px.floatValue(), py.floatValue());
        }

        @Caller("skew")
        public void skew(Number sx, Number sy) {
            displayBitmap.skew(sx.floatValue(), sy.floatValue());
        }

        // Paint 设置
        @Caller("setStrokeWidth")
        public void setStrokeWidth(Number width) {
            displayBitmap.setStrokeWidth(width.floatValue());
        }

        @Caller("setTextSize")
        public void setTextSize(Number textSize) {
            displayBitmap.setTextSize(textSize.floatValue());
        }

        @Caller("setAntiAlias")
        public void setAntiAlias(Boolean aa) {
            displayBitmap.setAntiAlias(aa);
        }

        @Caller("setStyle")
        public void setStyle(Number style) {
            displayBitmap.setStyle(style.intValue());
        }

        @Caller("setColor")
        public void setColor(Number color) {
            displayBitmap.setColor(color.intValue());
        }

        @Caller("remove")
        public void remove() {
            displayCanvas.removeCanvas(displayBitmap);
        }

        @Caller("invalidate")
        public void invalidate() {
            displayCanvas.invalidate();
        }

        @Override
        public int onGc(Context context) {
            this.remove();
            return super.onGc(context);
        }
    }


}
