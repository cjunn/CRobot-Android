package com.crobot.core.infra.tool;

public interface Output {
    void print(String tag, String source, int currentLine, String message);

    void error(Exception exception);
}
