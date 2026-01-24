package com.crobot.core.drive;

import com.crobot.core.infra.tool.FileOperationFactory;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

import java.io.File;

public class FileSystemInitiator implements Initiator {
    private FileOperationFactory fileOperationFactory;

    public FileSystemInitiator(FileOperationFactory fileOperationFactory) {
        this.fileOperationFactory = fileOperationFactory;
    }


    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("FileSystem", new ObjApt() {
            @Caller("getFilesDir")
            public FileOperationApt getFilesDir() {
                return new FileOperationApt(fileOperationFactory.getFilesDir());
            }

            @Caller("getCacheDir")
            public FileOperationApt getCacheDir() {
                return new FileOperationApt(fileOperationFactory.getCacheDir());
            }

            @Caller("getModule")
            public FileOperationApt getModule(String root) {
                return new FileOperationApt(fileOperationFactory.getModule(root));
            }

            @Caller("exits")
            public boolean exits(String path) {
                return new File(path).exists();
            }

        });
    }


}
