package com.crobot.core.drive;

import com.crobot.core.infra.tool.App;
import com.crobot.runtime.engine.ContextProxy;
import com.crobot.runtime.engine.Initiator;
import com.crobot.runtime.engine.apt.ObjApt;
import com.crobot.runtime.engine.apt.anno.Caller;
import com.crobot.runtime.engine.apt.anno.Value;

public class AppInitiator implements Initiator {
    private String appVer;
    private String coreVer;
    private App app;

    public AppInitiator(App app, String appVer, String coreVer) {
        this.app = app;
        this.appVer = appVer;
        this.coreVer = coreVer;
    }

    @Override
    public void execute(ContextProxy context) {
        context.setObjApt("App", new AppApt(this.app, appVer, coreVer));
    }


    public static class AppApt extends ObjApt {
        @Value("appVer")
        private String appVer;
        @Value("coreVer")
        private String coreVer;
        @Value("archite")
        private String archite;

        @Value("coreNum")
        private Number coreNum;

        private App app;

        public AppApt(App app, String appVer, String coreVer) {
            this.app = app;
            this.appVer = appVer;
            this.coreVer = coreVer;
            this.archite = app.archite();
            this.coreNum = Runtime.getRuntime().availableProcessors();
        }

        @Caller("currentPackage")
        public String currentPackage() {
            return app.currentPackage();
        }

        @Caller("launch")
        public boolean launch(String packageName) {
            return app.launch(packageName);
        }

        @Caller("uninstall")
        public void uninstall(String packageName) {
            app.uninstall(packageName);
        }

        @Caller("viewFile")
        public boolean viewFile(String path) {
            return app.viewFile(path);
        }

        @Caller("editFile")
        public boolean editFile(String path) {
            return app.editFile(path);
        }

        @Caller("openUrl")
        public boolean openUrl(String url) {
            return app.openUrl(url);
        }

        @Caller("getInstalledApps")
        public void getInstalledApps() {
            app.getInstalledApps();
        }

    }

}
