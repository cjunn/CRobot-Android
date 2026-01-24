package com.crobot.core.drive;

import com.crobot.core.infra.tool.FileOperation;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;

public class AttachInitiator implements Initiator {
    private FileOperation attachFile;

    public AttachInitiator(FileOperation attachFile) {
        this.attachFile = attachFile;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("Attach", new FileOperationApt(this.attachFile));
    }


}