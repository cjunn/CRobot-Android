package com.crobot.core.infra.tool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

public class DisplayBitmap {
    public Bitmap bitmap;
    public Canvas canvas;
    public Paint paint;

    public DisplayBitmap(int w, int h) {
        paint = new Paint();
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    private static float[] calculateVertices(float centerX, float centerY, float width, float height, float degree) {
        float[] vertices = new float[8];
        // 角度转弧度（Java三角函数需用弧度）
        double radian = Math.toRadians(degree);
        float cos = (float) Math.cos(radian);
        float sin = (float) Math.sin(radian);

        // 计算半宽、半高（中心到边的偏移量）
        float halfW = width / 2;
        float halfH = height / 2;

        // 计算四个顶点（旋转公式：x' = x*cos - y*sin；y' = x*sin + y*cos，再平移到中心）
        // 顶点1：(-halfW, -halfH) 旋转后 + 中心坐标
        vertices[0] = centerX + (-halfW * cos - (-halfH) * sin);
        vertices[1] = centerY + (-halfW * sin + (-halfH) * cos);
        // 顶点2：(halfW, -halfH) 旋转后 + 中心坐标
        vertices[2] = centerX + (halfW * cos - (-halfH) * sin);
        vertices[3] = centerY + (halfW * sin + (-halfH) * cos);
        // 顶点3：(halfW, halfH) 旋转后 + 中心坐标
        vertices[4] = centerX + (halfW * cos - halfH * sin);
        vertices[5] = centerY + (halfW * sin + halfH * cos);
        // 顶点4：(-halfW, halfH) 旋转后 + 中心坐标
        vertices[6] = centerX + (-halfW * cos - halfH * sin);
        vertices[7] = centerY + (-halfW * sin + halfH * cos);

        return vertices;
    }

    private static List<Float> te(float[] vertices) {
        List<Float> re = new ArrayList<>();
        for (float f : vertices) {
            re.add(f);
        }
        return re;
    }

    private static Path createRotatedRectPath(float centerX, float centerY, float width, float height, float degree) {
        Path path = new Path();
        float[] vertices = calculateVertices(centerX, centerY, width, height, degree);
        // 构建Path：连接四个顶点并闭合
        path.moveTo(vertices[0], vertices[1]);
        path.lineTo(vertices[2], vertices[3]);
        path.lineTo(vertices[4], vertices[5]);
        path.lineTo(vertices[6], vertices[7]);
        path.close();
        return path;
    }

    @Override
    protected void finalize() throws Throwable {
        bitmap.recycle();
    }

    public void setTextSize(float textSize) {
        paint.setTextSize(textSize);
    }

    public void drawLine(float startX, float startY, float stopX, float stopY) {
        canvas.drawLine(startX, startY, stopX, stopY, paint);
    }

    public void drawText(float startX, float startY, String text) {
        canvas.drawText(text, startX, startY, paint);
    }

    public void drawText(String text, float x, float y) {
        canvas.drawText(text, x, y, paint);
    }

    public void drawRect(float startX, float startY, float stopX, float stopY) {
        canvas.drawRect(startX, startY, stopX, stopY, paint);
    }

    public void drawPoint(float x, float y) {
        canvas.drawPoint(x, y, paint);
    }

    public void drawPoints(float[] pts) {
        canvas.drawPoints(pts, paint);
    }

    public void drawLines(float[] pts) {
        canvas.drawLines(pts, paint);
    }

    public void drawRGB(int r, int g, int b) {
        canvas.drawRGB(r, g, b);
    }

    public void drawARGB(int a, int r, int g, int b) {
        canvas.drawARGB(a, r, g, b);
    }

    public void drawColor(int color) {
        canvas.drawColor(color);
    }

    public void drawColor(int color, PorterDuff.Mode mode) {
        canvas.drawColor(color, mode);
    }

    public void drawPaint() {
        canvas.drawPaint(paint);
    }

    public void drawRotateRect(float centerX, float centerY, float width, float height, float degree) {
        canvas.drawPath(createRotatedRectPath(centerX, centerY, width, height, degree), paint); // 绘制轮廓
    }

    public void drawOval(float left, float top, float right, float bottom) {
        canvas.drawOval(new RectF(left, top, right, bottom), paint);
    }

    public void drawCircle(float cx, float cy, float radius) {
        canvas.drawCircle(cx, cy, radius, paint);
    }

    public void drawArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean useCenter) {
        canvas.drawArc(new RectF(left, top, right, bottom), startAngle, sweepAngle, useCenter, paint);
    }

    public void drawRoundRect(float left, float top, float right, float bottom, float rx, float ry) {
        canvas.drawRoundRect(new RectF(left, top, right, bottom), rx, ry, paint);
    }

    public void drawPath(Path path) {
        canvas.drawPath(path, paint);
    }

    public void drawBitmap(Bitmap bitmap, float left, float top) {
        canvas.drawBitmap(bitmap, left, top, paint);
    }

    public void setStyle(int style) {
        Paint.Style[] values = Paint.Style.values();
        if (style >= values.length) {
            return;
        }
        paint.setStyle(Paint.Style.values()[style]);
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public void setStrokeWidth(float width) {
        paint.setStrokeWidth(width);
    }


    public void setAntiAlias(boolean aa) {
        paint.setAntiAlias(aa);
    }

    // 获取画布大小
    public int getWidth() {
        return canvas.getWidth();
    }

    public int getHeight() {
        return canvas.getHeight();
    }

    // 矩阵变换
    public void translate(float dx, float dy) {
        canvas.translate(dx, dy);
    }

    public void scale(float sx, float sy) {
        canvas.scale(sx, sy);
    }

    public void scale(float sx, float sy, float px, float py) {
        canvas.scale(sx, sy, px, py);
    }

    public void rotate(float degrees) {
        canvas.rotate(degrees);
    }

    public void rotate(float degrees, float px, float py) {
        canvas.rotate(degrees, px, py);
    }

    public void skew(float sx, float sy) {
        canvas.skew(sx, sy);
    }
}
