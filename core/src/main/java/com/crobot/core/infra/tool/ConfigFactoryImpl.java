package com.crobot.core.infra.tool;

public class ConfigFactoryImpl implements ConfigFactory {
    private FileOperation fileOperation;

    public ConfigFactoryImpl(FileOperationFactory fileOperationFactory) {
        this.fileOperation = fileOperationFactory.getModule("configs");
    }

    @Override
    public Config getConfig(String module) {
        return new ConfigImpl(fileOperation, module);
    }
}
