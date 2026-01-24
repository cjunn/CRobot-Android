package com.crobot.core.drive;

import com.crobot.core.util.Base64Util;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

public class Base64Initiator implements Initiator {

    public Base64Initiator() {
    }

    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Base64", new ObjApt() {
            @Caller("encode")
            public String encode(byte[] data) {
                try {
                    return Base64Util.encode(data);
                } catch (Exception e) {
                    return null;
                }
            }

            @Caller("decode")
            public byte[] decode(String str) {
                try {
                    return Base64Util.decode(str);
                } catch (Exception e) {
                    return null;
                }
            }
        });
    }


}