package com.crobot.core.drive.lib;

import android.graphics.Bitmap;

import com.crobot.core.util.FileUtil;
import com.crobot.runtime.engine.ContextException;

import java.io.File;
import java.util.List;

public class YoloV8 {
    private long yoloV8Ptr;

    private void checkFile(String path, String name) {
        if (!new File(path).exists()) {
            throw new ContextException(String.format("The file %s cannot be located", name));
        }
    }

    public void init(String soPath, String param, String bin, int type, int target_size, boolean useGpu) {
        FileUtil.checkFile(soPath, "soPath");
        FileUtil.checkFile(soPath, "param");
        FileUtil.checkFile(soPath, "bin");
        YoloV8Bridge.initEnv(soPath);
        yoloV8Ptr = YoloV8Bridge.newYoloV8(param, bin, type, target_size, useGpu);
    }

    public List detect(Bitmap input) {
        if (yoloV8Ptr == 0) {
            throw new ContextException("YoloV8 has not been initialized yet!");
        }
        return YoloV8Bridge.detect(yoloV8Ptr, input);
    }


    public void close() {
        if (yoloV8Ptr == 0) {
            return;
        }
        YoloV8Bridge.close(yoloV8Ptr);
    }
}
