package com.crobot.core.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

public class StackUtil {
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

    public static Throwable trackThrowable(Throwable e) {
        if (e == null || e.getCause() == null) {
            return e;
        }
        if (InvocationTargetException.class.isAssignableFrom(e.getClass()) ||
                ExecutionException.class.isAssignableFrom(e.getClass())) {
            return trackThrowable(e.getCause());
        }
        return e;
    }

}
