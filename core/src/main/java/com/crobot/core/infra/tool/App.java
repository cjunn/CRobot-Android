package com.crobot.core.infra.tool;

public interface App {
    String archite();

    String currentPackage();

    boolean launch(String packageName);

    void uninstall(String packageName);

    boolean viewFile(String path);

    boolean editFile(String path);

    boolean openUrl(String url);

    void getInstalledApps();

}
