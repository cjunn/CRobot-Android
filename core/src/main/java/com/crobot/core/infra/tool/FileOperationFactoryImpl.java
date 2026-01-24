package com.crobot.core.infra.tool;

import android.content.Context;

import java.io.File;

public class FileOperationFactoryImpl implements FileOperationFactory {
    private Context context;

    public FileOperationFactoryImpl(Context context) {
        this.context = context;
    }

    public FileOperation getFilesDir() {
        return new FileOperationImpl(context.getFilesDir().getAbsolutePath());
    }

    public FileOperation getCacheDir() {
        return new FileOperationImpl(context.getCacheDir().getAbsolutePath());
    }

    public FileOperation getModule(String root) {
        return new FileOperationImpl(new File(context.getDataDir(), root).getAbsolutePath());
    }


}
