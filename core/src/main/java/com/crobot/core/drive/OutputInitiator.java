package com.crobot.core.drive;

import com.crobot.core.infra.tool.Output;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.FuncApt;
import com.crobot.runtime.engine.apt.anno.Execute;

public class OutputInitiator implements Initiator {

    private Output output;

    public OutputInitiator(Output output) {
        this.output = output;
    }


    @Override
    public void execute(ContextProxy context) {
        context.setFuncApt("output", new FuncApt() {
            @Execute
            public void execute(String source, Number currentLine, String message, String tag) {
                output.print(tag, source, currentLine.intValue(), message);
            }
        });
    }

}