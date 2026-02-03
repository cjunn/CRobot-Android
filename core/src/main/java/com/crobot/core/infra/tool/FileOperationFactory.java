package com.crobot.core.infra.tool;

public interface FileOperationFactory {
    FileOperation getModule(String root);
}
