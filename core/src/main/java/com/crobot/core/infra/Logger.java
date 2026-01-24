package com.crobot.core.infra;

public interface Logger {
    void output(String tag, String source, int currentLine, String message);
}
