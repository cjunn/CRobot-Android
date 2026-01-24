package com.crobot.runtime.engine;

import com.crobot.utils.CLog;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

public final class ContextException extends RuntimeException implements Serializable {

    private Throwable cause;
    private String message;

    public ContextException(Throwable cause) {
        cause = parseThrowable(cause);
        if (cause instanceof ContextException) {
            ContextException exception = (ContextException) cause;
            if (exception.message != null) {
                this.message = exception.message;
            } else {
                this.cause = exception.cause;
            }
            return;
        }
        this.cause = cause;
    }

    public ContextException(String message) {
        this.message = message;
    }

    public String getMessage() {
        if (cause != null) {
            return getStackTrace(cause);
        }
        return message;
    }

    private static Throwable parseThrowable(Throwable cause){
        if(cause instanceof InvocationTargetException){
            return parseThrowable(cause.getCause());
        }
        return cause;
    }


    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        try {
            sw.close();
        } catch (IOException e1) {
            //ignore
        }
        return sw.toString();
    }

}
