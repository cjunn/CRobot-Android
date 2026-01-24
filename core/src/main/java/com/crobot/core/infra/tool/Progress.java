package com.crobot.core.infra.tool;

public interface Progress {
    void show(String title);

    void setProgress(int progress);

    void close();
}
