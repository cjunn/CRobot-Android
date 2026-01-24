package com.crobot.core.drive;

import com.crobot.core.infra.tool.FileOperation;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;

import java.io.File;

public class FileOperationApt extends ObjApt {
    private FileOperation fileOperation;

    public FileOperationApt(FileOperation fileOperation) {
        this.fileOperation = fileOperation;
    }

    @Caller("read")
    public byte[] read(String path) {
        return fileOperation.read(path);
    }

    @Caller("exits")
    public boolean exits(String path) {
        return fileOperation.exits(path);
    }

    @Caller("write")
    public void write(String path, byte[] body) {
        fileOperation.write(path, body);
    }

    @Caller("readText")
    public String readText(String path) {
        return fileOperation.readText(path);
    }

    @Caller("writeText")
    public void writeText(String path, String body) {
        fileOperation.writeText(path, body);
    }

    @Caller("getPath")
    public String getPath(String path) {
        return new File(fileOperation.pwd(), path).getAbsolutePath();
    }
}