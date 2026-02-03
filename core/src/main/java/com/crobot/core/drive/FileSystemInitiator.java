package com.crobot.core.drive;

import com.crobot.core.infra.tool.FileOperationFactory;
import com.crobot.runtime.engine.Context;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.Varargs;
import com.crobot.runtime.engine.apt.FuncApt;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;
import com.crobot.runtime.engine.apt.anno.Execute;

import java.io.File;

public class FileSystemInitiator implements Initiator {
    private FileOperationFactory fileOperationFactory;

    public FileSystemInitiator(FileOperationFactory fileOperationFactory) {
        this.fileOperationFactory = fileOperationFactory;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setFuncApt("getFileSystem", new FuncApt() {
            @Execute
            public FileOperationApt getModule(String root) {
                return new FileOperationApt(fileOperationFactory.getModule(root));
            }
        });
    }


}
