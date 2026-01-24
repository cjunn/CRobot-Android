package com.crobot.runtime.engine;

import com.crobot.runtime.engine.boot.BootUtil;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScriptException extends RuntimeException implements Serializable {
    private String code;
    private String causeMessage;
    private List<Trace> traces;

    private static final Pattern PAREN_CONTENT_PATTERN = Pattern.compile("\\(([^)]+)\\)");

    public static Trace extractTrace(String input) {
        // 空值校验（不变）
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        input = input.trim();
        // 复用预编译的Pattern创建Matcher，避免重复编译
        Matcher matcher = PAREN_CONTENT_PATTERN.matcher(input);
        if (matcher.find()) {
            String content = matcher.group(1);
            String[] parts = content.split(":");
            if (parts.length == 2) {
                String identifier = parts[0].trim();
                String lineStr = parts[1].trim();

                try {
                    int lineNumber = Integer.parseInt(lineStr);
                    return new Trace(input, identifier, lineNumber);
                } catch (NumberFormatException e) {
                    return new Trace(input, identifier, 0);
                }
            }
        }
        return null;
    }

    public ScriptException(String code, String message, String traceback) {
        super(message);
        this.code = code;
        this.causeMessage = new StringBuilder()
                .append("Exception in thread \"")
                .append(Thread.currentThread().getName())
                .append("\": ")
                .append(message).toString();
        this.traces = Arrays.asList(traceback.split("\n"))
                .stream()
                .map(k -> extractTrace(k))
                .filter(k->k!=null)
                .collect(Collectors.toList());
    }


    public static Constructor<?> getJniConstructor() {
        return BootUtil.getJniConstructor(ScriptException.class);
    }

    public String getCauseMessage(){
        return causeMessage;
    }


    public String getCode() {
        return code;
    }


    public List<Trace> getTraces() {
        return traces;
    }


    public static class Trace {
        public final String body;
        public final String source;
        public final Integer line;

        public Trace(String body, String source, Integer line) {
            this.body = body;
            this.source = source;
            this.line = line;
        }
    }


}
