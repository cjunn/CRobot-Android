package com.crobot.core.drive;

import android.graphics.Bitmap;

import com.crobot.core.util.Base64Util;
import com.crobot.runtime.engine.Context;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;
import com.crobot.utils.BitmapUtil;

public class BitmapApt extends ObjApt {

    private Bitmap bitmap;

    public BitmapApt(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    @Caller("rect")
    public BitmapApt rect(Number sx1, Number sy1, Number sx2, Number sy2) {
        return new BitmapApt(BitmapUtil.rect(bitmap, sx1.intValue(), sy1.intValue(), sx2.intValue(), sy2.intValue()));
    }

    @Caller("rotation")
    public BitmapApt rotation(Number degree) {
        return new BitmapApt(BitmapUtil.rotation(bitmap, degree.intValue()));
    }

    @Caller("scale")
    public BitmapApt scale(Number scale) {
        return new BitmapApt(BitmapUtil.scale(bitmap, scale.intValue()));
    }

    @Caller("toBytes")
    public byte[] toBytes() {
        return BitmapUtil.bitmap2Byte(bitmap);
    }

    @Caller("toBase64")
    public String toBase64() {
        return Base64Util.encode(BitmapUtil.bitmap2Byte(bitmap));
    }


    @Override
    public int onGc(Context context) {
        this.bitmap.recycle();
        return super.onGc(context);
    }
}
