package com.crobot.core.drive;

import android.os.Build;

import com.crobot.core.infra.tool.Device;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;
import com.crobot.runtime.engine.apt.anno.Value;

public class DeviceInitiator implements Initiator {
    private Device device;

    public DeviceInitiator(Device device) {
        this.device = device;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Device", new ObjApt() {
            @Value("buildId")
            private String buildId = Build.DISPLAY;
            @Value("board")
            private String board = Build.BOARD;
            @Value("brand")
            private String brand = Build.BRAND;
            @Value("device")
            private String device = Build.DEVICE;
            @Value("model")
            private String model = Build.MODEL;
            @Value("product")
            private String product = Build.PRODUCT;
            @Value("hardWard")
            private String hardWard = Build.HARDWARE;

            @Caller("getWidth")
            public Number getWidth() {
                return DeviceInitiator.this.device.getWidth();
            }

            @Caller("getHeight")
            public Number getHeight() {
                return DeviceInitiator.this.device.getHeight();
            }


        });
    }
}
