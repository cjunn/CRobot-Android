package com.crobot.core.infra.tool;

import com.crobot.core.infra.Logger;
import com.crobot.runtime.engine.ContextException;
import com.crobot.runtime.engine.LogTag;
import com.crobot.runtime.engine.ScriptException;
import com.crobot.utils.StackUtil;

public class OutputImpl implements Output {
    private Logger logger;

    public OutputImpl(Logger logger) {
        this.logger = logger;
    }

    @Override
    public synchronized void print(String tag, String source, int currentLine, String message) {
        logger.output(tag, source, currentLine, message);
    }

    @Override
    public synchronized void error(Exception exception) {
        if (exception instanceof ScriptException) {
            ScriptException se = (ScriptException) exception;
            String source = "";
            int line = 0;
            if (se.getTraces().size() > 0) {
                source = se.getTraces().get(0).source;
                line = se.getTraces().get(0).line;
            }
            logger.output(LogTag.ERROR, source, line, se.getCauseMessage());
            se.getTraces().forEach(trace -> logger.output(LogTag.ERROR, trace.source, trace.line, " " + trace.body));
            return;
        }
        if (exception instanceof ContextException) {
        }
        logger.output(LogTag.ERROR, "<jvm>", 0, StackUtil.getStackTrace(exception));
    }
}
