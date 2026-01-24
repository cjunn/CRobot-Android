package com.crobot.core.infra.tool;

import java.util.function.Consumer;

public interface Dialog {
    void alert(String title, String content, Runnable callback);

    void confirm(String title, String content, Consumer<Boolean> callback);

    void input(String title, String prefill, Consumer<String> callback);

    void clear();

}
