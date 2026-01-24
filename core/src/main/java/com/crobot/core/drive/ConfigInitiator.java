package com.crobot.core.drive;

import com.crobot.core.infra.tool.Config;
import com.crobot.core.infra.tool.ConfigFactory;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.FuncApt;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;
import com.crobot.runtime.engine.apt.anno.Execute;

public class ConfigInitiator implements Initiator {
    private ConfigFactory configFactory;

    public ConfigInitiator(ConfigFactory configFactory) {
        this.configFactory = configFactory;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setFuncApt("getConfig", new FuncApt() {
            @Execute
            public ObjApt execute(String module) {
                return new ConfigObjApt(configFactory, module);
            }
        });
    }

    public static class ConfigObjApt extends ObjApt {
        private Config config;

        public ConfigObjApt(ConfigFactory configFactory, String module) {
            this.config = configFactory.getConfig(module);
        }

        @Caller("setValue")
        public void setValue(String key, Object value) {
            this.config.set(key, value);
        }

        @Caller("getValue")
        public synchronized Object getValue(String key) {
            return this.config.get(key);
        }

    }


}
