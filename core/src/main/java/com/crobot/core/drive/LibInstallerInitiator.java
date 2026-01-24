package com.crobot.core.drive;

import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

import java.io.File;

public class LibInstallerInitiator implements Initiator {
    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("LibInstaller", new ObjApt() {
            @Caller("install")
            public boolean install(String libPath) {
                if (!new File(libPath).exists()) {
                    return false;
                }
                System.load(libPath);
                return true;
            }
        });
    }
}
