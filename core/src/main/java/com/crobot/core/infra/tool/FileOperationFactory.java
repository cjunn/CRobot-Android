package com.crobot.core.infra.tool;

public interface FileOperationFactory {
    FileOperation getFilesDir();

    FileOperation getCacheDir();

    FileOperation getModule(String root);
}
