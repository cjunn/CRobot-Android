package com.crobot.core.drive;

import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

public class TextInitiator implements Initiator {
    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Text", new TextApt());
    }

    public static class TextApt extends ObjApt {
        @Caller("encode")
        public String encode(byte[] data, String charsetName) {
            return new String(data, Charset.forName(charsetName));
        }

        @Caller("decode")
        public byte[] decode(String data, String charsetName) {
            return data.getBytes(Charset.forName(charsetName));
        }
    }

}
